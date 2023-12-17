package com.axreng.backend.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class XmlMapper {

    public List<String> mapAnchorsWithSameBaseUrl(String xmlLine, String baseUrl) {
        List<String> anchorList = new ArrayList<>();

        try {
            int startIndex = xmlLine.indexOf("<a");
            while (startIndex != -1) {
                int endIndex = xmlLine.indexOf(">", startIndex + 2);
                if (endIndex != -1) {
                    String anchorTag = xmlLine.substring(startIndex, endIndex + 1);
                    processAnchorTag(anchorTag, baseUrl, anchorList);
                }

                startIndex = xmlLine.indexOf("<a", startIndex + 2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return anchorList;
    }

    private void processAnchorTag(String anchorTag, String baseUrl, List<String> anchorList) {
        int hrefIndex = anchorTag.indexOf("href=");
        if (hrefIndex != -1) {
            int startQuote = anchorTag.indexOf("\"", hrefIndex + 5);
            int endQuote = anchorTag.indexOf("\"", startQuote + 1);

            if (startQuote != -1 && endQuote != -1) {
                String hrefValue = anchorTag.substring(startQuote + 1, endQuote);
                if(hrefValue.startsWith("mailto:") || hrefValue.startsWith("ftp:"))
                    return;

                if ((hrefValue.startsWith("http://") || hrefValue.startsWith("https://"))) {
                    if (hrefValue.startsWith(baseUrl)) {
                        anchorList.add(hrefValue);
                    }
                } else {
                    hrefValue = hrefValue.replace("../", "");
                    anchorList.add(baseUrl + hrefValue);
                }
            }
        }
    }

}

