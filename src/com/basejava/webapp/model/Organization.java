package com.basejava.webapp.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Organization {
    private final String name;
    private final String url;
    private final List<Position> positions;

    public Organization(String name, String url, Position... positions) {
        this(name, url, Arrays.asList(positions));
    }

    public Organization(String name, String url, List<Position> positions) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(positions, "positions must not be null");
        this.name = name;
        this.url = url;
        this.positions = positions;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public List<Position> getPositions() {
        return positions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization that = (Organization) o;
        return Objects.equals(name, that.name)
                && Objects.equals(url, that.url)
                && Objects.equals(positions, that.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, positions);
    }

    @Override
    public String toString() {
        return "Organization(" + name + ", " + positions + ")";
    }
}
