package com.axreng.backend.usecase;

import com.axreng.backend.client.CrawlerWebClient;
import com.axreng.backend.constants.CrawlStatus;
import com.axreng.backend.domain.CrawlerDomain;
import com.axreng.backend.domain.CrawlerIndexDomain;
import com.axreng.backend.mapper.XmlMapper;
import com.axreng.backend.model.SearchCrawlerDetailResponse;

import java.util.*;
import java.util.logging.Logger;

import static com.axreng.backend.constants.Constants.BASE_URL_ENVIRONMENT;
import static com.axreng.backend.constants.Constants.KEYWORD_ERROR_MESSAGE;
import static com.axreng.backend.utils.Utils.getEnvironmentVariable;

public class CrawlerUseCase {

    private static final Logger log = Logger.getLogger(CrawlerUseCase.class.getName());
    private static final HashMap<String, Set<String>> mappedAnchors = new HashMap();
    private CrawlerIndexDomain crawlerIndexDomain;
    private XmlMapper xmlMapper;
    private CrawlerWebClient crawlerlWebClient;

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
                countKeyword(keyword);;
            }
        } catch (Exception e) { // TODO tratar como?
            log.warning(e.getMessage());
        }

         return id;

    }

    private void countKeyword(String keyword) throws Exception {
        String baseUrl = getEnvironmentVariable(BASE_URL_ENVIRONMENT);

        List<String> sources = new ArrayList<>(List.of(baseUrl));
        Set<String> nonDuplicatedSources = new HashSet<>(List.of());

        for(int i = 0; i < sources.size(); i++) {

            try {

                if(i == 14)
                    System.out.println("");

                CrawlerDomain result = crawlerlWebClient.crawlWebPage(sources.get(i), baseUrl, keyword, mappedAnchors);
                this.updateSources(sources.get(i), nonDuplicatedSources, sources, result.getAnchors());
                this.updateSearchedWords(keyword, result.getContainsKey());

            } catch (Exception e){
                log.warning("Error searching for " + keyword +" data from: " + sources.get(i));
                continue;
            }
        }

        crawlerIndexDomain.updateStatus(keyword, CrawlStatus.DONE);
    }

    private void updateSearchedWords(String keyword, Set<String> containsKey) {
        crawlerIndexDomain.getSearchedKeywords().get(keyword).getUrls().addAll(containsKey);
    }

    private void updateSources(String urlString, Set<String> sourcesNonDuplicated, List<String> referenceSources, Set<String> aux) {


        if(mappedAnchors.get(urlString) == null) {
            mappedAnchors.put(urlString, aux);
        }

        for(String path : mappedAnchors.get(urlString)) {
            if(!sourcesNonDuplicated.contains(path)){
                sourcesNonDuplicated.add(path);
                referenceSources.add(path);
            }
        }
    }

    public SearchCrawlerDetailResponse getResult(String id) {
        String keyword  = crawlerIndexDomain.getGeneratedIds().get(id);
        return crawlerIndexDomain.getSearchedKeywords().get(keyword);
    }

    public boolean isQueued(String id) {
        return crawlerIndexDomain.getGeneratedIds().containsKey(id);
    }
}
