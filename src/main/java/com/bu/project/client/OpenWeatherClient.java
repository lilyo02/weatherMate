package com.bu.project.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenWeatherClient {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    // One Call API, Current Weather, Forecast, Air Pollution 모두 /data/2.5를 사용하므로,
    // 하나의 WebClient로 통합하여 사용합니다. (리소스 효율성 증대)
    private final WebClient weatherDataWebClient = WebClient.builder().baseUrl("https://api.openweathermap.org/data/2.5").build();


    // 1. 현재 날씨 정보 호출 메서드 (이름: getCurrentWeather, 역할: /weather 호출)
    public String getCurrentWeather(double lat, double lon) {
    	 String url = "/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric&lang=kr";
    	 
        return weatherDataWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // 2. 미세먼지(대기 오염) 정보 호출 메서드
    public String getAirPollution(double lat, double lon) {
        String url = "/air_pollution?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey;
        
        return weatherDataWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
    
    // 3. 5일치 시간별 예보 호출 메서드 (새로 추가, 역할: /forecast 호출)
    public String getFiveDayForecast(double lat, double lon) {
        String url = "/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric&lang=kr";
        
        return weatherDataWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // 도시 이름으로 위도/경도 정보를 가져오는 메서드
    public String getCoordinatesByCity(String city) {
     // GeoCoding API 엔드포인트: /geo/1.0/direct
     String url = "/geo/1.0/direct?q=" + city + "&limit=1&appid=" + apiKey;
     
     // 이 API는 data/2.5가 아닌 data/1.0이므로 base URL을 새로 지정하거나
     // 전체 URL을 사용해야 합니다. 
     // 임시로 전체 URL을 사용하겠습니다.
     WebClient geoClient = WebClient.builder().baseUrl("https://api.openweathermap.org").build();
     
     return geoClient.get()
             .uri(url)
             .retrieve()
             .bodyToMono(String.class)
             .block();
    }
    
 // 💡 5. 역지오코딩 (위도/경도로 도시 이름 정보를 가져오는 메서드)
    public String getCityNameByCoordinates(double lat, double lon) {
        // GeoCoding API 엔드포인트: /geo/1.0/reverse
        String url = "/geo/1.0/reverse?lat=" + lat + "&lon=" + lon + "&limit=1&appid=" + apiKey;
        
        // 이 API는 data/2.5가 아닌 data/1.0이므로 base URL을 새로 지정하거나
        // 전체 URL을 사용해야 합니다. (이전 getCoordinatesByCity와 동일하게 처리)
        WebClient geoClient = WebClient.builder().baseUrl("https://api.openweathermap.org").build();
        
        return geoClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}