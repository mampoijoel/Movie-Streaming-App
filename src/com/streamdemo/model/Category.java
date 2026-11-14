package com.streamdemo.model;

import java.util.List;

public record Category(String name, List<Movie> movies) {
}