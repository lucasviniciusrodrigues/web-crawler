package com.axr.backend.domain;

import com.axr.backend.config.AppConfig;
import com.axr.backend.constants.CrawlStatus;
import com.axr.backend.entity.CrawlerBaseEntity;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static com.axr.backend.constants.Constants.THREAD_FIXED_POOL_PROPERTIES;

public class CrawlerDomain extends CrawlerBaseEntity {

    private static final Logger log = Logger.getLogger(CrawlerDomain.class.getName());
    private final ExecutorService executorService;
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1);
    private final AtomicInteger runningThreads = new AtomicInteger(0);
    private Set<String> urls = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private String status;

    public CrawlerDomain(String generatedId, String status) {
        super(generatedId);
        this.status = status;
        executorService = Executors.newFixedThreadPool(
                Integer.parseInt(AppConfig.getProperty(THREAD_FIXED_POOL_PROPERTIES)));
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
                executorService.shutdownNow();
                retryExecutor.shutdownNow();
                log.info("Crawl finished: " + getId());
            }

        } catch (Exception e) {
            log.severe("Crawl " + getId() + " Thread shutdown with error: " + e.getMessage());
        }
    }
}