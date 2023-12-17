package com.axreng.backend.domain;

import java.util.HashSet;
import java.util.Set;

public class CrawlerCompareDomain {

    private Set<String> anchors = new HashSet<>();
    private Set<String> containsKey = new HashSet<>();

    public CrawlerCompareDomain(Set<String> anchors, Set<String> containsKey) {
        this.anchors = anchors;
        this.containsKey = containsKey;
    }

    public CrawlerCompareDomain() {}

    public Set<String> getAnchors() {
        return anchors;
    }

    public Set<String> getContainsKey() {
        return containsKey;
    }
}
