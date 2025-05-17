package com.masonakcamara.basejump.ui;

import com.masonakcamara.basejump.api.Forecast;
import com.masonakcamara.basejump.api.WeatherService;
import com.masonakcamara.basejump.model.JumpEntry;
import com.masonakcamara.basejump.persistence.JumpEntryDao;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;

import java.util.List;

public class DashboardController {

    @FXML
    private TableView<JumpEntry> jumpTable;

    @FXML
    private LineChart<Number, Number> tempChart;

    private final JumpEntryDao dao = new JumpEntryDao();
    private final WeatherService weatherService = new WeatherService();

    @FXML
    public void initialize() {
        // load and show jumps
        var entries = dao.findAll();
        jumpTable.setItems(FXCollections.observableList(entries));

        // when user selects a jump, load its forecast
        jumpTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null) {
                        loadForecast(selected);
                    }
                });

        // select first entry by default
        if (!entries.isEmpty()) {
            jumpTable.getSelectionModel().selectFirst();
        }
    }

    private void loadForecast(JumpEntry entry) {
        tempChart.getData().clear();

        new Thread(() -> {
            try {
                List<Forecast> list = weatherService.get5DayForecast(
                        entry.getLatitude(), entry.getLongitude()
                );
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                for (int i = 0; i < list.size(); i++) {
                    Forecast f = list.get(i);
                    series.getData().add(new XYChart.Data<>(i, f.getTemperature()));
                }
                Platform.runLater(() -> tempChart.getData().add(series));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}