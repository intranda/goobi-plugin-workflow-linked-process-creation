package org.goobi.api.rest;

import com.google.gson.Gson;
import de.intranda.goobi.plugins.processcreation.model.*;
import de.intranda.goobi.plugins.processcreation.model.json.vocabulary.JsonVocabulary;
import de.intranda.goobi.plugins.processcreation.model.json.vocabulary.VocabularyBuilder;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import io.goobi.workflow.api.vocabulary.APIException;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.goobi.beans.Process;
import ugh.dl.*;
import ugh.exceptions.*;
import ugh.fileformats.mets.MetsMods;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Path("/plugins/processcreation")
public class LinkedprocesscreationWorkflowPluginApi {
    public static String title = "intranda_workflow_linkedprocesscreation";
    private static final VocabularyAPIManager vocabularyAPI = VocabularyAPIManager.getInstance();
    private static final VocabularyBuilder vocabularyBuilder = new VocabularyBuilder(vocabularyAPI);
    private static Gson gson = new Gson();

    @GET
    @Path("/vocabularies")
    public Map<String, JsonVocabulary> allVocabs() {
        XMLConfiguration conf = ConfigPlugins.getPluginConfig(title);
        List<Column> colList = readColsFromConfig(conf);
        Map<String, JsonVocabulary> vocabMap = new TreeMap<>();
        Set<String> vocabularyNames = new HashSet<>();
        colList.stream()
                .flatMap(col -> col.getBoxes().stream())
                .flatMap(box -> box.getFields().stream())
                .forEach(field -> {
                    vocabularyNames.addAll(field.getSourceVocabularies());
                    vocabularyNames.addAll(field.getGroupMappings().stream().map(GroupMapping::getSourceVocabulary).toList());
                });
        vocabularyNames.stream()
                .filter(Objects::nonNull)
                .forEach(vocabName -> {
                    try {
                        ExtendedVocabulary vocab = vocabularyAPI.vocabularies().findByName(vocabName);
                        vocabMap.put(vocabName, vocabularyBuilder.buildVocabulary(vocab));
                    } catch (APIException e) {
                        String message = "Unable to retrieve vocabulary \"" + vocabName + "\"";
                        Helper.setFehlerMeldung(message);
                        log.error(message);
                    }
                });
        return vocabMap;
    }

    @GET
    @Path("/allCreationScreens")
    public List<ProcessCreationScreen> allCreationScreens() {
        XMLConfiguration conf = ConfigPlugins.getPluginConfig(title);
        conf.setExpressionEngine(new XPathExpressionEngine());
        return conf.configurationsAt("//type")
                .stream()
                .map(ProcessCreationScreen::fromConfig)
                .collect(Collectors.toList());
    }

    //            http.post("/processes", Handlers.createProcesses, gson::toJson);
    @POST
    @Path("/processes")
    public Response createProcesses(String body) throws DAOException, ReadException, WriteException, SwapException, MetadataTypeNotAllowedException, IOException, PreferencesException {
        ProcessCreationScreen screen = gson.fromJson(body, ProcessCreationScreen.class);
        Map<String, org.goobi.beans.Process> createdProcesses = new HashMap<>();
        for (ProcessIdentifier processId : screen.getProcesses()) {
            try {
                createdProcesses.put(processId.getId(), createProcess(processId, screen.getColumns()));
            } catch (Exception e) {
                log.error(e);
                return Response.serverError().entity(e.getMessage()).build();
            }
        }
        for (ProcessRelation relation : screen.getRelations()) {
            connectProcesses(createdProcesses, relation);
        }
        return Response.ok().build();
    }

    private static List<Column> readColsFromConfig(XMLConfiguration conf) {
        conf.setExpressionEngine(new XPathExpressionEngine());

        return conf.configurationsAt("//column")
                .stream()
                .map(Column::fromConfig)
                .collect(Collectors.toList());
    }

    private static void connectProcesses(Map<String, org.goobi.beans.Process> createdProcesses, ProcessRelation relation) throws PreferencesException, ReadException,
            WriteException, IOException, SwapException, MetadataTypeNotAllowedException {
        org.goobi.beans.Process sourceProcess = createdProcesses.get(relation.getSourceProcessID());
        DocStruct sourceDocStruct = readDocStruct(sourceProcess);
        Optional<Metadata> sourceMd = sourceDocStruct.getAllMetadata()
                .stream()
                .filter(md -> md.getType().getName().equals(relation.getSourceMetadataType()))
                .findAny();
        if (sourceMd.isEmpty()) {
            throw new MetadataTypeNotAllowedException("Could not find source metadata");
        }
        org.goobi.beans.Process targetProcess = createdProcesses.get(relation.getTargetProcessID());
        writeMetadataToTarget(relation, sourceMd, targetProcess);
    }

