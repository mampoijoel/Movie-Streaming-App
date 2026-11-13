# StreamBox - JavaFX Movie Streaming Demo

Netflix-style demo app (JavaFX 21). Trailers stream from YouTube inside the app; no video files are stored.

## Setup (VS Code, "No build tools" project)

1. Install **JDK 17+** and the VS Code *Extension Pack for Java*.
2. Download the **JavaFX 21 SDK** from https://gluonhq.com/products/javafx/ and copy its contents into the `javafx-sdk/` folder
   (you should end up with `javafx-sdk/lib/javafx.base.jar` etc.). See `javafx-sdk/README.txt`. It is git-ignored, so each member does this once.
3. Open the project folder in VS Code, wait for Java to load, then press **F5** (Run > "StreamBox").
   If red squiggles remain on `javafx` imports: Ctrl+Shift+P > "Java: Clean Java Language Server Workspace".

An internet connection is needed. Controls: click a thumbnail to play - Space play/pause - Esc or Back to close - arrows or horizontal swipe to scroll a row.

## Project layout

```
src/com/streamdemo/
  MainApp.java            app start-up, wiring, keyboard shortcuts
  model/                  Movie, Category, Catalog (3 genres x 8 movies)
  ui/
    HomeView              scrolling home page
    HeroBanner            featured banner
    NavBar                logo, category links, sound switch
    CategoryRow           labelled row, 5 visible thumbnails + smooth scroll
    MovieCard             thumbnail + title, hover effect
    CustomButton          our own button (StackPane + Label, NOT javafx Button)
    SeekBar               custom progress/seek bar
    LoadingAnimation      custom "loading trailer" animation
    PlayerView            trailer player (WebView + custom controls)
    Icons                 vector icons drawn with shapes
  player/PlayerServer     tiny localhost server for the YouTube page
  audio/SoundManager      synthesised sound effects (no audio files)
src/                      player.html (YouTube IFrame API page), style.css
```

## How the requirements are met

| Requirement | Where |
|---|---|
| 3+ categories, 8+ movies each, labelled | `Catalog`, `CategoryRow` header |
| Thumbnail + title | `MovieCard` |
| Hover effect | `MovieCard` (grow, glow, zoom, play badge, sound) |
| Only 5 visible, scroll for the rest | `CategoryRow` (card width = viewport / 5) |
| Trailer plays from YouTube inside the app | `PlayerView` + `PlayerServer` + `player.html` |
| Play / pause | `PlayerView.togglePlayback()` -> JS `streamApi` |
| Custom loading animation | `LoadingAnimation` |
| No `Button` class | `CustomButton`, arrows in `CategoryRow`, `SeekBar` |
| Sound effects | `SoundManager` (hover, click, open, close) |

## How the player works (for the demo / viva)

YouTube blocks embeds that have no web origin, so `PlayerServer` serves `player.html` from `http://localhost:<port>`.
That page loads the YouTube IFrame API with its own controls hidden. Java calls `window.streamApi.play()/pause()/seek()` via
`WebEngine.executeScript`, and the page reports state back through the `javaBridge` object (`PlayerView.Bridge`).
The loading animation stays until YouTube reports the "playing" state; if there is an error or a 20 s timeout, a
"Watch on YouTube" fallback button opens the trailer in the browser.

## Changing a trailer

Edit `Catalog.java`: replace the YouTube id (the part after `watch?v=`). If a trailer shows "owner does not allow playing inside other apps",
use another upload of that trailer.

## Git workflow (10 + 15 marks depend on this)

- `main` is protected: nobody commits to it directly. Work in feature branches, merge by pull request.
- Suggested split, so every member has real commits: **A** model + CategoryRow/MovieCard (layout, scrolling) -
  **B** hover effects, CustomButton, LoadingAnimation, CSS/theme - **C** PlayerView, PlayerServer, player.html - **D** SoundManager, NavBar, HeroBanner, README.
- Commit small and often with clear messages (`Add hover glow to MovieCard`), and make sure each member can explain the classes they own.
