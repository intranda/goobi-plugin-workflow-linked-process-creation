package de.intranda.goobi.plugins.processcreation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.goobi.beans.Process;
import org.goobi.vocabulary.Vocabulary;

import com.google.gson.Gson;

import de.intranda.goobi.plugins.LinkedprocesscreationWorkflowPlugin;
import de.intranda.goobi.plugins.processcreation.model.Box;
import de.intranda.goobi.plugins.processcreation.model.Column;
import de.intranda.goobi.plugins.processcreation.model.Field;
import de.intranda.goobi.plugins.processcreation.model.FieldValue;
import de.intranda.goobi.plugins.processcreation.model.GroupMapping;
import de.intranda.goobi.plugins.processcreation.model.ProcessCreationScreen;
import de.intranda.goobi.plugins.processcreation.model.ProcessIdentifier;
import de.intranda.goobi.plugins.processcreation.model.ProcessRelation;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.VocabularyManager;
import lombok.extern.log4j.Log4j2;
import spark.Route;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;

@Log4j2
public class Handlers {
    private static Gson gson = new Gson();
    public static Route allVocabs = (req, res) -> {
        XMLConfiguration conf = ConfigPlugins.getPluginConfig(LinkedprocesscreationWorkflowPlugin.title);
        List<Column> colList = readColsFromConfig(conf);
        Map<String, Vocabulary> vocabMap = new TreeMap<>();
        for (Column col : colList) {
            for (Box box : col.getBoxes()) {
                for (Field field : box.getFields()) {
                    for (String vocabName : field.getSourceVocabularies()) {
                        if (vocabName != null && !vocabMap.containsKey(vocabName)) {
                            Vocabulary vocab = VocabularyManager.getVocabularyByTitle(vocabName);
                            VocabularyManager.getAllRecords(vocab);
                            vocabMap.put(vocabName, vocab);
                        }
                    }
                    for (GroupMapping gm : field.getGroupMappings()) {
                        String vocabName = gm.getSourceVocabulary();
                        if (vocabName != null && !vocabMap.containsKey(vocabName)) {
                            Vocabulary vocab = VocabularyManager.getVocabularyByTitle(vocabName);
                            VocabularyManager.getAllRecords(vocab);
                            vocabMap.put(vocabName, vocab);
                        }
                    }
                }
            }
        }
        return vocabMap;
    };

    public static Route allCreationScreens = (req, res) -> {
        XMLConfiguration conf = ConfigPlugins.getPluginConfig(LinkedprocesscreationWorkflowPlugin.title);
        conf.setExpressionEngine(new XPathExpressionEngine());
        List<ProcessCreationScreen> screens = conf.configurationsAt("//type")
                .stream()
                .map(node -> ProcessCreationScreen.fromConfig(node))
                .collect(Collectors.toList());
        return screens;
    };

    public static Route createProcesses = (req, res) -> {
        ProcessCreationScreen screen = gson.fromJson(req.body(), ProcessCreationScreen.class);
        Map<String, Process> createdProcesses = new HashMap<>();
        for (ProcessIdentifier processId : screen.getProcesses()) {
            try {
                createdProcesses.put(processId.getId(), createProcess(processId, screen.getColumns()));
            } catch (Exception e) {
                log.error(e);
                res.body(e.getMessage());
                res.status(500);
            }
        }
        for (ProcessRelation relation : screen.getRelations()) {
            connectProcesses(createdProcesses, relation);
        }
        return "";
    };

    private static List<Column> readColsFromConfig(XMLConfiguration conf) {
        conf.setExpressionEngine(new XPathExpressionEngine());
        List<Column> colList = conf.configurationsAt("//column")
                .stream()
                .map(node -> Column.fromConfig(node))
                .collect(Collectors.toList());

        return colList;
    }

    private static void connectProcesses(Map<String, Process> createdProcesses, ProcessRelation relation) throws PreferencesException, ReadException,
            WriteException, IOException, InterruptedException, SwapException, DAOException, MetadataTypeNotAllowedException {
        Process sourceProcess = createdProcesses.get(relation.getSourceProcessID());
        DocStruct sourceDocStruct = readDocStruct(sourceProcess);
        Optional<Metadata> sourceMd = sourceDocStruct.getAllMetadata()
                .stream()
                .filter(md -> md.getType().getName().equals(relation.getSourceMetadataType()))
                .findAny();
        if (!sourceMd.isPresent()) {
            throw new MetadataTypeNotAllowedException("Could not find source metadata");
        }
        Process targetProcess = createdProcesses.get(relation.getTargetProcessID());
        writeMetadataToTarget(relation, sourceMd, targetProcess);
    }

    private static void writeMetadataToTarget(ProcessRelation relation, Optional<Metadata> sourceMd, Process targetProcess) throws ReadException,
            IOException, InterruptedException, PreferencesException, SwapException, DAOException, WriteException, MetadataTypeNotAllowedException {
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

    private static DocStruct readDocStruct(Process process)
            throws PreferencesException, ReadException, WriteException, IOException, InterruptedException, SwapException, DAOException {
        Fileformat ff = process.readMetadataFile();
        DigitalDocument dd = ff.getDigitalDocument();
        return dd.getLogicalDocStruct();
    }

    private static Process createProcess(ProcessIdentifier processIdentifier, List<Column> columns)
            throws DAOException, PreferencesException, TypeNotAllowedForParentException, MetadataTypeNotAllowedException, WriteException, IOException,
            InterruptedException, SwapException {
        Process template = ProcessManager.getProcessByExactTitle(processIdentifier.getTemplateName());
        Process createdProcess = cloneTemplate(template);
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
                .filter(field -> field.isProcessTitle())
                .filter(field -> field.getProcessID().equals(processIdentifier.getId()))
                .flatMap(field -> field.getValues().stream())
                .map(val -> val.getValue())
                .findAny();

        return processTitle.get();
    }

    private static Fileformat createMetadataForProcess(Process process, ProcessIdentifier processIdentifier, List<Column> columns)
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

    private static Process cloneTemplate(Process template) {
        Process process = new Process();

        process.setIstTemplate(false);
        process.setInAuswahllisteAnzeigen(false);
        process.setProjekt(template.getProjekt());
        process.setRegelsatz(template.getRegelsatz());
        process.setDocket(template.getDocket());

        BeanHelper bHelper = new BeanHelper();
        bHelper.SchritteKopieren(template, process);
        bHelper.ScanvorlagenKopieren(template, process);
        bHelper.WerkstueckeKopieren(template, process);
        bHelper.EigenschaftenKopieren(template, process);

        return process;
    }
}
