package com.bu.project.service;

import org.springframework.stereotype.Service;

import com.bu.project.dto.WeatherDto;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Override
    public String getRecommendation(WeatherDto weatherData) {

        // 체감온도 및 날씨 정보
        double currentFeel = weatherData.getCurrentFeelsLike();
        double minFeel = weatherData.getMinFeelsLike();
        double maxFeel = weatherData.getMaxFeelsLike();
        double pop = weatherData.getCurrentPop();
        String description = weatherData.getDescription();

        StringBuilder rec = new StringBuilder();

        // 기본 날씨 설명 문장
        if (description.contains("비") || description.contains("소나기") || pop > 0.5) {
            rec.append("☔ 비 소식이 있어요. 우산을 꼭 챙기세요! ");
        } else if (description.contains("맑음")) {
            rec.append("☀️ 맑고 화창한 날씨입니다! ");
        } else if (description.contains("흐림") || description.contains("구름")) {
            rec.append("☁️ 구름이 많아요. 일교차에 대비하세요. ");
        } else {
            rec.append("오늘 날씨는 ").append(description).append("입니다. ");
        }

        // 옷차림 추천 (최저 체감온도 기반)
        String clothes;

        if (minFeel <= 0) {
            clothes = "아침·저녁은 매우 춥습니다. 두꺼운 패딩, 니트, 목도리를 꼭 챙기세요.";
        } else if (minFeel <= 5) {
            clothes = "아침·저녁은 겨울처럼 춥습니다. 두꺼운 긴팔에 패딩이나 두꺼운 코트를 추천해요.";
        } else if (minFeel <= 10) {
            clothes = "쌀쌀한 날씨입니다. 긴팔 상의에 경량 패딩이나 코트를 걸치면 좋아요.";
        } else if (minFeel <= 15) {
            clothes = "선선한 날씨입니다. 가벼운 자켓이나 얇은 니트 정도가 적당해요.";
        } else {
            clothes = "온화한 날씨입니다. 가벼운 옷차림도 괜찮아요.";
        }

        rec.append(" ").append(clothes);

        // 낮 시간대 체감온도 반영
        if (maxFeel >= 18) {
            rec.append(" 한낮에는 꽤 따뜻해져서 겉옷을 벗어도 되는 정도예요.");
        } else if (maxFeel >= 12) {
            rec.append(" 한낮에는 아침보다는 조금 더 포근해져요.");
        } else {
            rec.append(" 한낮에도 크게 따뜻해지지 않아 하루 종일 겉옷이 필요합니다.");
        }

        // 일교차 반영
        double gap = maxFeel - minFeel;
        if (gap >= 8) {
            rec.append(" 일교차가 큰 날이라 겹쳐 입기 좋은 옷차림을 추천해요.");
        }

        // 미세먼지 정보
        if (weatherData.getAirPollution() != null) {
            String grade = weatherData.getAirPollution().getGrade();

            if (grade.equals("나쁨") || grade.equals("매우 나쁨")) {
                rec.append(" 🚨 미세먼지가 높으니 KF94 마스크 착용을 추천합니다.");
            } else if (grade.equals("좋음")) {
                rec.append(" ✨ 공기가 깨끗해 상쾌하게 산책하기 좋아요.");
            }
        }

        return rec.toString().trim();
    }
}
