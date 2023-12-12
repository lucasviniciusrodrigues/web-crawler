package com.axreng.backend;

import com.axreng.backend.client.CrawlerWebClient;
import com.axreng.backend.controller.CrawlerController;
import com.axreng.backend.usecase.CrawlerUseCase;
import com.axreng.backend.domain.CrawlerIndexDomain;
import com.axreng.backend.mapper.XmlMapper;
import com.axreng.backend.service.CrawlerService;

import java.security.SecureRandom;

public class Main {// TODO Logs
    public static void main(String[] args) {

        CrawlerUseCase crawlerDomain = null;

        crawlerDomain = new CrawlerUseCase(
                CrawlerIndexDomain.getInstance(new SecureRandom()), new CrawlerWebClient(new XmlMapper()));

        CrawlerService crawlerService = new CrawlerService(crawlerDomain);

        new CrawlerController(crawlerService).setupRoutes();
    }
}