package com.bu.project.dto;

public class HourlyForecastDto {
	private String time; // 시간
    private String icon; // 날씨 아이콘
    private double temp;
    private double pop; // 강수 확률
    
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public double getTemp() {
		return temp;
	}
	public void setTemp(double temp) {
		this.temp = temp;
	}
	public double getPop() {
		return pop;
	}
	public void setPop(double pop) {
		this.pop = pop;
	}
}
