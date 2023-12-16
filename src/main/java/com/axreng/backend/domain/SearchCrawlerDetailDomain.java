package com.axreng.backend.domain;

import com.axreng.backend.constants.CrawlStatus;
import com.axreng.backend.model.SearchCrawlerResponse;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class SearchCrawlerDetailDomain extends SearchCrawlerResponse {

    private static final Logger log = Logger.getLogger(SearchCrawlerDetailDomain.class.getName());

    public SearchCrawlerDetailDomain(String generatedId) {
        super(generatedId);
    }

    public SearchCrawlerDetailDomain(String generatedId, String status) {
        super(generatedId);
        this.status = status;
    }
    private String status;
    private Set<String> urls = new HashSet<>();

    public final ExecutorService executorService = Executors.newCachedThreadPool();

    public SearchCrawlerDetailDomain() {}
    public Set<String> getUrls() {
        return urls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void endProcess() {

        try {
            if (executorService.isTerminated()) {
                this.status = CrawlStatus.DONE.getStatusDescription();
                executorService.shutdownNow();
            }
        } catch (Exception e) {
            log.warning("Thread shutdown with error: " + e.getMessage());
        }
    }
}
