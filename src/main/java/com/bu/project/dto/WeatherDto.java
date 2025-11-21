package com.bu.project.dto;

import java.util.List;

public class WeatherDto {
    private String cityName;
    private double currentTemp; // 현재 기온
    private String description; // 날씨 설명
    private double currentPop; // 현재 강수 확률
    
    private double currentFeelsLike;   // 현재 체감온도
    private double minFeelsLike;       // 예보 중 최저 체감온도
    private double maxFeelsLike;
    
    public double getCurrentFeelsLike() {
		return currentFeelsLike;
	}

	public void setCurrentFeelsLike(double currentFeelsLike) {
		this.currentFeelsLike = currentFeelsLike;
	}

	public double getMinFeelsLike() {
		return minFeelsLike;
	}

	public void setMinFeelsLike(double minFeelsLike) {
		this.minFeelsLike = minFeelsLike;
	}

	public double getMaxFeelsLike() {
		return maxFeelsLike;
	}

	public void setMaxFeelsLike(double maxFeelsLike) {
		this.maxFeelsLike = maxFeelsLike;
	}

	private String recommendText;
    
    // 일별 날씨 예보 리스트 (최고/최저 기온, 강수확률)
    private List<DailyForecastDto> dailyForecasts;
    
    // 시간별 날씨 예보 리스트
    private List<HourlyForecastDto> hourlyForecasts;

    // 미세먼지 데이터
    private AirPollutionDto airPollution;
    
    
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public double getCurrentTemp() {
		return currentTemp;
	}

	public void setCurrentTemp(double currentTemp) {
		this.currentTemp = currentTemp;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getCurrentPop() {
		return currentPop;
	}

	public void setCurrentPop(double currentPop) {
		this.currentPop = currentPop;
	}
	
	public String getRecommendText() {
        return recommendText;
    }

    public void setRecommendText(String recommendText) {
        this.recommendText = recommendText;
    }

    
    public List<DailyForecastDto> getDailyForecasts() {
        return dailyForecasts;
    }

    public void setDailyForecasts(List<DailyForecastDto> dailyForecasts) {
        this.dailyForecasts = dailyForecasts;
    }

    public List<HourlyForecastDto> getHourlyForecasts() {
        return hourlyForecasts;
    }

    public void setHourlyForecasts(List<HourlyForecastDto> hourlyForecasts) {
        this.hourlyForecasts = hourlyForecasts;
    }

    public AirPollutionDto getAirPollution() {
        return airPollution;
    }

    public void setAirPollution(AirPollutionDto airPollution) {
        this.airPollution = airPollution;
    }
}