package com.axreng.backend.usecase;

import com.axreng.backend.client.CrawlerWebClient;
import com.axreng.backend.constants.CrawlStatus;
import com.axreng.backend.domain.CrawlerDomain;
import com.axreng.backend.domain.CrawlerIndexDomain;
import com.axreng.backend.exception.UnreacheableSourceException;
import com.axreng.backend.mapper.XmlMapper;
import com.axreng.backend.model.SearchCrawlerDetailResponse;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static com.axreng.backend.constants.Constants.BASE_URL_ENVIRONMENT;
import static com.axreng.backend.constants.Constants.KEYWORD_ERROR_MESSAGE;
import static com.axreng.backend.utils.Utils.getEnvironmentVariable;

public class CrawlerUseCase {

    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final int RETRY_DELAY_SECONDS = 20;
    private final ExecutorService executorService = Executors.newCachedThreadPool(); // TODO Shutdown in the end
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1);
    private static final Logger log = Logger.getLogger(CrawlerUseCase.class.getName());
    private CrawlerIndexDomain crawlerIndexDomain;
    private CrawlerWebClient crawlerlWebClient;
    private static final Map<String, Set<String>> mappedAnchors = new HashMap<>();


    public CrawlerUseCase(CrawlerIndexDomain crawlerIdDomain, CrawlerWebClient webClient) {
        this.crawlerIndexDomain = crawlerIdDomain;
        this.crawlerlWebClient = webClient;
     }
    public String put(String keyword) {

        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalArgumentException(KEYWORD_ERROR_MESSAGE);
        }

        keyword = keyword.toLowerCase(Locale.ROOT);
        String id = crawlerIndexDomain.generateUniqueID(keyword);

        try {
            if(crawlerIndexDomain.haveStatus(keyword, CrawlStatus.CREATED)){
                crawlerIndexDomain.updateStatus(keyword, CrawlStatus.ACTIVE);
                countKeyword(keyword);
            }
        } catch (Exception e) { // TODO tratar como?
            log.warning("Error crawling for " + keyword + ": " + e.getMessage());
        }

         return id;
    }

    private void countKeywordOld(String keyword) {
        String baseUrl = getEnvironmentVariable(BASE_URL_ENVIRONMENT);

        List<String> sources = new ArrayList<>(List.of(baseUrl));
        Set<String> nonDuplicatedSources = new HashSet<>(List.of());

        for(int i = 0; i < sources.size(); i++) {

            try {

                CrawlerDomain result = crawlerlWebClient.crawlWebPage(sources.get(i), baseUrl, keyword, mappedAnchors);
//                this.updateSources(sources.get(i), nonDuplicatedSources, sources, result.getAnchors());
                this.updateSearchedWords(keyword, result.getContainsKey());

            } catch (Exception e){
                log.warning("Error searching for " + keyword +" data from: " + sources.get(i));
                break;
            }
        }

        crawlerIndexDomain.updateStatus(keyword, CrawlStatus.DONE);
    }

    private void countKeyword(String keyword) {

        String baseUrl = getEnvironmentVariable(BASE_URL_ENVIRONMENT);

        Set<String> nonDuplicatedSources = new HashSet<>(List.of());

        runCrawler(keyword, null, baseUrl, nonDuplicatedSources, 0);

    }

    private void runCrawler(String keyword, String source, String baseUrl, Set<String> nonDuplicatedSources, int attempts) {

//        log.info(keyword + " no site: " + source);

        if (source == null || source.isBlank())
            source = baseUrl;

        String finalSource = source;

        AtomicReference<CrawlerDomain> result = new AtomicReference<>();
        CompletableFuture.runAsync(() -> {

            try {
                result.set(crawlerlWebClient.crawlWebPage(finalSource, baseUrl, keyword, mappedAnchors));

                this.updateSearchedWords(keyword, result.get().getContainsKey());
                this.updateMappedAnchors(finalSource, result.get().getAnchors());

                for (String path : mappedAnchors.get(finalSource)) {
                    if (!nonDuplicatedSources.contains(path)) {
                        nonDuplicatedSources.add(path);
                        runCrawler(keyword, path, baseUrl, nonDuplicatedSources, 0);
                    }
                }

            }
            catch (UnreacheableSourceException e){
                retryCrawler(keyword, finalSource, baseUrl, nonDuplicatedSources, attempts);
            }
            catch (Exception e){ // TODO tratamento
                log.warning("Error searching for " + keyword +" data from: " + finalSource);
            }

        }, executorService);
    }

    private void retryCrawler(String keyword, String source, String baseUrl, Set<String> nonDuplicatedSources, int retryAttempts) {
        if (retryAttempts <= MAX_RETRY_ATTEMPTS) {
            log.info("Retrying crawler for " + keyword + " at " + source + " (Attempt " + retryAttempts + ")");
            retryExecutor.schedule(() -> runCrawler(keyword, source, baseUrl, nonDuplicatedSources, retryAttempts + 1),
                    RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
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

    public SearchCrawlerDetailResponse getResult(String id) {
        String keyword  = crawlerIndexDomain.getGeneratedIds().get(id);
        return crawlerIndexDomain.getSearchedKeywords().get(keyword);
    }

    public boolean isQueued(String id) {
        return crawlerIndexDomain.getGeneratedIds().containsKey(id);
    }
}
