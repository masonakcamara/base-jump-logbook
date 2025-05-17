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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class DashboardController {

    @FXML private TableView<JumpEntry> jumpTable;
    @FXML private TableColumn<JumpEntry, ?> colDate;
    @FXML private TableColumn<JumpEntry, ?> colLocation;
    @FXML private TableColumn<JumpEntry, ?> colHeight;
    @FXML private TableColumn<JumpEntry, ?> colType;
    @FXML private LineChart<Number, Number> tempChart;

    private final JumpEntryDao dao = new JumpEntryDao();
    private final WeatherService weatherService = new WeatherService();

    @FXML
    public void initialize() {
        // configure columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("objectName"));
        colHeight.setCellValueFactory(new PropertyValueFactory<>("height"));
        colType.setCellValueFactory(new PropertyValueFactory<>("jumpType"));

        // load and display entries
        List<JumpEntry> entries = dao.findAll();
        jumpTable.setItems(FXCollections.observableList(entries));

        // update chart when selection changes
        jumpTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    if (sel != null) loadForecast(sel);
                });

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
                    series.getData().add(new XYChart.Data<>(i, list.get(i).getTemperature()));
                }
                Platform.runLater(() -> tempChart.getData().add(series));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}