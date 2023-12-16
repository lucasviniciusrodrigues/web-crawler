package com.axreng.backend.model;

import com.axreng.backend.domain.SearchCrawlerDetailDomain;

import java.util.HashSet;
import java.util.Set;

public class SearchCrawlerDetailModel extends SearchCrawlerResponse {

    public SearchCrawlerDetailModel(SearchCrawlerDetailDomain domain){
        super(domain.getId());
        this.status = domain.getStatus();
        this.urls = domain.getUrls();
    }
    private String status;
    private Set<String> urls = new HashSet<>();

    public SearchCrawlerDetailModel() {}
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
