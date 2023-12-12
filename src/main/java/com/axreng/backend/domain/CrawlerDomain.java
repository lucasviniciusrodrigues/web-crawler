package com.axreng.backend.domain;

import java.util.Set;

public class CrawlerDomain {

    private Set<String> anchors;
    private Set<String> containsKey;

    public CrawlerDomain(Set<String> anchors, Set<String> containsKey) {
        this.anchors = anchors;
        this.containsKey = containsKey;
    }

    public Set<String> getAnchors() {
        return anchors;
    }

    public Set<String> getContainsKey() {
        return containsKey;
    }
}
