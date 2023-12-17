package com.axreng.backend.model;

import com.axreng.backend.domain.CrawlerDomain;
import com.axreng.backend.entity.CrawlerBaseEntity;

import java.util.HashSet;
import java.util.Set;

public class CrawlerDetailModel extends CrawlerBaseEntity {

    public CrawlerDetailModel(CrawlerDomain domain){
        super(domain.getId());
        this.status = domain.getStatus();
        this.urls = domain.getUrls();
    }
    private String status;
    private Set<String> urls = new HashSet<>();

    public CrawlerDetailModel() {}
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