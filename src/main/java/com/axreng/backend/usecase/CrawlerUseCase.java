package com.axreng.backend.usecase;

import com.axreng.backend.client.CrawlerWebClient;
import com.axreng.backend.constants.CrawlStatus;
import com.axreng.backend.domain.CrawlerDomain;
import com.axreng.backend.domain.CrawlerIndexDomain;
import com.axreng.backend.mapper.XmlMapper;
import com.axreng.backend.domain.SearchCrawlerDetailDomain;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static com.axreng.backend.constants.Constants.BASE_URL_ENVIRONMENT;
import static com.axreng.backend.constants.Constants.KEYWORD_ERROR_MESSAGE;
import static com.axreng.backend.utils.Utils.getEnvironmentVariable;

public class CrawlerUseCase {

    private XmlMapper xmlMapper;
    private CrawlerIndexDomain crawlerIndexDomain;
    private CrawlerWebClient crawlerlWebClient;
    private static final HashMap<String, Set<String>> mappedAnchors = new HashMap();
    private static final Logger log = Logger.getLogger(CrawlerUseCase.class.getName());



    public CrawlerUseCase(CrawlerIndexDomain crawlerIdDomain, CrawlerWebClient webClient) {
        this.crawlerIndexDomain = crawlerIdDomain;
        this.crawlerlWebClient = webClient;
     }
    public String put(String keyword) {

        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalArgumentException(KEYWORD_ERROR_MESSAGE);
        }

        keyword = keyword.toLowerCase(Locale.ROOT);
        SearchCrawlerDetailDomain crawlerResponse = crawlerIndexDomain.generateUniqueID(keyword);
        String id = crawlerResponse.getId();

        try {
            if(crawlerIndexDomain.haveStatus(keyword, CrawlStatus.CREATED)){
                crawlerIndexDomain.updateStatus(keyword, CrawlStatus.ACTIVE);
                countKeyword(keyword, crawlerResponse);
            }
        } catch (Exception e) { // TODO tratar como?
            log.warning(e.getMessage());
        }

         return id;

    }

    private void countKeyword(String keyword, SearchCrawlerDetailDomain domain) {

        String baseUrl = getEnvironmentVariable(BASE_URL_ENVIRONMENT);

        Set<String> nonDuplicatedSources = new HashSet<>(List.of(baseUrl));

        runCrawler(keyword, null, baseUrl, nonDuplicatedSources, domain);

    }

    private CrawlerDomain runCrawler(String keyword, String source, String baseUrl, Set<String> nonDuplicatedSources, SearchCrawlerDetailDomain domain) {

        if (source == null || source.isBlank())
            source = baseUrl;

        String finalSource = source;

        AtomicReference<CrawlerDomain> result = new AtomicReference<CrawlerDomain>();
        CompletableFuture.runAsync(() -> {
            try {

                result.set(crawlerlWebClient.crawlWebPage(finalSource, baseUrl, keyword, mappedAnchors));
                this.updateSearchedWords(keyword, result.get().getContainsKey());

                if(mappedAnchors.get(finalSource) == null) {
                    mappedAnchors.put(finalSource, result.get().getAnchors());
                }

                for(String path : mappedAnchors.get(finalSource)) {
                    if(!nonDuplicatedSources.contains(path)){
                        nonDuplicatedSources.add(path);
                        runCrawler(keyword, path, baseUrl, nonDuplicatedSources, domain);
                    }
                }

            } catch (Exception e){
                log.warning("Error searching for " + keyword +" data from: " + finalSource);
            }
        }, domain.executorService);

        domain.endProcess();

        return result.get();
    }

    private void updateSearchedWords(String keyword, Set<String> containsKey) {
        crawlerIndexDomain.getSearchedKeywords().get(keyword).getUrls().addAll(containsKey);
    }

    private void updateSources(String urlString, Set<String> sourcesNonDuplicated, Set<String> aux) {


        if(mappedAnchors.get(urlString) == null) {
            mappedAnchors.put(urlString, aux);
        }

        for(String path : mappedAnchors.get(urlString)) {
            if(!sourcesNonDuplicated.contains(path)){
                sourcesNonDuplicated.add(path);
            }
        }
    }

    public SearchCrawlerDetailDomain getResult(String id) {
        String keyword  = crawlerIndexDomain.getGeneratedIds().get(id);
        return crawlerIndexDomain.getSearchedKeywords().get(keyword);
    }

    public boolean isQueued(String id) {
        return crawlerIndexDomain.getGeneratedIds().containsKey(id);
    }

}
