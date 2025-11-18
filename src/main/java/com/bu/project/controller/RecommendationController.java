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

    // 새로운 추천 API 엔드포인트: /recommend
    // 💡 파라미터를 String city에서 double lat, double lon으로 변경했습니다.
    @GetMapping("/recommend")
    public String getRecommendation(
        @RequestParam("lat") double lat, // 💡 위도 파라미터 추가
        @RequestParam("lon") double lon) { // 💡 경도 파라미터 추가
        
        // [2단계] WeatherService를 통해 모든 날씨 데이터 (캐싱 포함)를 가져옵니다.
        // 💡 수정된 getWeatherData(double, double) 메서드를 호출합니다.
        WeatherDto weatherData = weatherService.getWeatherData(lat, lon);

        // [3단계] 가져온 데이터를 RecommendationService에 전달하여 추천 메시지(String)를 받습니다.
        String recommendation = recommendationService.getRecommendation(weatherData);

        return recommendation; // 추천 메시지 문자열을 반환합니다.
    }
}