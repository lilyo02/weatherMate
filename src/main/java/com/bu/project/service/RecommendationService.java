package com.bu.project.service;

import com.bu.project.dto.WeatherDto;

public interface RecommendationService {
	/**
     * 날씨 데이터(WeatherDto)를 기반으로 사용자에게 추천 메시지를 반환합니다.
     * @param weatherData 모든 날씨 정보가 담긴 DTO
     * @return 추천 메시지 문자열
     */
    String getRecommendation(WeatherDto weatherData);
}
