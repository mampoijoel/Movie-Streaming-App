package com.streamdemo.model;

public record Movie(String title, int year, String youtubeId, String blurb) {

    public String thumbnailUrl() {
        return "https://img.youtube.com/vi/" + youtubeId + "/mqdefault.jpg";
    }

    public String heroUrl() {
        return "https://img.youtube.com/vi/" + youtubeId + "/maxresdefault.jpg";
    }

    public String fallbackHeroUrl() {
        return "https://img.youtube.com/vi/" + youtubeId + "/hqdefault.jpg";
    }

    public String watchUrl() {
        return "https://www.youtube.com/watch?v=" + youtubeId;
    }
}