// com.bu.project.service.RecommendationServiceImpl.java

package com.bu.project.service;

import org.springframework.stereotype.Service;

import com.bu.project.dto.WeatherDto;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Override
    public String getRecommendation(WeatherDto weatherData) {
        double currentTemp = weatherData.getCurrentTemp();
        String description = weatherData.getDescription();
        double currentPop = weatherData.getCurrentPop();
        String recommendation = "";

        // -----------------------------------------------------------------
        // [1. 날씨 상태에 따른 추천]
        // -----------------------------------------------------------------
        if (description.contains("비") || description.contains("소나기") || currentPop > 0.5) {
            recommendation = "☔ 비 소식이 있어요. 우산을 꼭 챙기세요! ";
        } else if (description.contains("맑음")) {
            recommendation = "☀️ 맑고 화창한 날씨입니다! ";
        } else if (description.contains("흐림") || description.contains("구름")) {
            recommendation = "☁️ 구름이 많아요. 일교차에 대비하세요. ";
        } else {
            recommendation = "오늘 날씨는 " + description + "입니다. ";
        }

        // -----------------------------------------------------------------
        // [2. 기온에 따른 옷차림 추천] (새로운 차트 기준 적용)
        // -----------------------------------------------------------------
        String clothesRecommendation;
        
        if (currentTemp >= 28) { // ~28°C
            clothesRecommendation = "민소매, 반팔, 반바지, 원피스로 시원하게 입으세요.";
        } else if (currentTemp >= 23) { // 27°C ~ 23°C
            clothesRecommendation = "반팔, 얇은 셔츠, 반바지, 면바지가 적당해요.";
        } else if (currentTemp >= 20) { // 22°C ~ 20°C
            clothesRecommendation = "얇은 가디건, 긴팔, 면바지, 청치마 등 가벼운 겉옷을 준비하세요.";
        } else if (currentTemp >= 17) { // 19°C ~ 17°C
            clothesRecommendation = "얇은 니트, 맨투맨, 가디건, 청재킷으로 쌀쌀함을 대비하세요.";
        } else if (currentTemp >= 12) { // 16°C ~ 12°C
            clothesRecommendation = "자켓, 가디건, 야상, 스타킹, 청바지 등 보온에 신경쓰세요.";
        } else if (currentTemp >= 9) { // 11°C ~ 9°C
            clothesRecommendation = "트렌치코트, 야상, 니트, 청재킷 등 따뜻하게 껴입으세요.";
        } else if (currentTemp >= 5) { // 8°C ~ 5°C
            clothesRecommendation = "코트, 가죽자켓, 히트텍, 니트 등 방한 복장이 필요합니다.";
        } else { // 4°C 이하
            clothesRecommendation = "패딩, 두꺼운 코트, 목도리, 기모 제품 등 최대한 따뜻하게 무장하세요!";
        }
        
        recommendation += clothesRecommendation; // 기존 추천 문구에 옷차림 추가
        
        // -----------------------------------------------------------------
        // [3. 미세먼지 상태에 따른 추가 추천] (기존 로직 유지)
        // -----------------------------------------------------------------
        if (weatherData.getAirPollution() != null) {
            String airGrade = weatherData.getAirPollution().getGrade();
            if (airGrade.equals("나쁨") || airGrade.equals("매우 나쁨")) {
                recommendation += " 🚨 미세먼지가 높으니 KF94 마스크 착용과 외출 자제가 필요합니다.";
            } else if (airGrade.equals("좋음")) {
                 recommendation += " ✨ 공기가 깨끗합니다! 마스크 없이 상쾌하게 산책하기 좋아요.";
            }
        }
        
        return recommendation.trim();
    }
}