package com.axreng.backend.constants;

public enum CrawlStatus {

    CREATED("created"),ACTIVE("active"), DONE("done"); // TODO Consumir a string

    CrawlStatus(String status) {
    }

    public String getStatus(){
        return this.name();
    }
}
