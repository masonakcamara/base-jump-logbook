package com.masonakcamara.basejump.api;

import java.time.LocalDateTime;

public class Forecast {
    private final LocalDateTime dateTime;
    private final double temperature;
    private final double windSpeed;
    private final double precipitationProbability;

    public Forecast(LocalDateTime dateTime, double temperature, double windSpeed, double precipitationProbability) {
        this.dateTime = dateTime;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.precipitationProbability = precipitationProbability;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getPrecipitationProbability() {
        return precipitationProbability;
    }
}