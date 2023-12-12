package com.axreng.backend.controller;

import com.axreng.backend.exception.NotFoundException;
import com.axreng.backend.service.CrawlerService;
import com.google.gson.JsonObject;
import spark.Request;
import spark.Response;

import java.util.logging.Logger;

import static com.axreng.backend.constants.Constants.KEYWORD_ERROR_MESSAGE;
import static com.axreng.backend.utils.Utils.*;
import static spark.Spark.*;

public class CrawlerController {

    private static final Logger log = Logger.getLogger(CrawlerService.class.getName()); // TODO Alterar o formato das logs

    private CrawlerService crawlerService;

    public CrawlerController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    public void setupRoutes() {
        log.info("Setting up routes");

        before((req, res) -> res.type("application/json"));

        path("/crawl", () -> {

            get("/:id", (req, res) -> getById(req, res));

            post("", (req, res) -> postCrawler(req, res));
        });

        log.info("Routes ON");

    }

    private String postCrawler(Request request, Response response) {

        try {
            log.info("POST - Posting new request");

            JsonObject jsonObject = getJsonObject(request.body());
            String keyword = crawlerService.getValidKeyword(jsonObject);

            if(keyword == null){
                log.warning(KEYWORD_ERROR_MESSAGE);
                throw new IllegalArgumentException(KEYWORD_ERROR_MESSAGE);
            }

            return gson.toJson(crawlerService.post(keyword));

        } catch (IllegalArgumentException e){
            return setErrorResponse(400,  response, "The key \'"+ crawlerService.getSearchKey() +"\' it's mandatory in the body and must have between 4 and 32 characters");
        }

    }

    private String getById(Request req, Response response) {

        log.info("GET - Getting search");

        try {

            String id = req.params("id");

            if(id == null || id.isBlank() || id.length() != 8){
                log.warning("GET - The ID generated by the search must be sended in the path with 8 characters");
                return setErrorResponse(400,  response, "The ID generated by the search must be sended in the path with 8 characters");
            }

            return crawlerService.get(req.params("id"));

        } catch (NotFoundException e){
            return setErrorResponse(404, response, e.getMessage());
        }

    }

}