    private static void writeMetadataToTarget(ProcessRelation relation, Optional<Metadata> sourceMd, org.goobi.beans.Process targetProcess) throws ReadException,
            IOException, PreferencesException, SwapException, WriteException, MetadataTypeNotAllowedException {
        Fileformat ff = targetProcess.readMetadataFile();
        DigitalDocument dd = ff.getDigitalDocument();
        DocStruct targetDocStruct = dd.getLogicalDocStruct();
        Prefs targetPrefs = targetProcess.getRegelsatz().getPreferences();
        MetadataType targetType = targetPrefs.getMetadataTypeByName(relation.getTargetMetadataType());
        Metadata targetMd = new Metadata(targetType);
        targetMd.setValue(sourceMd.get().getValue());
        targetDocStruct.addMetadata(targetMd);
        targetProcess.writeMetadataFile(ff);
    }

    private static DocStruct readDocStruct(org.goobi.beans.Process process)
            throws PreferencesException, ReadException, IOException, SwapException {
        Fileformat ff = process.readMetadataFile();
        DigitalDocument dd = ff.getDigitalDocument();
        return dd.getLogicalDocStruct();
    }

    private static org.goobi.beans.Process createProcess(ProcessIdentifier processIdentifier, List<Column> columns)
            throws DAOException, PreferencesException, TypeNotAllowedForParentException, MetadataTypeNotAllowedException, WriteException, IOException,
            SwapException {
        org.goobi.beans.Process template = ProcessManager.getProcessByExactTitle(processIdentifier.getTemplateName());
        org.goobi.beans.Process createdProcess = cloneTemplate(template);
        String processTitle = findProcessTitle(processIdentifier, columns);
        createdProcess.setTitel(processTitle);
        Fileformat fileFormat = createMetadataForProcess(createdProcess, processIdentifier, columns);
        ProcessManager.saveProcess(createdProcess);
        createdProcess.writeMetadataFile(fileFormat);
        return createdProcess;
    }

    private static String findProcessTitle(ProcessIdentifier processIdentifier, List<Column> columns) {
        Optional<String> processTitle = columns
                .stream()
                .flatMap(col -> col.getBoxes().stream())
                .flatMap(box -> box.getFields().stream())
                .filter(Field::isProcessTitle)
                .filter(field -> field.getProcessID().equals(processIdentifier.getId()))
                .flatMap(field -> field.getValues().stream())
                .map(FieldValue::getValue)
                .findAny();

        return processTitle.orElseThrow();
    }

    private static Fileformat createMetadataForProcess(org.goobi.beans.Process process, ProcessIdentifier processIdentifier, List<Column> columns)
            throws PreferencesException, TypeNotAllowedForParentException, MetadataTypeNotAllowedException {
        Prefs prefs = process.getRegelsatz().getPreferences();
        Fileformat fileformat = new MetsMods(prefs);
        DigitalDocument digDoc = new DigitalDocument();
        fileformat.setDigitalDocument(digDoc);
        DocStruct logicalDs = digDoc.createDocStruct(prefs.getDocStrctTypeByName(processIdentifier.getLogicalDSType()));
        digDoc.setLogicalDocStruct(logicalDs);
        if (digDoc.getPhysicalDocStruct() == null) {
            DocStruct physical = digDoc.createDocStruct(prefs.getDocStrctTypeByName("BoundBook"));
            digDoc.setPhysicalDocStruct(physical);
        }

        for (Column col : columns) {
            for (Box box : col.getBoxes()) {
                for (Field field : box.getFields()) {
                    if (!field.getProcessID().equals(processIdentifier.getId())) {
                        continue;
                    }
                    if (field.getMetadatatype() == null || field.getMetadatatype().isEmpty()) {
                        continue;
                    }
                    // add metadata to new process
                    MetadataType mdt = prefs.getMetadataTypeByName(field.getMetadatatype());
                    if (mdt == null) {
                        throw new MetadataTypeNotAllowedException("Coudl not find metadata type '" + field.getMetadatatype() + "'");
                    }
                    for (FieldValue value : field.getValues()) {
                        Metadata md;
                        md = new Metadata(mdt);
                        md.setValue(value.getValue());
                        logicalDs.addMetadata(md);
                    }
                }
            }
        }
        return fileformat;
    }

    private static org.goobi.beans.Process cloneTemplate(org.goobi.beans.Process template) {
        org.goobi.beans.Process process = new Process();

        process.setIstTemplate(false);
        process.setInAuswahllisteAnzeigen(false);
        process.setProjekt(template.getProjekt());
        process.setRegelsatz(template.getRegelsatz());
        process.setDocket(template.getDocket());

        BeanHelper bHelper = new BeanHelper();
        bHelper.SchritteKopieren(template, process);
        bHelper.EigenschaftenKopieren(template, process);

        return process;
    }
}
