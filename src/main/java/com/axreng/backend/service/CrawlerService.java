package com.axreng.backend.service;

import com.axreng.backend.config.AppConfig;
import com.axreng.backend.exception.NotFoundException;
import com.axreng.backend.model.SearchCrawlerResponse;
import com.axreng.backend.usecase.CrawlerUseCase;
import com.google.gson.JsonObject;

import java.util.logging.Logger;

import static com.axreng.backend.constants.Constants.SEARCH_KEY_PROPERTIES;
import static com.axreng.backend.utils.Utils.gson;

public class CrawlerService {

    private static final Logger log = Logger.getLogger(CrawlerService.class.getName());
    private final String searchKey;
    private CrawlerUseCase crawlerUseCase;

    public CrawlerService(CrawlerUseCase crawlerQueueService){
        this.searchKey = AppConfig.getProperty(SEARCH_KEY_PROPERTIES);
        this.crawlerUseCase = crawlerQueueService;
    }

    public String get(String id) throws NotFoundException {

        log.info("Getting response for ID: " + id);

        if(!crawlerUseCase.isQueued(id)){
            log.warning("ID not found: " + id);
            throw new NotFoundException("ID " + id + " not found in the queue");
        }


        return gson.toJson(crawlerUseCase.getResult(id));
    }

    public SearchCrawlerResponse post(String keyword)  {

        log.info("Posting new search for keyword: " + keyword);

        return new SearchCrawlerResponse(crawlerUseCase.put(keyword));

    }

    public String getValidKeyword(JsonObject jsonObject) {

        if(!jsonObject.has(searchKey))
            return null;

        String value = jsonObject.get(searchKey).getAsString();

        if(value == null || value.length() < 4
                || value.length() > 32 || value.isBlank())
            return null;

        return value;
    }

    public String getSearchKey() {
        return searchKey;
    }

}
