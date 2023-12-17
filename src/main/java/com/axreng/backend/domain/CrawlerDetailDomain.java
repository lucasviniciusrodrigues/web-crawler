package com.axreng.backend.domain;

import com.axreng.backend.constants.CrawlStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class CrawlerDetailDomain extends CrawlerIdDomain {

    private static final Logger log = Logger.getLogger(CrawlerDetailDomain.class.getName());


    private final AtomicInteger runningThreads = new AtomicInteger(0);

    public CrawlerDetailDomain(String generatedId) {
        super(generatedId);
    }

    public CrawlerDetailDomain(String generatedId, String status) {
        super(generatedId);
        this.status = status;
    }
    private String status;
    private Set<String> urls = new HashSet<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1);

    public CrawlerDetailDomain() {}
    public Set<String> getUrls() {
        return urls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public ScheduledExecutorService getRetryExecutor() {
        return retryExecutor;
    }
    public void incrementRunningThreads() {
        runningThreads.incrementAndGet();
    }

    public void decrementRunningThreads() {
        if (runningThreads.decrementAndGet() == 0) {
            isDone();
        }
    }



    public void isDone() {

        try {

            if (runningThreads.get() == 0) {
                this.status = CrawlStatus.DONE.getStatusDescription();
                executorService.shutdown();
                retryExecutor.shutdown();
                log.info("Crawl finished: " + getId());
            }

        } catch (Exception e) {
            log.warning("Crawl " + getId() + " Thread shutdown with error: " + e.getMessage());
        }
    }
}