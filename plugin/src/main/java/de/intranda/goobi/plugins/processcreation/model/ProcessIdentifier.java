package de.intranda.goobi.plugins.processcreation.model;

import org.apache.commons.configuration.HierarchicalConfiguration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessIdentifier {
    private String id;
    private String templateName;

    public static ProcessIdentifier fromConfig(HierarchicalConfiguration conf) {
        String id = conf.getString("./@id");
        String template = conf.getString("./@template");

        return new ProcessIdentifier(id, template);
    }
}
