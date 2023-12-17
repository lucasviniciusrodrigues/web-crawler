package com.axreng.backend.entity;

public class CrawlerBaseEntity {

    String id;

    public CrawlerBaseEntity(String id) {
        this.id = id;
    }
    public CrawlerBaseEntity() {}

    public String getId() {
        return id;
    }

}
