package de.intranda.goobi.plugins.processcreation.model;

import org.apache.commons.configuration.HierarchicalConfiguration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessIdentifier {
    private String id;
    private String templateName;
    private String logicalDSType;

    public static ProcessIdentifier fromConfig(HierarchicalConfiguration conf) {
        String id = conf.getString("./@id");
        String template = conf.getString("./@template");
        String logicalDSType = conf.getString("./@logicalDSType");

        return new ProcessIdentifier(id, template, logicalDSType);
    }
}
