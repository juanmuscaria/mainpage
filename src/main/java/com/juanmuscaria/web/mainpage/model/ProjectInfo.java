package com.juanmuscaria.web.mainpage.model;

import java.util.Objects;

public final class ProjectInfo {
    private final String owner;
    private final String id;
    private final String displayName;
    private final String description;
    private final String lang;
    private final int stars;
    private final int forks;

    public ProjectInfo(String owner, String id, String displayName, String description, String lang, int stars, int forks) {
        this.owner = owner;
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.lang = lang;
        this.stars = stars;
        this.forks = forks;
    }

    public String owner() {
        return owner;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public String lang() {
        return lang;
    }

    public int stars() {
        return stars;
    }

    public int forks() {
        return forks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ProjectInfo) obj;
        return Objects.equals(this.owner, that.owner) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.displayName, that.displayName) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.lang, that.lang) &&
                this.stars == that.stars &&
                this.forks == that.forks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, id, displayName, description, lang, stars, forks);
    }

    @Override
    public String toString() {
        return "ProjectInfo[" +
                "owner=" + owner + ", " +
                "id=" + id + ", " +
                "displayName=" + displayName + ", " +
                "description=" + description + ", " +
                "lang=" + lang + ", " +
                "stars=" + stars + ", " +
                "forks=" + forks + ']';
    }

}
