package com.axreng.backend.model;

import java.util.HashSet;
import java.util.Set;

public class SearchCrawlerDetailResponse extends SearchCrawlerResponse {

    private String status;
    private Set<String> urls = new HashSet<>();

    public SearchCrawlerDetailResponse() {}
    public SearchCrawlerDetailResponse(String generatedId) {
        super(generatedId);
    }

    public SearchCrawlerDetailResponse(String generatedId, String status) {
        super(generatedId);
        this.status = status;
    }

    public Set<String> getUrls() {
        return urls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
