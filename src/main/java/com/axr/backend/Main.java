package com.axr.backend;

import com.axr.backend.client.CrawlerWebClient;
import com.axr.backend.controller.CrawlerController;
import com.axr.backend.domain.CrawlerIdGeneratorDomain;
import com.axr.backend.domain.CrawlerIndexDomain;
import com.axr.backend.mapper.XmlMapper;
import com.axr.backend.service.CrawlerService;
import com.axr.backend.usecase.CrawlerUseCase;

import java.security.SecureRandom;

public class Main {

    public static void main(String[] args) {

        CrawlerUseCase crawlerDomain = null;

        crawlerDomain = new CrawlerUseCase(
                CrawlerIndexDomain.getInstance(CrawlerIdGeneratorDomain.getInstance(new SecureRandom())), new CrawlerWebClient(new XmlMapper()));

        CrawlerService crawlerService = new CrawlerService(crawlerDomain);

        new CrawlerController(crawlerService).setupRoutes();
    }
}
