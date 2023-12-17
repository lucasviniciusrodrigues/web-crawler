package com.axreng.backend.usecase;

import com.axreng.backend.client.CrawlerWebClient;
import com.axreng.backend.config.AppConfig;
import com.axreng.backend.constants.CrawlStatus;
import com.axreng.backend.entity.CrawlerCompareEntity;
import com.axreng.backend.domain.CrawlerIndexDomain;
import com.axreng.backend.exception.UnreacheableSourceException;
import com.axreng.backend.domain.CrawlerDomain;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static com.axreng.backend.constants.Constants.BASE_URL_ENVIRONMENT;
import static com.axreng.backend.constants.Constants.RETRY_DELAY_SECONDS_PROPERTIES;
import static com.axreng.backend.constants.Constants.RETRY_MAX_ATTEMPTS_PROPERTIES;
import static com.axreng.backend.utils.Utils.getEnvironmentVariable;

public class CrawlerUseCase {

    private final int MAX_RETRY_ATTEMPTS;
    private final int RETRY_DELAY_SECONDS;
    private final String BASE_URL;
    private static final Logger log = Logger.getLogger(CrawlerUseCase.class.getName());
    private static final Map<String, Set<String>> mappedAnchors = new HashMap<>();
    private CrawlerIndexDomain crawlerIndexDomain;
    private CrawlerWebClient crawlerlWebClient;


    public CrawlerUseCase(CrawlerIndexDomain crawlerIdDomain, CrawlerWebClient webClient) {
        this.crawlerIndexDomain = crawlerIdDomain;
        this.crawlerlWebClient = webClient;
        MAX_RETRY_ATTEMPTS = Integer.parseInt(AppConfig.getProperty(RETRY_MAX_ATTEMPTS_PROPERTIES));
        RETRY_DELAY_SECONDS = Integer.parseInt(AppConfig.getProperty(RETRY_DELAY_SECONDS_PROPERTIES));
        BASE_URL = getEnvironmentVariable(BASE_URL_ENVIRONMENT);
     }
    public String put(String keyword) {

        if (isValidKeyword(keyword)) {
            throw new IllegalArgumentException();
        }

        keyword = keyword.toLowerCase(Locale.ROOT);
        CrawlerDomain domain = crawlerIndexDomain.generateUniqueID(keyword);

        if(crawlerIndexDomain.haveStatus(keyword, CrawlStatus.CREATED)){
            crawlerIndexDomain.updateStatus(keyword, CrawlStatus.ACTIVE);
            countKeyword(keyword, domain);
        }

         return domain.getId();
    }

    private void countKeyword(String keyword, CrawlerDomain domain) {

        Set<String> nonDuplicatedSources = new HashSet<>(List.of());

        runCrawler(keyword, null, BASE_URL, nonDuplicatedSources, 0, domain);

    }

    private void runCrawler(String keyword, String source, String baseUrl, Set<String> nonDuplicatedSources, int attempts, CrawlerDomain domain) {

        domain.incrementRunningThreads();

        if (source == null)
            source = baseUrl;

        String finalSource = source;
        AtomicReference<CrawlerCompareEntity> result = new AtomicReference<>();

        CompletableFuture.runAsync(() -> {

            try {
                result.set(crawlerlWebClient.crawlWebPage(finalSource, baseUrl, keyword, mappedAnchors));

                this.updateSearchedWords(keyword, result.get().getContainsKey());
                this.updateMappedAnchors(finalSource, result.get().getAnchors());

                for (String path : mappedAnchors.get(finalSource)) {
                    if (!nonDuplicatedSources.contains(path)) {
                        nonDuplicatedSources.add(path);
                        runCrawler(keyword, path, baseUrl, nonDuplicatedSources, 0, domain);
                    }
                }
            }
            catch (UnreacheableSourceException e){
                retryCrawler(keyword, finalSource, baseUrl, nonDuplicatedSources, attempts + 1, domain);
            }
            catch (Exception e){
                log.severe(e.getMessage());
            }
            finally {
                domain.decrementRunningThreads();
            }

        }, domain.getExecutorService());

    }

    private void retryCrawler(String keyword, String source, String baseUrl, Set<String> nonDuplicatedSources, int retryAttempts, CrawlerDomain domain) {
        if (retryAttempts <= MAX_RETRY_ATTEMPTS) {
            log.info("Retrying crawler for " + keyword + " at " + source + " (Attempt " + retryAttempts + ")");

            domain.getRetryExecutor().schedule(() -> runCrawler(keyword, source, baseUrl, nonDuplicatedSources, retryAttempts + 1, domain),
                    RETRY_DELAY_SECONDS, TimeUnit.SECONDS);

        } else {
            log.severe("Max retry attempts reached for " + keyword + " at " + source);
        }
    }

    private void updateSearchedWords(String keyword, Set<String> containsKey) {
        crawlerIndexDomain.getSearchedKeywords().get(keyword).getUrls().addAll(containsKey);
    }

    private void updateMappedAnchors(String urlString, Set<String> newAnchors) {

        if (!mappedAnchors.containsKey(urlString)) {
            mappedAnchors.put(urlString, new HashSet<>());
        }

        mappedAnchors.get(urlString).addAll(newAnchors);

    }

    public CrawlerDomain getResult(String id) {
        String keyword  = crawlerIndexDomain.getGeneratedIds().get(id);
        return crawlerIndexDomain.getSearchedKeywords().get(keyword);
    }

    public boolean isQueued(String id) {
        return crawlerIndexDomain.getGeneratedIds().containsKey(id);
    }

    public boolean isValidKeyword(String value) {
        if(value == null || value.length() < 4 || value.length() > 32 || value.isBlank())
            return false;

        return true;
    }
}
