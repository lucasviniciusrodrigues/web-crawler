package com.axr.backend.domain;

import com.axr.backend.constants.CrawlStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlerIndexDomain {
    private static CrawlerIndexDomain instance;
    private final CrawlerIdGeneratorDomain idGenerator;
    private final Map<String, CrawlerDomain> searchedKeywords = new ConcurrentHashMap<>();

    private CrawlerIndexDomain(CrawlerIdGeneratorDomain idGenerator) {
        this.idGenerator = idGenerator;
    }

    public static synchronized CrawlerIndexDomain getInstance(CrawlerIdGeneratorDomain idGenerator) {
        if (instance == null) {
            instance = new CrawlerIndexDomain(idGenerator);
        }
        return instance;
    }

    public Map<String, CrawlerDomain> getSearchedKeywords() {
        return searchedKeywords;
    }

    synchronized public boolean haveStatus(String keyword, CrawlStatus status) {
        return getSearchedKeywords().get(keyword).getStatus().equals(status.getStatusDescription());
    }

    synchronized public void updateStatus(String keyword, CrawlStatus status) {
        getSearchedKeywords().get(keyword).setStatus(status.getStatusDescription());
    }

    public CrawlerDomain generateUniqueID(String keyword) {
        return idGenerator.generateUniqueID(keyword, getSearchedKeywords());
    }

    public Map<String, String> getGeneratedIds() {
        return idGenerator.getGeneratedIds();
    }
}
