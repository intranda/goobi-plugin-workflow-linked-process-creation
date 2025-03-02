package de.intranda.goobi.plugins;

import de.sub.goobi.config.ConfigPlugins;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IGuiPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IWorkflowPlugin;

@PluginImplementation
@Log4j2
public class LinkedprocesscreationWorkflowPlugin implements IWorkflowPlugin, IGuiPlugin, IPlugin {

    public static String title = "intranda_workflow_linkedprocesscreation";

    @Getter
    private String value;

    @Override
    public PluginType getType() {
        return PluginType.Workflow;
    }

    @Override
    public String getGui() {
        return "/uii/plugin_workflow_linkedprocesscreation.xhtml";
    }

    /**
     * Constructor
     */
    public LinkedprocesscreationWorkflowPlugin() {
        log.info("Linkedprocesscreation workflow plugin started");
        value = ConfigPlugins.getPluginConfig(title).getString("value", "default value");
    }

    @Override
    public String[] getJsPaths() {
        return new String[] { "app.js" };
    }

    @Override
    public String getTitle() {
        return title;
    }
}
