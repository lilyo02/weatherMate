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

    //@GetMapping("/weather")
    //public WeatherDto getWeather(@RequestParam("city") String city) {
    //    return weatherService.getWeatherData(city);
    //}
    
    @GetMapping("/weather")
    public WeatherDto getWeather(
        @RequestParam("lat") double lat, 
        @RequestParam("lon") double lon) {
        
        // 💡 서비스 메서드 호출 파라미터도 변경
        return weatherService.getWeatherData(lat, lon); 
    }
}