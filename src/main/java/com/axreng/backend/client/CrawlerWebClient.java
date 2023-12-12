package com.axreng.backend.client;

import com.axreng.backend.domain.CrawlerDomain;
import com.axreng.backend.exception.UnreacheableSourceException;
import com.axreng.backend.mapper.XmlMapper;
import com.axreng.backend.service.CrawlerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Logger;

public class CrawlerWebClient {

    private static final Logger log = Logger.getLogger(CrawlerService.class.getName());

    private XmlMapper xmlMapper;

    public CrawlerWebClient(XmlMapper xmlMapper){
        this.xmlMapper = xmlMapper;
    }
    public CrawlerDomain crawlWebPage(String urlString, String baseUrl, String keyword, HashMap<String, Set<String>> mappedSources) throws UnreacheableSourceException, IOException {
        try {

            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;

                Set<String> aux = new HashSet<>();
                Set<String> containsList = new HashSet<>();

                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");

                    if(mappedSources.get(urlString) == null) {
                        aux.addAll(xmlMapper.mapAnchorsWithSameBaseUrl(line, baseUrl));
                    }

                    if(!containsList.contains(urlString) && line.contains(keyword)){
                        containsList.add(urlString);
                    }
                }

                return new CrawlerDomain(aux, containsList);
            }

        } catch (Exception e) {
            log.warning("Error retrieving data from " + urlString + " : " + e.getMessage());
            throw e;
        }
    }


}
