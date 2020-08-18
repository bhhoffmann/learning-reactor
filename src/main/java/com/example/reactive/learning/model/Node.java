package com.example.reactive.learning.model;

import java.util.List;

public class Node {

    private String id;
    private List<String> children;

    public Node(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}
