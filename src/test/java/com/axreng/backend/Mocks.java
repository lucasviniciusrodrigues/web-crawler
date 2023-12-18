package com.axreng.backend;

import com.axreng.backend.constants.CrawlStatus;
import com.axreng.backend.domain.CrawlerDomain;
import com.axreng.backend.entity.CrawlerBaseEntity;
import com.axreng.backend.model.CrawlerDetailModel;

public class Mocks {

    public static String validKeyowrd = "1a2B3c4D";

    public static CrawlerBaseEntity getCrawlerBaseEntity(){
        return new CrawlerBaseEntity(validKeyowrd);
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
}
