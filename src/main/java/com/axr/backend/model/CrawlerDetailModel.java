package com.axr.backend.model;

import com.axr.backend.domain.CrawlerDomain;
import com.axr.backend.entity.CrawlerBaseEntity;

import java.util.HashSet;
import java.util.Set;

public class CrawlerDetailModel extends CrawlerBaseEntity {

    public CrawlerDetailModel(CrawlerDomain domain){
        super(domain.getId());
        this.status = domain.getStatus();
        this.urls.addAll(domain.getUrls());
    }
    private String status;
    private Set<String> urls = new HashSet<>();

    public Set<String> getUrls() {
        return urls;
    }

    public String getStatus() {
        return status;
    }

}