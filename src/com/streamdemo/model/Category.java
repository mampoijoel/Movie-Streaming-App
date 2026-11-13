package com.streamdemo.model;

import java.util.List;

/** A labelled group of movies (a genre row on the home screen). */
public record Category(String name, List<Movie> movies) {
}
