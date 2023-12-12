package com.axreng.backend.constants;

public enum CrawlStatus {

    CREATED("created"),ACTIVE("active"), DONE("done"); // TODO Consumir a string

    private String statusDescription;

    CrawlStatus(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getStatusDescription(){
        return this.statusDescription;
    }
}
