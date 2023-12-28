package com.axr.backend.client;

import com.axr.backend.entity.CrawlerCompareEntity;
import com.axr.backend.exception.UnreacheableSourceException;
import com.axr.backend.mapper.XmlMapper;
import com.axr.backend.service.CrawlerService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class CrawlerWebClient {

    private static final Logger log = Logger.getLogger(CrawlerService.class.getName());

    private XmlMapper xmlMapper;

    public CrawlerWebClient(XmlMapper xmlMapper){
        this.xmlMapper = xmlMapper;
    }
    public CrawlerCompareEntity crawlWebPage(String urlString, String baseUrl, String keyword, Map<String, Set<String>> mappedSources) throws Exception {
        try {

            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                String line;

                Set<String> anchors = new HashSet<>();
                Set<String> containsList = new HashSet<>();

                while ((line = reader.readLine()) != null) {

                    if(mappedSources.get(urlString) == null) {
                        anchors.addAll(xmlMapper.mapAnchorsWithSameBaseUrl(line, baseUrl));
                    }

                    line = line.toLowerCase(Locale.ROOT);
                    if(!containsList.contains(urlString) && line.contains(keyword)) {
                        containsList.add(urlString);
                    }
                }

                return new CrawlerCompareEntity(anchors, containsList);
            }

        }
        catch (FileNotFoundException e){
            log.warning("Not found source: " + urlString);
            return new CrawlerCompareEntity();
        }
        catch (Exception e){
            log.warning("Source to retry: " + urlString);
            throw new UnreacheableSourceException(urlString);
        }
    }


}
