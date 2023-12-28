package com.axr.backend;

import com.axr.backend.constants.CrawlStatus;
import com.axr.backend.domain.CrawlerDomain;
import com.axr.backend.entity.CrawlerBaseEntity;
import com.axr.backend.model.CrawlerDetailModel;
import com.axr.backend.model.error.ErrorResponse;
import com.google.gson.Gson;

public class Mocks {

    public static final Gson gson = new Gson();

    public static String validKeyowrd = "1a2B3c4D";

    public static CrawlerBaseEntity getCrawlerBaseEntity(){
        return new CrawlerBaseEntity(validKeyowrd);
    }

    public static String getCrawlerBaseEntityAsString(){
        return gson.toJson(new CrawlerBaseEntity(validKeyowrd));
    }

    public static CrawlerDomain getCrawlerDomain(){
        CrawlerDomain crawlerDomain = new CrawlerDomain(validKeyowrd, CrawlStatus.ACTIVE.getStatusDescription());
        crawlerDomain.getUrls().add("http://url/aqui");
        crawlerDomain.getUrls().add("http://url/la");

        return crawlerDomain;
    }

    public static CrawlerDetailModel getCrawlerDetailModel(){
        return new CrawlerDetailModel(getCrawlerDomain());
    }

    public static String  getCrawlerDetailModelAsString(){
        return gson.toJson(new CrawlerDetailModel(getCrawlerDomain()));
    }

    public static String getErrorAsString(int status, String message){
        return gson.toJson(new ErrorResponse(status, message));
    }


}
