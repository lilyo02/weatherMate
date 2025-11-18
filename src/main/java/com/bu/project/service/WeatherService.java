package com.bu.project.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bu.project.client.OpenWeatherClient;
import com.bu.project.dto.AirPollutionDto;
import com.bu.project.dto.DailyForecastDto;
import com.bu.project.dto.HourlyForecastDto;
import com.bu.project.dto.WeatherDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WeatherService {

    private final OpenWeatherClient openWeatherClient;
    private final RecommendationService recommendationService;

    public WeatherService(OpenWeatherClient openWeatherClient, 
    		RecommendationService recommendationService) {
        this.openWeatherClient = openWeatherClient;
        this.recommendationService = recommendationService;
    }

    // 💡 시그니처 변경: 위도와 경도를 받도록 수정
    // 💡 캐시 키 변경: lat과 lon을 조합하여 유니크하게 만듭니다.
//    @Cacheable(value = "weatherCache", key = "#lat + '-' + #lon")
    public WeatherDto getWeatherData(double lat, double lon) {
        
        ObjectMapper objectMapper = new ObjectMapper();
        String city = "위치 정보 없음"; // 기본 도시 이름 설정
        
        try {
            // ----------------------------------------------------
            // 1. 역지오코딩 API 호출 및 도시 이름 파싱 (Reverse GeoCoding)
            // ----------------------------------------------------
            // 기존: 도시 이름으로 위도/경도 찾기 로직 (제거)
            // 변경: 위도/경도로 도시 이름 찾기
            String geoJson = openWeatherClient.getCityNameByCoordinates(lat, lon); // 💡 수정된 클라이언트 메서드 호출
            JsonNode geoNode = objectMapper.readTree(geoJson);

            // GeoCoding API는 JSON 배열을 반환. 첫 번째 결과에서 name을 가져옴.
            if (geoNode.isArray() && geoNode.size() > 0) {
                // 첫 번째 도시의 이름을 사용 (name 필드가 지역 이름)
                JsonNode location = geoNode.get(0);
                city = location.get("name").asText(); 
                
                // 한국어 이름이 있다면 그것을 사용하도록 로직을 추가할 수도 있습니다.
                if (location.has("local_names") && location.get("local_names").has("ko")) {
                     city = location.get("local_names").get("ko").asText();
                } else if (location.has("state")) {
                     city = location.get("state").asText() + " " + city;
                }
            } else {
                // 역지오코딩 실패 시 (예외 발생 대신 'city' 변수에 기본값을 유지)
                System.err.println("Reverse GeoCoding failed for: " + lat + ", " + lon);
            }
            
            // ----------------------------------------------------
            // 2. 날씨 API 호출 (lat, lon은 이제 파라미터로 받습니다)
            // ----------------------------------------------------
            String currentJson = openWeatherClient.getCurrentWeather(lat, lon); 
            String airPollutionJson = openWeatherClient.getAirPollution(lat, lon);
            String forecastJson = openWeatherClient.getFiveDayForecast(lat, lon);

            WeatherDto weatherDto = new WeatherDto();
            weatherDto.setCityName(city); // 💡 여기서 추출한 city 이름 설정
            
            // --- 3. 현재 날씨 파싱 ---
            JsonNode currentNode = objectMapper.readTree(currentJson);
            weatherDto.setCurrentTemp(currentNode.get("main").get("temp").asDouble());
            weatherDto.setDescription(currentNode.get("weather").get(0).get("description").asText());
            
            // --- 4. 예보 파싱 및 계산 (Hourly, Daily) ---
            JsonNode forecastNode = objectMapper.readTree(forecastJson);
            JsonNode listNode = forecastNode.get("list");
            
            // 시간/날짜 포매터 정의
            DateTimeFormatter apiDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);  
            DateTimeFormatter hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            // listNode의 첫 번째 항목(가장 가까운 미래 시점의 예보)에서 강수 확률 가져오기
            if (listNode.size() > 0 && listNode.get(0).has("pop")) {
                double popValue = listNode.get(0).get("pop").asDouble();
                weatherDto.setCurrentPop(popValue); 
            }
            
            // 일별 예보 계산을 위한 임시 맵 초기화
            Map<String, DailyForecastDto> dailyMap = new LinkedHashMap<>();
            List<HourlyForecastDto> hourlyForecasts = new ArrayList<>();
            List<DailyForecastDto> dailyForecasts = new ArrayList<>();  

            // 40개 전체 예보 데이터를 순회하며 시간별 저장 및 일별 계산을 동시에 수행
            for (int i = 0; i < listNode.size(); i++) {
                JsonNode item = listNode.get(i);
                String dateTimeString = item.get("dt_txt").asText();
                double currentTemp = item.get("main").get("temp").asDouble();
                double popValue = item.get("pop").asDouble();
                
                // UTC 시간을 KST로 변환
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, apiDateFormatter);
                ZonedDateTime utcTime = localDateTime.atZone(ZoneId.of("UTC"));
                ZonedDateTime koreaTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
                
                // A. 시간별 예보 저장 (첫 8개 항목만 사용)
                if (i < 8) {
                    HourlyForecastDto dto = new HourlyForecastDto();
                    dto.setTime(koreaTime.format(hourMinuteFormatter));  
                    dto.setTemp(currentTemp);
                    dto.setIcon(item.get("weather").get(0).get("icon").asText());
                    dto.setPop(popValue);
                    hourlyForecasts.add(dto);
                }
                
                // B. 일별 최고/최저 기온 계산
                String dateKey = koreaTime.toLocalDate().toString();  
                
                if (!dailyMap.containsKey(dateKey)) {
                    // 새 날짜 DTO 생성
                    DailyForecastDto newDto = new DailyForecastDto();
                    newDto.setDayOfWeek(koreaTime.format(dayOfWeekFormatter));  
                    newDto.setMaxTemp(currentTemp);  
                    newDto.setMinTemp(currentTemp);
                    newDto.setPop(popValue);
                    dailyMap.put(dateKey, newDto);
                } else {
                    // 최고/최저 기온, 최대 강수 확률 업데이트
                    DailyForecastDto existingDto = dailyMap.get(dateKey);
                    if (currentTemp > existingDto.getMaxTemp()) {
                        existingDto.setMaxTemp(currentTemp);
                    }
                    if (currentTemp < existingDto.getMinTemp()) {
                        existingDto.setMinTemp(currentTemp);
                    }
                    if (popValue > existingDto.getPop()) {
                        existingDto.setPop(popValue);
                    }
                }
            }
            
            // 최종 DTO에 저장
            weatherDto.setHourlyForecasts(hourlyForecasts);
            dailyForecasts.addAll(dailyMap.values());
            
//            // *주의*: 첫 번째 항목 제거 로직은 그대로 유지
//            if (!dailyForecasts.isEmpty()) {
//                 dailyForecasts.remove(0);  
//            }
//            
            weatherDto.setDailyForecasts(dailyForecasts);  

            
            // --- 5. 미세먼지 파싱 (로직 유지) ---
            JsonNode airPollutionNode = objectMapper.readTree(airPollutionJson);
            JsonNode components = airPollutionNode.get("list").get(0).get("components");
            
            AirPollutionDto pollutionDto = new AirPollutionDto();
            pollutionDto.setPm10(components.get("pm10").asDouble());
            pollutionDto.setPm25(components.get("pm2_5").asDouble());
            
            double pm10Value = pollutionDto.getPm10();
            String grade;

            if (pm10Value <= 30) {
                grade = "좋음";
            } else if (pm10Value <= 80) {
                grade = "보통";
            } else if (pm10Value <= 150) {
                grade = "나쁨";
            } else {
                grade = "매우 나쁨";
            }

            pollutionDto.setGrade(grade);
            weatherDto.setAirPollution(pollutionDto);
            
            String rec = recommendationService.getRecommendation(weatherDto);
            weatherDto.setRecommendText(rec);
            
            return weatherDto;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}