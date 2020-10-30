package de.intranda.goobi.plugins.processcreation.model;

import org.apache.commons.configuration.HierarchicalConfiguration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessRelation {
    private String sourceProcessID;
    private String targetProcessID;
    private String sourceMetadataType;
    private String targetMetadataType;

    public static ProcessRelation fromConfig(HierarchicalConfiguration conf) {
        String sourceProcessID = conf.getString("./@sourceProcessId");
        String targetProcessID = conf.getString("./@targetProcessId");
        String sourceMetadataType = conf.getString("./@sourceMetadataType");
        String targetMetadataType = conf.getString("./@targetMetadataType");

        return new ProcessRelation(sourceProcessID, targetProcessID, sourceMetadataType, targetMetadataType);
    }
}
