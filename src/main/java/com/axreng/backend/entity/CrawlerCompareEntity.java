package com.axreng.backend.entity;

import java.util.HashSet;
import java.util.Set;

public class CrawlerCompareEntity {

    private Set<String> anchors = new HashSet<>();
    private Set<String> containsKey = new HashSet<>();

    public CrawlerCompareEntity(Set<String> anchors, Set<String> containsKey) {
        this.anchors = anchors;
        this.containsKey = containsKey;
    }

    public CrawlerCompareEntity() {}

    public Set<String> getAnchors() {
        return anchors;
    }

    public Set<String> getContainsKey() {
        return containsKey;
    }
}
