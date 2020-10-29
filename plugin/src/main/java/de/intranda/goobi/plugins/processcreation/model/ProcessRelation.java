package de.intranda.goobi.plugins.processcreation.model;

import org.apache.commons.configuration.HierarchicalConfiguration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessRelation {
    private String sourceProcessID;
    private String targetProcessID;
    private String type;

    public static ProcessRelation fromConfig(HierarchicalConfiguration conf) {
        String sourceProcessID = conf.getString("./@sourceProcessID");
        String targetProcessID = conf.getString("./@targetProcessID");
        String type = conf.getString("./@type");

        return new ProcessRelation(sourceProcessID, targetProcessID, type);
    }
}
