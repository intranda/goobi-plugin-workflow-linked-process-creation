package de.intranda.goobi.plugins.processcreation;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.goobi.vocabulary.Vocabulary;

import de.intranda.goobi.plugins.LinkedprocesscreationWorkflowPlugin;
import de.intranda.goobi.plugins.processcreation.model.Box;
import de.intranda.goobi.plugins.processcreation.model.Column;
import de.intranda.goobi.plugins.processcreation.model.Field;
import de.intranda.goobi.plugins.processcreation.model.GroupMapping;
import de.intranda.goobi.plugins.processcreation.model.ProcessCreationScreen;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.persistence.managers.VocabularyManager;
import spark.Route;

public class Handlers {
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

    private static List<Column> readColsFromConfig(XMLConfiguration conf) {
        conf.setExpressionEngine(new XPathExpressionEngine());
        List<Column> colList = conf.configurationsAt("//column")
                .stream()
                .map(node -> Column.fromConfig(node))
                .collect(Collectors.toList());

        return colList;
    }
}
