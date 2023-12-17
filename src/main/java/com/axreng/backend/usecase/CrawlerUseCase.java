package com.axreng.backend.usecase;

import com.axreng.backend.client.CrawlerWebClient;
import com.axreng.backend.constants.CrawlStatus;
import com.axreng.backend.domain.CrawlerCompareDomain;
import com.axreng.backend.domain.CrawlerIndexDomain;
import com.axreng.backend.exception.UnreacheableSourceException;
import com.axreng.backend.domain.CrawlerDetailDomain;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static com.axreng.backend.constants.Constants.BASE_URL_ENVIRONMENT;
import static com.axreng.backend.constants.Constants.KEYWORD_ERROR_MESSAGE;
import static com.axreng.backend.constants.Constants.RETRY_DELAY_SECONDS_PROPERTIES;
import static com.axreng.backend.constants.Constants.RETRY_MAX_ATTEMPTS_PROPERTIES;
import static com.axreng.backend.utils.Utils.getEnvironmentVariable;

public class CrawlerUseCase {

    private final int MAX_RETRY_ATTEMPTS = 3;
    private final int RETRY_DELAY_SECONDS = 50;
    private static final Logger log = Logger.getLogger(CrawlerUseCase.class.getName());
    private static final Map<String, Set<String>> mappedAnchors = new HashMap<>();
    private CrawlerIndexDomain crawlerIndexDomain;
    private CrawlerWebClient crawlerlWebClient;


    public CrawlerUseCase(CrawlerIndexDomain crawlerIdDomain, CrawlerWebClient webClient) {
        this.crawlerIndexDomain = crawlerIdDomain;
        this.crawlerlWebClient = webClient;
//        MAX_RETRY_ATTEMPTS = Integer.parseInt(getEnvironmentVariable(RETRY_MAX_ATTEMPTS_PROPERTIES)); TODO
//        RETRY_DELAY_SECONDS = Integer.parseInt(getEnvironmentVariable(RETRY_DELAY_SECONDS_PROPERTIES));
     }
    public String put(String keyword) {

        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalArgumentException();
        }

        keyword = keyword.toLowerCase(Locale.ROOT);
        CrawlerDetailDomain domain = crawlerIndexDomain.generateUniqueID(keyword);

        try {
            if(crawlerIndexDomain.haveStatus(keyword, CrawlStatus.CREATED)){
                crawlerIndexDomain.updateStatus(keyword, CrawlStatus.ACTIVE);
                countKeyword(keyword, domain);
            }
        } catch (Exception e) { // TODO tratar como?
            log.warning("Error crawling for " + keyword + ": " + e.getMessage());
        }

         return domain.getId();
    }

    private void countKeyword(String keyword, CrawlerDetailDomain domain) {

        String baseUrl = getEnvironmentVariable(BASE_URL_ENVIRONMENT);

        Set<String> nonDuplicatedSources = new HashSet<>(List.of());

        runCrawler(keyword, null, baseUrl, nonDuplicatedSources, 0, domain);

    }

    private void runCrawler(String keyword, String source, String baseUrl, Set<String> nonDuplicatedSources, int attempts, CrawlerDetailDomain domain) {

        domain.incrementRunningThreads();

        if (source == null || source.isBlank())
            source = baseUrl;

        String finalSource = source;

        AtomicReference<CrawlerCompareDomain> result = new AtomicReference<>();
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
                retryCrawler(keyword, finalSource, baseUrl, nonDuplicatedSources, attempts, domain);
            }
            catch (Exception e){ // TODO tratamento
                log.warning("Error searching for " + keyword +" data from: " + finalSource);
            }
            finally {
                domain.decrementRunningThreads();
            }

        }, domain.getExecutorService());

    }

    private void retryCrawler(String keyword, String source, String baseUrl, Set<String> nonDuplicatedSources, int retryAttempts, CrawlerDetailDomain domain) {
        if (retryAttempts <= MAX_RETRY_ATTEMPTS) {
            log.info("Retrying crawler for " + keyword + " at " + source + " (Attempt " + retryAttempts + ")");

            domain.getRetryExecutor().schedule(() -> runCrawler(keyword, source, baseUrl, nonDuplicatedSources, retryAttempts + 1, domain),
                    RETRY_DELAY_SECONDS, TimeUnit.SECONDS);

//            domain.retryIsDone();
        } else {
            log.warning("Max retry attempts reached for " + keyword + " at " + source);
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

    public CrawlerDetailDomain getResult(String id) {
        String keyword  = crawlerIndexDomain.getGeneratedIds().get(id);
        return crawlerIndexDomain.getSearchedKeywords().get(keyword);
    }

    public boolean isQueued(String id) {
        return crawlerIndexDomain.getGeneratedIds().containsKey(id);
    }
}
