package com.axreng.backend.domain;

import com.axreng.backend.model.SearchCrawlerDetailResponse;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.axreng.backend.constants.CrawlStatus.CREATED;

public class CrawlerIdGeneratorDomain {

    private static final Logger log = Logger.getLogger(CrawlerIdGeneratorDomain.class.getName());
    private static CrawlerIdGeneratorDomain instance;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 8;
    SecureRandom random;
    private final Map<String, String> generatedIds = new HashMap<>();

    private CrawlerIdGeneratorDomain(SecureRandom secureRandom) {
        random = secureRandom;
    }

    public static synchronized CrawlerIdGeneratorDomain getInstance(SecureRandom secureRandom) {
        if (instance == null) {
            instance = new CrawlerIdGeneratorDomain(secureRandom);
        }
        return instance;
    }

    public String generateUniqueID(String keyword, Map<String, SearchCrawlerDetailResponse> index) {
        String generatedId;

        if(index.containsKey(keyword)){
            log.info("Key already processed");
            return index.get(keyword).getId();
        }

        do {
            generatedId = generateId(random);
        } while (!isUnique(generatedId));

        generatedIds.put(generatedId, keyword);
        index.put(keyword, new SearchCrawlerDetailResponse(generatedId, CREATED.getStatusDescription()));

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

}
