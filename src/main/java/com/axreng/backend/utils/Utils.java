package com.axreng.backend.utils;

import com.axreng.backend.model.error.ErrorResponse;
import com.axreng.backend.service.CrawlerService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import spark.Response;

import java.util.logging.Logger;

public class Utils {

    private static final Logger log = Logger.getLogger(Utils.class.getName());

    public static final Gson gson = new Gson();

    public static JsonObject getJsonObject(String requestBody) {
        if(requestBody != null && !requestBody.isBlank())
            return JsonParser.parseString(requestBody).getAsJsonObject();
        else
            return null;
    }

    public static String getEnvironmentVariable(String variableName) {
        String variable = System.getenv(variableName);

        if(variable == null || variable.isBlank()){
            log.warning(variableName + "it's not in the system");
        }

        return variable;
    }

    public static String setErrorResponse(int status, Response response, String message) {
        response.status(status);
        return gson.toJson(new ErrorResponse(status, message));
    }
}
