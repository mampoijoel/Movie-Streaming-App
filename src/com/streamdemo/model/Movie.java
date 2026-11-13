package com.streamdemo.model;

/**
 * A single movie in the catalogue.
 * Only the YouTube video id is stored - no video assets live in the project.
 *
 * @param title      movie name shown under the thumbnail
 * @param year       release year
 * @param youtubeId  id of the official trailer on YouTube (the part after "v=")
 * @param blurb      one-line description used by the featured banner
 */
public record Movie(String title, int year, String youtubeId, String blurb) {

    /** 16:9 thumbnail (320x180) served by YouTube. */
    public String thumbnailUrl() {
        return "https://img.youtube.com/vi/" + youtubeId + "/mqdefault.jpg";
    }

    /** Large backdrop (1280x720) used by the hero banner. */
    public String heroUrl() {
        return "https://img.youtube.com/vi/" + youtubeId + "/maxresdefault.jpg";
    }

    /** Fallback backdrop if the max-resolution image does not exist. */
    public String fallbackHeroUrl() {
        return "https://img.youtube.com/vi/" + youtubeId + "/hqdefault.jpg";
    }

    public String watchUrl() {
        return "https://www.youtube.com/watch?v=" + youtubeId;
    }
}
