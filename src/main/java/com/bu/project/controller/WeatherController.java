package com.bu.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bu.project.dto.WeatherDto;
import com.bu.project.service.WeatherService;

@RestController
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    // 날씨 정보 조회 API
    @GetMapping("/weather")
    public WeatherDto getWeather(
        @RequestParam("lat") double lat, 
        @RequestParam("lon") double lon) {
        
        return weatherService.getWeatherData(lat, lon); 
    }
}