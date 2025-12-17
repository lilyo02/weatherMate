package com.bu.project.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenWeatherClient {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    // OpenWeather API (/data/2.5)
    private final WebClient weatherDataWebClient = WebClient.builder().baseUrl("https://api.openweathermap.org/data/2.5").build();


    // 현재 날씨 정보 호출
    public String getCurrentWeather(double lat, double lon) {
    	 String url = "/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric&lang=kr";
    	 
        return weatherDataWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // 대기 오염(미세먼지) 정보 호출
    public String getAirPollution(double lat, double lon) {
        String url = "/air_pollution?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey;
        
        return weatherDataWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
    
    // 5일치 시간별 예보 호출
    public String getFiveDayForecast(double lat, double lon) {
        String url = "/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric&lang=kr";
        
        return weatherDataWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // 지오코딩 (도시 이름으로 위도/경도 정보 조회)
    public String getCoordinatesByCity(String city) {
     // GeoCoding API 엔드포인트: /geo/1.0/direct
     String url = "/geo/1.0/direct?q=" + city + "&limit=1&appid=" + apiKey;
     
     WebClient geoClient = WebClient.builder().baseUrl("https://api.openweathermap.org").build();
     
     return geoClient.get()
             .uri(url)
             .retrieve()
             .bodyToMono(String.class)
             .block();
    }
    
    // 역지오코딩 (위도/경도로 도시 이름 조회)
    public String getCityNameByCoordinates(double lat, double lon) {
        // GeoCoding API 엔드포인트: /geo/1.0/reverse
        String url = "/geo/1.0/reverse?lat=" + lat + "&lon=" + lon + "&limit=1&appid=" + apiKey;
        
        WebClient geoClient = WebClient.builder().baseUrl("https://api.openweathermap.org").build();
        
        return geoClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}