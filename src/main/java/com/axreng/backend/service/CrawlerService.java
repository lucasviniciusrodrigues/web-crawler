package com.axreng.backend.service;

import com.axreng.backend.config.AppConfig;
import com.axreng.backend.exception.NotFoundException;
import com.axreng.backend.entity.CrawlerBaseEntity;
import com.axreng.backend.model.CrawlerDetailModel;
import com.axreng.backend.usecase.CrawlerUseCase;
import com.google.gson.JsonObject;

import java.util.logging.Logger;

import static com.axreng.backend.constants.Constants.KEYWORD_ERROR_MESSAGE;
import static com.axreng.backend.constants.Constants.SEARCH_KEY_PROPERTIES;
import static com.axreng.backend.utils.Utils.gson;

public class CrawlerService {

    private static final Logger log = Logger.getLogger(CrawlerService.class.getName());
    private final String searchKey;
    private CrawlerUseCase crawlerUseCase;

    public CrawlerService(CrawlerUseCase crawlerUseCase){
        this.searchKey = AppConfig.getProperty(SEARCH_KEY_PROPERTIES);
        this.crawlerUseCase = crawlerUseCase;
    }

    public String get(String id) throws NotFoundException {

        log.info("Getting response for ID: " + id);

        if(!crawlerUseCase.isQueued(id)){
            log.warning("ID not found: " + id);
            throw new NotFoundException("ID " + id + " not found in the queue");
        }

        return gson.toJson(
                new CrawlerDetailModel(crawlerUseCase.getResult(id)));
    }

    public CrawlerBaseEntity post(String keyword) throws IllegalAccessException {

        try {

            log.info("Posting new search for keyword: " + keyword);

            return new CrawlerBaseEntity(crawlerUseCase.put(keyword));

        }catch (IllegalArgumentException e){
            throw new IllegalAccessException(KEYWORD_ERROR_MESSAGE + getSearchKey());
        }


    }

    public String getValidKeyword(JsonObject jsonObject) {

        try {

            if(!jsonObject.has(searchKey))
                throw new Exception();


            String value = jsonObject.get(searchKey).getAsString();

            if (!crawlerUseCase.isValidKeyword(value))
                throw new Exception();

            return value;

        } catch (Exception e){
            throw new IllegalArgumentException(KEYWORD_ERROR_MESSAGE + getSearchKey());
        }

    }

    public String getSearchKey() {
        return searchKey;
    }

}
