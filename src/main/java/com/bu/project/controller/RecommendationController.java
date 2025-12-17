package com.bu.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bu.project.dto.WeatherDto;
import com.bu.project.service.RecommendationService;
import com.bu.project.service.WeatherService;

@RestController
public class RecommendationController {

    private final WeatherService weatherService;
    private final RecommendationService recommendationService;

    // 생성자 주입
    public RecommendationController(WeatherService weatherService, RecommendationService recommendationService) {
        this.weatherService = weatherService;
        this.recommendationService = recommendationService;
    }

    // 추천 문장 반환 API
    @GetMapping("/recommend")
    public String getRecommendation(
        @RequestParam("lat") double lat,
        @RequestParam("lon") double lon) {
        
        WeatherDto weatherData = weatherService.getWeatherData(lat, lon);

        String recommendation = recommendationService.getRecommendation(weatherData);

        return recommendation;
    }
}