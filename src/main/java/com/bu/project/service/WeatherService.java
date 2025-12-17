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

    // 날씨 데이터 조회
    public WeatherDto getWeatherData(double lat, double lon) {

        ObjectMapper objectMapper = new ObjectMapper();
        String city = "위치 정보 없음";

        try {
            // 도시 이름 조회
            String geoJson = openWeatherClient.getCityNameByCoordinates(lat, lon);
            JsonNode geoNode = objectMapper.readTree(geoJson);

            if (geoNode.isArray() && geoNode.size() > 0) {
                JsonNode location = geoNode.get(0);
                city = location.get("name").asText();

                if (location.has("local_names") && location.get("local_names").has("ko")) {
                    city = location.get("local_names").get("ko").asText();
                } else if (location.has("state")) {
                    city = location.get("state").asText() + " " + city;
                }
            } else {
                System.err.println("Reverse GeoCoding failed for: " + lat + ", " + lon);
            }

            // 날씨 API 호출
            String currentJson = openWeatherClient.getCurrentWeather(lat, lon);
            String airPollutionJson = openWeatherClient.getAirPollution(lat, lon);
            String forecastJson = openWeatherClient.getFiveDayForecast(lat, lon);

            WeatherDto weatherDto = new WeatherDto();
            weatherDto.setCityName(city);

            // 현재 날씨 파싱
            JsonNode currentNode = objectMapper.readTree(currentJson);
            double currentTemp = currentNode.get("main").get("temp").asDouble();
            double currentFeelsLike = currentNode.get("main").get("feels_like").asDouble(); // ✅ 체감온도

            weatherDto.setCurrentTemp(currentTemp);
            weatherDto.setCurrentFeelsLike(currentFeelsLike); // ✅ 추가
            weatherDto.setDescription(currentNode.get("weather").get(0).get("description").asText());

            // 예보 데이터 파싱
            JsonNode forecastNode = objectMapper.readTree(forecastJson);
            JsonNode listNode = forecastNode.get("list");

            DateTimeFormatter apiDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
            DateTimeFormatter hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm");

            if (listNode.size() > 0 && listNode.get(0).has("pop")) {
                double popValue = listNode.get(0).get("pop").asDouble();
                weatherDto.setCurrentPop(popValue);
            }

            Map<String, DailyForecastDto> dailyMap = new LinkedHashMap<>();
            List<HourlyForecastDto> hourlyForecasts = new ArrayList<>();
            List<DailyForecastDto> dailyForecasts = new ArrayList<>();

            // 체감온도 범위 계산
            double minFeelsLike = Double.POSITIVE_INFINITY;
            double maxFeelsLike = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < listNode.size(); i++) {
                JsonNode item = listNode.get(i);
                String dateTimeString = item.get("dt_txt").asText();

                double temp = item.get("main").get("temp").asDouble();
                double feelsLike = item.get("main").get("feels_like").asDouble(); // ✅ 체감온도
                double popValue = item.get("pop").asDouble();

                if (feelsLike < minFeelsLike) {
                    minFeelsLike = feelsLike;
                }
                if (feelsLike > maxFeelsLike) {
                    maxFeelsLike = feelsLike;
                }

                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, apiDateFormatter);
                ZonedDateTime utcTime = localDateTime.atZone(ZoneId.of("UTC"));
                ZonedDateTime koreaTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));

                // 시간별 예보
                if (i < 8) {
                    HourlyForecastDto dto = new HourlyForecastDto();
                    dto.setTime(koreaTime.format(hourMinuteFormatter));
                    dto.setTemp(temp);
                    // 필요하면 dto에 feelsLike 필드 추가해서 저장 가능
                    // dto.setFeelsLike(feelsLike);
                    dto.setIcon(item.get("weather").get(0).get("icon").asText());
                    dto.setPop(popValue);
                    hourlyForecasts.add(dto);
                }

                // 일별 예보
                String dateKey = koreaTime.toLocalDate().toString();

                if (!dailyMap.containsKey(dateKey)) {
                    DailyForecastDto newDto = new DailyForecastDto();
                    newDto.setDayOfWeek(koreaTime.format(dayOfWeekFormatter));
                    newDto.setMaxTemp(temp);
                    newDto.setMinTemp(temp);
                    newDto.setPop(popValue);
                    dailyMap.put(dateKey, newDto);
                } else {
                    DailyForecastDto existingDto = dailyMap.get(dateKey);
                    if (temp > existingDto.getMaxTemp()) {
                        existingDto.setMaxTemp(temp);
                    }
                    if (temp < existingDto.getMinTemp()) {
                        existingDto.setMinTemp(temp);
                    }
                    if (popValue > existingDto.getPop()) {
                        existingDto.setPop(popValue);
                    }
                }
            }

            weatherDto.setHourlyForecasts(hourlyForecasts);
            dailyForecasts.addAll(dailyMap.values());
            weatherDto.setDailyForecasts(dailyForecasts);

            if (minFeelsLike != Double.POSITIVE_INFINITY) {
                weatherDto.setMinFeelsLike(minFeelsLike);
                weatherDto.setMaxFeelsLike(maxFeelsLike);
            }

            // 미세먼지 정보 파싱
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

            // 추천 문장 생성
            String rec = recommendationService.getRecommendation(weatherDto);
            weatherDto.setRecommendText(rec);

            return weatherDto;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
