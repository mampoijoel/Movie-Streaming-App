# StreamBox – JavaFX Movie Streaming Demo

**Course:** B/DIIM3110 – Interactive Multimedia  
**Project:** Group Assignment

StreamBox is a JavaFX movie streaming application inspired by Netflix. It provides a user-friendly interface where users can browse movies by category and watch trailers directly inside the application using YouTube.

The application is developed using JavaFX 21. Trailers stream from YouTube, so no video files are stored locally.

## 1. Setup (VS Code – No Build Tools)

1. Install **JDK 17 or later** and the VS Code **Extension Pack for Java**.
2. Download the **JavaFX 21 SDK** from [Gluon JavaFX](https://gluonhq.com/products/javafx/).
3. Copy the JavaFX SDK contents into the `javafx-sdk/` folder.
   - You should have files such as `javafx-sdk/lib/javafx.base.jar`.
   - See `javafx-sdk/README.txt` for more information.
   - The SDK is Git-ignored, so each group member must download it separately.
4. Open the project folder in VS Code.
5. Wait for Java to load, then press **F5** to run the application.

If red squiggly lines remain under JavaFX imports, press `Ctrl + Shift + P`, select **Java: Clean Java Language Server Workspace**, and restart VS Code.

An internet connection is required to stream trailers.

### Application Controls
- Click a movie thumbnail to play its trailer.
- Press Space to play or pause.
- Press Esc or click Back to close the player.
- Use the arrow keys or horizontal scrolling to browse movie categories.

## 2. Project Structure

```text
Movie-Streaming-App/
│
├── .gitignore
├── README.md
├── javafx-sdk/
│   └── README.txt
│
└── src/
    ├── com/streamdemo/
    │   ├── MainApp.java
    │   │
    │   ├── model/
    │   │   ├── Movie.java
    │   │   ├── Category.java
    │   │   └── Catalog.java
    │   │
    │   ├── ui/
    │   │   ├── HomeView.java
    │   │   ├── HeroBanner.java
    │   │   ├── NavBar.java
    │   │   ├── CategoryRow.java
    │   │   ├── MovieCard.java
    │   │   ├── CustomButton.java
    │   │   ├── SeekBar.java
    │   │   ├── LoadingAnimation.java
    │   │   ├── PlayerView.java
    │   │   └── Icons.java
    │   │
    │   ├── player/
    │   │   └── PlayerServer.java
    │   │
    │   └── audio/
    │       └── SoundManager.java
    │
    ├── player.html
    └── style.css
```

## 3. How the Project Meets the Requirements

| Requirement | Implementation |
|---|---|
| Three or more movie categories | Catalog and CategoryRow |
| At least eight movies per category | Catalog |
| Movie thumbnails and titles | MovieCard |
| Hover effects | MovieCard |
| Five visible thumbnails at a time | CategoryRow |
| Horizontal scrolling | CategoryRow |
| Trailer playback inside the application | PlayerView, PlayerServer and player.html |
| Play and pause controls | PlayerView |
| Custom loading animation | LoadingAnimation |
| Custom buttons | CustomButton |
| Sound effects | SoundManager |

## 4. How the Video Player Works

YouTube blocks some embedded videos when they have no web origin. To solve this, PlayerServer creates a small local server that serves `player.html` from localhost.

The HTML page loads the YouTube IFrame API with its default controls hidden.

Java communicates with the webpage using `WebEngine.executeScript()` to control playback, pause and seek functions.

The webpage also communicates with Java through the `javaBridge` object.

The loading animation remains visible until YouTube reports that playback has started. If an error occurs or the video takes longer than 20 seconds to load, a fallback button allows users to open the trailer on YouTube.

## 5. Changing a Movie Trailer

To change a movie trailer:

1. Open `Catalog.java`.
2. Locate the movie you want to update.
3. Replace its YouTube video ID with the ID of another trailer.
4. Save the file and run the application again.

The YouTube ID is the part that appears after `watch?v=` in a YouTube video link.

Some YouTube videos do not allow playback inside other applications. If this happens, use another trailer upload.

## 6. GitHub Collaboration and Workflow

The project is managed using Git and GitHub to support group collaboration.

The `main` branch is protected. Members should work using individual feature branches and submit pull requests when their work is ready for review.

### Suggested Group Task Distribution

- **Member A:** Movie model, CategoryRow and MovieCard.
- **Member B:** Hover effects, CustomButton, LoadingAnimation and CSS/theme.
- **Member C:** PlayerView, PlayerServer and player.html.
- **Member D:** SoundManager, NavBar, HeroBanner and README documentation.

Each member should:
- Make regular commits with meaningful messages.
- Work