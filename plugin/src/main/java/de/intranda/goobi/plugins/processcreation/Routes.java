package de.intranda.goobi.plugins.processcreation;

import com.google.gson.Gson;

import spark.Service;

public class Routes {
    private static Gson gson = new Gson();

    public static void initRoutes(Service http) {
        http.path("/processcreation", () -> {
            http.get("/vocabularies", Handlers.allVocabs, gson::toJson);
            http.get("/allCreationScreens", Handlers.allCreationScreens, gson::toJson);
            http.post("/processes", Handlers.createProcesses, gson::toJson);
        });

    }
}
