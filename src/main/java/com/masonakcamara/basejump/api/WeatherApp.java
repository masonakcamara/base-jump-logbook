package com.masonakcamara.basejump.api;

import java.util.List;

public class WeatherApp {
    public static void main(String[] args) {
        double latitude = 34.0;
        double longitude = -117.0;

        try {
            WeatherService service = new WeatherService();
            List<Forecast> forecasts = service.get5DayForecast(latitude, longitude);

            System.out.println("5-Day Forecast:");
            for (Forecast f : forecasts) {
                System.out.printf(
                        "%s | Temp: %.1f°F | Wind: %.1f mph | Precip: %.0f%%%n",
                        f.getDateTime(),
                        f.getTemperature(),
                        f.getWindSpeed(),
                        f.getPrecipitationProbability() * 100
                );
            }
        } catch (Exception e) {
            System.err.println("Error fetching forecast: " + e.getMessage());
            e.printStackTrace();
        }
    }
}