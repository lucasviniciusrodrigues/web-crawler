package com.axreng.backend.domain;

import com.axreng.backend.constants.CrawlStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CrawlerIndexDomain {

    private static final Logger log = Logger.getLogger(CrawlerIndexDomain.class.getName());
    private static CrawlerIndexDomain instance;
    private final CrawlerIdGeneratorDomain idGenerator;
    private final Map<String, CrawlerDetailDomain> searchedKeywords = new HashMap<>();


    private CrawlerIndexDomain(CrawlerIdGeneratorDomain idGenerator) {
        this.idGenerator = idGenerator;
    }

    public static synchronized CrawlerIndexDomain getInstance(CrawlerIdGeneratorDomain idGenerator) {
        if (instance == null) {
            instance = new CrawlerIndexDomain(idGenerator);
        }
        return instance;
    }

    public Map<String, CrawlerDetailDomain> getSearchedKeywords() {
        return searchedKeywords;
    }

    synchronized public boolean haveStatus(String keyword, CrawlStatus status) {
        return getSearchedKeywords().get(keyword).getStatus().equals(status.getStatusDescription());
    }

    synchronized public void updateStatus(String keyword, CrawlStatus status) {
        getSearchedKeywords().get(keyword).setStatus(status.getStatusDescription());
    }

    public CrawlerDetailDomain generateUniqueID(String keyword) {
        return idGenerator.generateUniqueID(keyword, getSearchedKeywords());
    }

    public Map<String, String> getGeneratedIds() {
        return idGenerator.getGeneratedIds();
    }
}
