package com.bu.project.dto;

public class AirPollutionDto {
	private double pm25;
    private double pm10;
    private String grade; // 미세먼지 등급
    
	public double getPm25() {
		return pm25;
	}
	public void setPm25(double pm25) {
		this.pm25 = pm25;
	}
	public double getPm10() {
		return pm10;
	}
	public void setPm10(double pm10) {
		this.pm10 = pm10;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}   
}