package com.bu.project.dto;


import java.util.List;

public record RecommendationDto(List<Item> items) {
    public record Item(String title, String reason) {}
}