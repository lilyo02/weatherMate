package com.bu.project.service;

import com.bu.project.dto.WeatherDto;

public interface RecommendationService {
	// 추천 문장 생성
    String getRecommendation(WeatherDto weatherData);
}
