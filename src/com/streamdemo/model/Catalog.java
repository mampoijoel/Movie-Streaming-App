package com.streamdemo.model;

import java.util.List;

/**
 * Hard-coded demo catalogue: 3 genres x 8 movies.
 *
 * To swap a trailer, replace the YouTube id (the text after "watch?v=" in the
 * trailer's URL). If a trailer ever refuses to play, the owner may have
 * disabled embedding - pick another upload of the same trailer.
 */
public final class Catalog {

    private Catalog() {
    }

    public static List<Category> load() {
        return List.of(
                new Category("Action", List.of(
                        new Movie("The Dark Knight", 2008, "EXeTwQWrcwY", "Batman faces the Joker in a battle for Gotham's soul."),
                        new Movie("Mad Max: Fury Road", 2015, "hEJnMQG9ev8", "A relentless chase across the wasteland."),
                        new Movie("John Wick", 2014, "2AUmvWm5ZDQ", "A retired hitman is pulled back into the underworld."),
                        new Movie("Avengers: Endgame", 2019, "TcMBFSGVi1c", "The Avengers assemble one final time."),
                        new Movie("Gladiator", 2000, "owK1qxDselE", "A betrayed general fights for revenge in the arena."),
                        new Movie("Mission: Impossible - Fallout", 2018, "wb49-oV0F78", "Ethan Hunt races to stop a global catastrophe."),
                        new Movie("Top Gun: Maverick", 2022, "qSqVVswa420", "Maverick returns to train a new generation of pilots."),
                        new Movie("Black Panther", 2018, "xjDjIWPwcPU", "T'Challa rises as king of Wakanda.")
                )),
                new Category("Sci-Fi", List.of(
                        new Movie("Inception", 2010, "YoHD9XEInc0", "A thief steals secrets from inside people's dreams."),
                        new Movie("Interstellar", 2014, "zSWdZVtXT7E", "Explorers travel through a wormhole to save humanity."),
                        new Movie("The Matrix", 1999, "vKQi3bBA1y8", "A hacker discovers the truth about his reality."),
                        new Movie("Blade Runner 2049", 2017, "gCcx85zbxz4", "A new blade runner uncovers a long-buried secret."),
                        new Movie("Dune", 2021, "n9xhJrPXop4", "A noble family fights for control of the desert planet Arrakis."),
                        new Movie("Arrival", 2016, "tFMo3UJ4B4g", "A linguist must decode an alien language."),
                        new Movie("The Martian", 2015, "ej3ioOneTy8", "An astronaut stranded on Mars fights to survive."),
                        new Movie("Guardians of the Galaxy", 2014, "d96cjJhvlMA", "A band of misfits become the galaxy's unlikely heroes.")
                )),
                new Category("Animation", List.of(
                        new Movie("Spider-Man: Into the Spider-Verse", 2018, "g4Hbz2jLxvQ", "Miles Morales meets Spider-people from other dimensions."),
                        new Movie("Toy Story", 1995, "v-PjgYDrg70", "Toys come alive when no one is watching."),
                        new Movie("Frozen", 2013, "TbQm5doF_Uc", "Two sisters, one icy secret."),
                        new Movie("The Lion King", 1994, "4sj1MT05lAA", "A young lion prince must reclaim his kingdom."),
                        new Movie("Coco", 2017, "Rvr68u6k5sI", "A boy journeys to the Land of the Dead."),
                        new Movie("Inside Out", 2015, "yRUAzGQ3nSY", "The emotions inside a girl's mind take centre stage."),
                        new Movie("Finding Nemo", 2003, "wZdpNglLbt8", "A clownfish crosses the ocean to find his son."),
                        new Movie("Up", 2009, "ORFWdXl_zJ4", "An old man ties balloons to his house and flies away.")
                ))
        );
    }

    /** The movie shown in the big banner at the top of the home screen. */
    public static Movie featured() {
        return load().get(1).movies().get(0); // Inception
    }
}
