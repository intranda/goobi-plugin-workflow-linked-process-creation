package de.intranda.goobi.plugins.processcreation.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.configuration.HierarchicalConfiguration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessCreationScreen {
    private String name;
    private List<ProcessIdentifier> processes;
    private List<ProcessRelation> relations;
    private List<Column> columns;

    public static ProcessCreationScreen fromConfig(HierarchicalConfiguration conf) {
        String name = conf.getString("./@name");
        List<ProcessIdentifier> processes = conf.configurationsAt("./process")
                .stream()
                .map(node -> ProcessIdentifier.fromConfig(node))
                .collect(Collectors.toList());
        List<ProcessRelation> relations = conf.configurationsAt("./relation")
                .stream()
                .map(node -> ProcessRelation.fromConfig(node))
                .collect(Collectors.toList());
        List<Column> columns = conf.configurationsAt("./column")
                .stream()
                .map(node -> Column.fromConfig(node))
                .collect(Collectors.toList());
        return new ProcessCreationScreen(name, processes, relations, columns);
    }
}
