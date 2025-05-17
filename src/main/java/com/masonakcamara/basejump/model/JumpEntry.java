package com.masonakcamara.basejump.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jump_entries")
public class JumpEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jump_datetime", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private double height;

    @Column(nullable = false)
    private String container;

    @Column(name = "main_parachute", nullable = false)
    private String mainParachute;

    @Column(name = "pilot_chute", nullable = false)
    private String pilotChute;

    @Enumerated(EnumType.STRING)
    @Column(name = "slider_position", nullable = false)
    private SliderPosition sliderPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "jump_type", nullable = false)
    private JumpType jumpType;

    @Column(name = "media_link")
    private String mediaLink;

    public JumpEntry() {
    }

    public JumpEntry(LocalDateTime dateTime,
                     String objectName,
                     double latitude,
                     double longitude,
                     double height,
                     String container,
                     String mainParachute,
                     String pilotChute,
                     SliderPosition sliderPosition,
                     JumpType jumpType,
                     String mediaLink) {
        this.dateTime = dateTime;
        this.objectName = objectName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.container = container;
        this.mainParachute = mainParachute;
        this.pilotChute = pilotChute;
        this.sliderPosition = sliderPosition;
        this.jumpType = jumpType;
        this.mediaLink = mediaLink;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getMainParachute() {
        return mainParachute;
    }

    public void setMainParachute(String mainParachute) {
        this.mainParachute = mainParachute;
    }

    public String getPilotChute() {
        return pilotChute;
    }

    public void setPilotChute(String pilotChute) {
        this.pilotChute = pilotChute;
    }

    public SliderPosition getSliderPosition() {
        return sliderPosition;
    }

    public void setSliderPosition(SliderPosition sliderPosition) {
        this.sliderPosition = sliderPosition;
    }

    public JumpType getJumpType() {
        return jumpType;
    }

    public void setJumpType(JumpType jumpType) {
        this.jumpType = jumpType;
    }

    public String getMediaLink() {
        return mediaLink;
    }

    public void setMediaLink(String mediaLink) {
        this.mediaLink = mediaLink;
    }
}