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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private TableView<JumpEntry> jumpTable;
    @FXML private TableColumn<JumpEntry, LocalDateTime> colDate;
    @FXML private TableColumn<JumpEntry, String> colLocation;
    @FXML private TableColumn<JumpEntry, Double> colHeight;
    @FXML private TableColumn<JumpEntry, JumpType> colType;

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnExport;

    @FXML private LineChart<String, Number> tempChart;
    @FXML private CategoryAxis xAxis1;
    @FXML private NumberAxis yAxis1;

    @FXML private LineChart<String, Number> windChart;
    @FXML private CategoryAxis xAxis2;
    @FXML private NumberAxis yAxis2;

    @FXML private WebView mapView;

    private final JumpEntryDao dao = new JumpEntryDao();
    private final WeatherService weatherService = new WeatherService();
    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("MM/dd HH:mm");

    @FXML
    public void initialize() {
        // Table columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        colDate.setCellFactory(col -> new TableCell<JumpEntry, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : dtFmt.format(item));
            }
        });
        colLocation.setCellValueFactory(new PropertyValueFactory<>("objectName"));
        colHeight.setCellValueFactory(new PropertyValueFactory<>("height"));
        colType.setCellValueFactory(new PropertyValueFactory<>("jumpType"));

        // Buttons
        btnAdd.setOnAction(e -> showEntryDialog(null));
        btnEdit.setOnAction(e -> {
            JumpEntry sel = jumpTable.getSelectionModel().getSelectedItem();
            if (sel != null) showEntryDialog(sel);
        });
        btnDelete.setOnAction(e -> {
            JumpEntry sel = jumpTable.getSelectionModel().getSelectedItem();
            if (sel != null && confirm("Delete chosen entry?")) {
                dao.delete(sel);
                refreshTable();
            }
        });
        btnExport.setOnAction(e -> exportCsv());

        // Load data and selection listener
        refreshTable();
        jumpTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) loadForecast(n);
        });
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
        JumpEntry tmp = isNew
                ? new JumpEntry()
                : new JumpEntry(existing.getDateTime(), existing.getObjectName(),
                existing.getLatitude(), existing.getLongitude(),
                existing.getHeight(), existing.getContainer(),
                existing.getMainParachute(), existing.getPilotChute(),
                existing.getSliderPosition(), existing.getJumpType(),
                existing.getMediaLink());

        Dialog<JumpEntry> dlg = new Dialog<>();
        dlg.setTitle(isNew ? "Add Jump" : "Edit Jump");
        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        DatePicker datePicker = new DatePicker();
        TextField timeField = new TextField();
        if (tmp.getDateTime() != null) {
            datePicker.setValue(tmp.getDateTime().toLocalDate());
            timeField.setText(tmp.getDateTime().toLocalTime().toString());
        }
        TextField locField     = new TextField(tmp.getObjectName());
        TextField latField     = new TextField(Double.toString(tmp.getLatitude()));
        TextField lonField     = new TextField(Double.toString(tmp.getLongitude()));
        TextField heightField  = new TextField(Double.toString(tmp.getHeight()));
        TextField contField    = new TextField(tmp.getContainer());
        TextField mainField    = new TextField(tmp.getMainParachute());
        TextField pilotField   = new TextField(tmp.getPilotChute());
        ComboBox<SliderPosition> sliderCb = new ComboBox<>(FXCollections.observableArrayList(SliderPosition.values()));
        sliderCb.setValue(tmp.getSliderPosition());
        ComboBox<JumpType> typeCb = new ComboBox<>(FXCollections.observableArrayList(JumpType.values()));
        typeCb.setValue(tmp.getJumpType());
        TextField mediaField   = new TextField(tmp.getMediaLink());

        grid.addRow(0, new Label("Date:"), datePicker, new Label("Time:"), timeField);
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
                LocalDateTime dt = LocalDateTime.parse(datePicker.getValue() + "T" + timeField.getText());
                tmp.setDateTime(dt);
                tmp.setObjectName(locField.getText());
                tmp.setLatitude(Double.parseDouble(latField.getText()));
                tmp.setLongitude(Double.parseDouble(lonField.getText()));
                tmp.setHeight(Double.parseDouble(heightField.getText()));
                tmp.setContainer(contField.getText());
                tmp.setMainParachute(mainField.getText());
                tmp.setPilotChute(pilotField.getText());
                tmp.setSliderPosition(sliderCb.getValue());
                tmp.setJumpType(typeCb.getValue());
                tmp.setMediaLink(mediaField.getText());
                return tmp;
            }
            return null;
        });

        Optional<JumpEntry> result = dlg.showAndWait();
        result.ifPresent(res -> {
            if (isNew) dao.save(res);
            else {
                existing.setDateTime(res.getDateTime());
                existing.setObjectName(res.getObjectName());
                existing.setLatitude(res.getLatitude());
                existing.setLongitude(res.getLongitude());
                existing.setHeight(res.getHeight());
                existing.setContainer(res.getContainer());
                existing.setMainParachute(res.getMainParachute());
                existing.setPilotChute(res.getPilotChute());
                existing.setSliderPosition(res.getSliderPosition());
                existing.setJumpType(res.getJumpType());
                existing.setMediaLink(res.getMediaLink());
                dao.update(existing);
            }
            refreshTable();
            jumpTable.getSelectionModel().select(isNew ? res : existing);
        });
    }

    private void loadForecast(JumpEntry entry) {
        tempChart.getData().clear();
        windChart.getData().clear();

        new Thread(() -> {
            try {
                List<Forecast> list = weatherService.get5DayForecast(
                        entry.getLatitude(), entry.getLongitude());

                XYChart.Series<String, Number> tempSeries = new XYChart.Series<>();
                tempSeries.setName("Temp (°F)");
                XYChart.Series<String, Number> windSeries = new XYChart.Series<>();
                windSeries.setName("Wind (mph)");

                List<String> categories = FXCollections.observableArrayList();
                for (Forecast f : list) {
                    String label = dtFmt.format(f.getDateTime());
                    categories.add(label);
                    tempSeries.getData().add(new XYChart.Data<>(label, f.getTemperature()));
                    windSeries.getData().add(new XYChart.Data<>(label, f.getWindSpeed()));
                }

                Platform.runLater(() -> {
                    xAxis1.setCategories(FXCollections.observableArrayList(categories));
                    xAxis2.setCategories(FXCollections.observableArrayList(categories));
                    tempChart.getData().setAll(tempSeries);
                    windChart.getData().setAll(windSeries);

                    String mapUrl = String.format(
                            "https://maps.google.com/maps?q=%f,%f&z=12&output=embed",
                            entry.getLatitude(), entry.getLongitude());
                    mapView.getEngine().load(mapUrl);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void exportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Jump Log CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(jumpTable.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter out = new PrintWriter(file)) {
            out.println("DateTime,Location,Lat, Lon,Height,Container,Main, Pilot,Slider,Type,Media");
            for (JumpEntry j : jumpTable.getItems()) {
                out.printf("\"%s\",\"%s\",%.6f,%.6f,%.1f,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        dtFmt.format(j.getDateTime()),
                        j.getObjectName(),
                        j.getLatitude(),
                        j.getLongitude(),
                        j.getHeight(),
                        j.getContainer(),
                        j.getMainParachute(),
                        j.getPilotChute(),
                        j.getSliderPosition(),
                        j.getJumpType(),
                        j.getMediaLink() == null ? "" : j.getMediaLink());
            }
            new Alert(Alert.AlertType.INFORMATION, "Exported to " + file.getName()).show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }

    private boolean confirm(String msg) {
        return new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL)
                .showAndWait()
                .filter(b -> b == ButtonType.OK)
                .isPresent();
    }
}