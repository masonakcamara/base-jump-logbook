package com.masonakcamara.basejump.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WeatherService {
    private static final String API_KEY;
    private static final String ENDPOINT = "https://api.openweathermap.org/data/2.5/forecast";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        try (InputStream in = WeatherService.class.getResourceAsStream("/weather.properties")) {
            Properties props = new Properties();
            props.load(in);
            API_KEY = props.getProperty("api.key");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load weather.properties", e);
        }
    }

    public List<Forecast> get5DayForecast(double lat, double lon) throws Exception {
        String uri = String.format("%s?lat=%f&lon=%f&units=imperial&appid=%s", ENDPOINT, lat, lon, API_KEY);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
        HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

        if (resp.statusCode() != 200) {
            JsonNode errorNode = MAPPER.readTree(resp.body());
            String msg = errorNode.has("message") ? errorNode.get("message").asText() : "HTTP " + resp.statusCode();
            throw new RuntimeException("OpenWeatherMap error: " + msg);
        }

        JsonNode root = MAPPER.readTree(resp.body());
        JsonNode listNode = root.get("list");
        if (listNode == null || !listNode.isArray()) {
            throw new RuntimeException("Unexpected response from weather API: missing 'list' field");
        }

        List<Forecast> forecasts = new ArrayList<>();
        for (JsonNode item : listNode) {
            LocalDateTime dt = LocalDateTime.parse(item.get("dt_txt").asText(), FORMATTER);
            JsonNode main = item.get("main");
            JsonNode wind = item.get("wind");
            double temp = main.get("temp").asDouble();
            double windSpeed = wind.get("speed").asDouble();
            double pop = item.has("pop") ? item.get("pop").asDouble() : 0.0;
            forecasts.add(new Forecast(dt, temp, windSpeed, pop));
        }
        return forecasts;
    }
}