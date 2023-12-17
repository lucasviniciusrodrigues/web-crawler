package com.axreng.backend.utils;

import com.axreng.backend.model.error.ErrorResponse;
import com.google.gson.Gson;
import spark.Response;

import java.util.logging.Logger;

public class Utils {

    private static final Logger log = Logger.getLogger(Utils.class.getName());

    public static final Gson gson = new Gson();

    public static String getEnvironmentVariable(String variableName) {
        String variable = System.getenv(variableName);

        if(variable == null || variable.isBlank()){
            log.severe(variableName + "it's not in the system");
        }

        return variable;
    }

    public static String setErrorResponse(int status, Response response, String message) {
        response.status(status);
        return gson.toJson(new ErrorResponse(status, message));
    }
}
