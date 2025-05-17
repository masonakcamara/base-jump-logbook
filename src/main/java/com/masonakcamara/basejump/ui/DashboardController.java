package com.masonakcamara.basejump.ui;

import com.masonakcamara.basejump.api.Forecast;
import com.masonakcamara.basejump.api.WeatherService;
import com.masonakcamara.basejump.model.JumpEntry;
import com.masonakcamara.basejump.model.JumpType;
import com.masonakcamara.basejump.model.SliderPosition;
import com.masonakcamara.basejump.persistence.JumpEntryDao;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.converter.DoubleStringConverter;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private TableView<JumpEntry> jumpTable;
    @FXML private TableColumn<JumpEntry, ?> colDate;
    @FXML private TableColumn<JumpEntry, ?> colLocation;
    @FXML private TableColumn<JumpEntry, ?> colHeight;
    @FXML private TableColumn<JumpEntry, ?> colType;
    @FXML private LineChart<Number, Number> tempChart;
    @FXML private Button btnAdd, btnEdit, btnDelete, btnExport;

    private final JumpEntryDao dao = new JumpEntryDao();
    private final WeatherService weatherService = new WeatherService();

    @FXML
    public void initialize() {
        // set up columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("objectName"));
        colHeight.setCellValueFactory(new PropertyValueFactory<>("height"));
        colType.setCellValueFactory(new PropertyValueFactory<>("jumpType"));

        // load data
        refreshTable();

        // selection listener to load forecast
        jumpTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) loadForecast(sel); });

        // wire buttons
        btnAdd.setOnAction(e -> showEntryDialog(null));
        btnEdit.setOnAction(e -> {
            JumpEntry sel = jumpTable.getSelectionModel().getSelectedItem();
            if (sel != null) showEntryDialog(sel);
        });
        btnDelete.setOnAction(e -> {
            JumpEntry sel = jumpTable.getSelectionModel().getSelectedItem();
            if (sel != null && confirm("Delete entry?")) {
                dao.delete(sel);
                refreshTable();
            }
        });
        btnExport.setOnAction(e -> exportCsv());

        // auto-select first
        if (!jumpTable.getItems().isEmpty()) {
            jumpTable.getSelectionModel().selectFirst();
        }
    }

    private void refreshTable() {
        List<JumpEntry> entries = dao.findAll();
        jumpTable.setItems(FXCollections.observableList(entries));
    }

    private void showEntryDialog(JumpEntry existing) {
        boolean isNew = existing == null;
        JumpEntry entry = isNew
                ? new JumpEntry()
                : new JumpEntry(existing.getDateTime(), existing.getObjectName(),
                existing.getLatitude(), existing.getLongitude(),
                existing.getHeight(), existing.getContainer(),
                existing.getMainParachute(), existing.getPilotChute(),
                existing.getSliderPosition(), existing.getJumpType(),
                existing.getMediaLink());

        // build form
        Dialog<JumpEntry> dlg = new Dialog<>();
        dlg.setTitle(isNew ? "Add Jump" : "Edit Jump");
        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        DatePicker datePicker = new DatePicker();
        TextField timeField = new TextField();
        datePicker.setValue(entry.getDateTime() != null ? entry.getDateTime().toLocalDate() : LocalDateTime.now().toLocalDate());
        timeField.setText(entry.getDateTime() != null ? entry.getDateTime().toLocalTime().toString() : "12:00");

        TextField locField = new TextField(entry.getObjectName());
        TextField latField = new TextField(Double.toString(entry.getLatitude()));
        TextField lonField = new TextField(Double.toString(entry.getLongitude()));
        TextField heightField = new TextField(Double.toString(entry.getHeight()));
        TextField contField = new TextField(entry.getContainer());
        TextField mainField = new TextField(entry.getMainParachute());
        TextField pilotField = new TextField(entry.getPilotChute());
        ComboBox<SliderPosition> sliderCb = new ComboBox<>(FXCollections.observableArrayList(SliderPosition.values()));
        sliderCb.setValue(entry.getSliderPosition());
        ComboBox<JumpType> typeCb = new ComboBox<>(FXCollections.observableArrayList(JumpType.values()));
        typeCb.setValue(entry.getJumpType());
        TextField mediaField = new TextField(entry.getMediaLink());

        grid.addRow(0, new Label("Date:"), datePicker, new Label("Time (HH:MM):"), timeField);
        grid.addRow(1, new Label("Location:"), locField);
        grid.addRow(2, new Label("Lat:"), latField, new Label("Lon:"), lonField);
        grid.addRow(3, new Label("Height:"), heightField);
        grid.addRow(4, new Label("Container:"), contField);
        grid.addRow(5, new Label("Main Chute:"), mainField);
        grid.addRow(6, new Label("Pilot Chute:"), pilotField);
        grid.addRow(7, new Label("Slider:"), sliderCb, new Label("Type:"), typeCb);
        grid.addRow(8, new Label("Media URL:"), mediaField);

        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn == ok) {
                LocalDateTime dt = LocalDateTime.parse(
                        datePicker.getValue() + "T" + timeField.getText()
                );
                entry.setDateTime(dt);
                entry.setObjectName(locField.getText());
                entry.setLatitude(Double.parseDouble(latField.getText()));
                entry.setLongitude(Double.parseDouble(lonField.getText()));
                entry.setHeight(Double.parseDouble(heightField.getText()));
                entry.setContainer(contField.getText());
                entry.setMainParachute(mainField.getText());
                entry.setPilotChute(pilotField.getText());
                entry.setSliderPosition(sliderCb.getValue());
                entry.setJumpType(typeCb.getValue());
                entry.setMediaLink(mediaField.getText());
                return entry;
            }
            return null;
        });

        Optional<JumpEntry> result = dlg.showAndWait();
        result.ifPresent(updated -> {
            if (isNew) {
                dao.save(updated);
                refreshTable();
                jumpTable.getSelectionModel().select(updated);
            } else {
                // copy fields back into the original entity before updating
                existing.setDateTime(updated.getDateTime());
                existing.setObjectName(updated.getObjectName());
                existing.setLatitude(updated.getLatitude());
                existing.setLongitude(updated.getLongitude());
                existing.setHeight(updated.getHeight());
                existing.setContainer(updated.getContainer());
                existing.setMainParachute(updated.getMainParachute());
                existing.setPilotChute(updated.getPilotChute());
                existing.setSliderPosition(updated.getSliderPosition());
                existing.setJumpType(updated.getJumpType());
                existing.setMediaLink(updated.getMediaLink());
                dao.update(existing);
                refreshTable();
                jumpTable.getSelectionModel().select(existing);
            }
        });
    }

    private boolean confirm(String msg) {
        return new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL)
                .showAndWait()
                .filter(b -> b == ButtonType.OK)
                .isPresent();
    }

    private void loadForecast(JumpEntry entry) {
        tempChart.getData().clear();
        new Thread(() -> {
            try {
                List<Forecast> list = weatherService.get5DayForecast(
                        entry.getLatitude(), entry.getLongitude()
                );
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName("Temp (°F)");
                for (int i = 0; i < list.size(); i++) {
                    series.getData().add(new XYChart.Data<>(i, list.get(i).getTemperature()));
                }
                Platform.runLater(() -> tempChart.getData().add(series));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void exportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Jump Log as CSV");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File file = chooser.showSaveDialog(jumpTable.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter out = new PrintWriter(file)) {
            // header
            out.println("DateTime,Location,Latitude,Longitude,Height,Container,MainChute,PilotChute,Slider,Type,MediaLink");
            for (JumpEntry j : jumpTable.getItems()) {
                out.printf("\"%s\",\"%s\",%.6f,%.6f,%.1f,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        j.getDateTime(),
                        j.getObjectName(),
                        j.getLatitude(),
                        j.getLongitude(),
                        j.getHeight(),
                        j.getContainer(),
                        j.getMainParachute(),
                        j.getPilotChute(),
                        j.getSliderPosition(),
                        j.getJumpType(),
                        j.getMediaLink() == null ? "" : j.getMediaLink()
                );
            }
            new Alert(Alert.AlertType.INFORMATION, "Exported to " + file.getName()).show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }

}