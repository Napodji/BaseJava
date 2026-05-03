package com.basejava.webapp.model;

public enum ContactType {
    PHONE("Тел."),
    SKYPE("Skype"),
    MAIL("Почта"),
    LINKEDIN("Профиль LinkedIn"),
    GITHUB("Профиль GitHub"),
    STACKOVERFLOW("Профиль Stackoverflow"),
    HOME_PAGE("Домашняя страница");

    private final String title;

    ContactType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String toHtml(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return switch (this) {
            case SKYPE        -> "<a href='skype:" + value + "'>" + value + "</a>";
            case MAIL         -> "<a href='mailto:" + value + "'>" + value + "</a>";
            case LINKEDIN     -> "<a href='" + value + "'>LinkedIn</a>";
            case GITHUB       -> "<a href='" + value + "'>GitHub</a>";
            case STACKOVERFLOW-> "<a href='" + value + "'>StackOverflow</a>";
            case HOME_PAGE    -> "<a href='" + value + "'>" + value + "</a>";
            default           -> value;
        };
    }
}

