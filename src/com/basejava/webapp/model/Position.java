package com.basejava.webapp.model;

import java.util.Objects;

public class Position {
    private final String startDate;
    private final String endDate;
    private final String title;
    private final String description;

    public Position(String startDate, String endDate, String title, String description) {
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");
        Objects.requireNonNull(title, "title must not be null");
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Position(" + startDate + " - " + endDate + ", " + title + ")";
    }
}
