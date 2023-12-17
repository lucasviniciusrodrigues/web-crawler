package com.axreng.backend.client;

import com.axreng.backend.domain.CrawlerCompareDomain;
import com.axreng.backend.exception.UnreacheableSourceException;
import com.axreng.backend.mapper.XmlMapper;
import com.axreng.backend.service.CrawlerService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.ConnectException;
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
    public CrawlerCompareDomain crawlWebPage(String urlString, String baseUrl, String keyword, Map<String, Set<String>> mappedSources) throws Exception {
        try {

            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;

                Set<String> anchors = new HashSet<>();
                Set<String> containsList = new HashSet<>();

                while ((line = reader.readLine()) != null) {
                    line = line.toLowerCase(Locale.ROOT);
                    content.append(line).append("\n");

                    if(mappedSources.get(urlString) == null) {
                        anchors.addAll(xmlMapper.mapAnchorsWithSameBaseUrl(line, baseUrl));
                    }

                    if(!containsList.contains(urlString) && line.contains(keyword)) {
                        containsList.add(urlString);
                    }
                }

                return new CrawlerCompareDomain(anchors, containsList);
            }

        }
        catch (FileNotFoundException e){
            log.warning("Not found source: " + urlString);
            return new CrawlerCompareDomain();
        }
        catch (ConnectException e){
            log.warning("Source to retry: " + urlString);
            throw new UnreacheableSourceException(urlString);
        }
        catch (Exception e) {
            log.warning("Error crawling for " + keyword + " at " + urlString + " : " + e.getMessage());
            throw e;
        }
    }


}
