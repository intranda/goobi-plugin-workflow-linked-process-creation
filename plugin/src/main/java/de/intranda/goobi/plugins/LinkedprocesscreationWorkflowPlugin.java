package de.intranda.goobi.plugins;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IRestPlugin;
import org.goobi.production.plugin.interfaces.IWorkflowPlugin;

import de.intranda.goobi.plugins.processcreation.Routes;
import de.sub.goobi.config.ConfigPlugins;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import spark.Service;

@PluginImplementation
@Log4j2
public class LinkedprocesscreationWorkflowPlugin implements IWorkflowPlugin, IPlugin, IRestPlugin {

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
        return new String[] { "js/app.js" };
    }

    @Override
    public void initRoutes(Service http) {
        Routes.initRoutes(http);
    }

    @Override
    public String getTitle() {
        return title;
    }
}
