package com.axreng.backend.domain;

import com.axreng.backend.constants.CrawlStatus;
import com.axreng.backend.model.SearchCrawlerDetailResponse;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.axreng.backend.constants.CrawlStatus.CREATED;

public class CrawlerIndexDomain {

    private static final Logger log = Logger.getLogger(CrawlerIndexDomain.class.getName());
    private static CrawlerIndexDomain instance;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 8;
    SecureRandom random;

    private CrawlerIndexDomain(SecureRandom secureRandom) {
        random = secureRandom;
//        random.getProvider().entrySet().add(Map.entry("ThreadSafe", true)); // TODO thread safe
    }

    public static synchronized CrawlerIndexDomain getInstance(SecureRandom secureRandom) {
        if (instance == null) {
            instance = new CrawlerIndexDomain(secureRandom);
        }
        return instance;
    }

    private final Map<String, String> generatedIds = new HashMap<>();
    Map<String, SearchCrawlerDetailResponse> searchedKeywords = new HashMap<>();

    public synchronized String generateUniqueID(String keyword) {
        String generatedId;

        if(searchedKeywords.containsKey(keyword)){
            log.info("Key already processed");
            return searchedKeywords.get(keyword).getId();
        }

        do {
            generatedId = generateId(random);
        } while (!isUnique(generatedId));

        generatedIds.put(generatedId, keyword);
        searchedKeywords.put(keyword, new SearchCrawlerDetailResponse(generatedId, CREATED.getStatus()));

        return generatedId;
    }

    private String generateId(SecureRandom random) {
        StringBuilder idBuilder = new StringBuilder();

        for (int i = 0; i < ID_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            idBuilder.append(randomChar);
        }

        return idBuilder.toString();
    }

    private  boolean isUnique(String id) {
        return !generatedIds.containsKey(id);
    }

    public Map<String, String> getGeneratedIds() {
        return generatedIds;
    }

    public Map<String, SearchCrawlerDetailResponse> getSearchedKeywords() {
        return searchedKeywords;
    }

    synchronized public boolean haveStatus(String keyword, CrawlStatus status) {
        return getSearchedKeywords().get(keyword).getStatus().equals(status.getStatus());
    }

    synchronized public void updateStatus(String keyword, CrawlStatus status) {
        getSearchedKeywords().get(keyword).setStatus(status.getStatus());
    }
}
