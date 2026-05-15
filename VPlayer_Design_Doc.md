# VPlayer Design Document

## 1. Overview
VPlayer is a premium, offline-first desktop video player built with Java 17 and JavaFX. It leverages VLCJ for high-performance media playback and FFmpeg for advanced media operations like GIF creation.

## 2. Technical Stack
- **Language**: Java 17
- **UI Framework**: JavaFX 17+
- **Media Engine**: VLCJ 4.x + VLCJ-JavaFX (PixelBuffer rendering)
- **Database**: SQLite (via JDBC)
- **Icons**: Ikonli (FontAwesome / Material Design)
- **Media Processing**: FFmpeg (via ProcessBuilder)
- **Build System**: Maven
- **Styling**: Vanilla CSS (Glassmorphism / Dark Theme)

## 3. Architecture
The application follows a modular Service-Controller-Repository pattern.

### 3.1 Core Modules
- `com.vplayer.app`: Application lifecycle and configuration.
- `com.vplayer.player`: VLCJ wrapper and playback logic.
- `com.vplayer.ui`: JavaFX Controllers and FXML views.
- `com.vplayer.database`: SQLite connection and data access objects.
- `com.vplayer.services`: Business logic (Library scanning, Vault, FFmpeg).
- `com.vplayer.models`: Data structures.

## 4. Key Features Implementation

### 4.1 Playback
- **Rendering**: `ImageViewVideoSurface` from VLCJ-JavaFX to render video directly onto a JavaFX `ImageView`.
- **Controls**: Custom overlay controls with volume boost (>100% using VLC volume scale).
- **Shortcuts**: Global `EventHandler` on the main scene.

### 4.2 Subtitles & Audio
- Multi-track support via VLCJ `MediaPlayer.subtitles()` and `MediaPlayer.audio()`.
- Dynamic CSS-based styling for subtitle overlays if rendered separately, or native VLC rendering for simplicity.

### 4.3 Media Library
- **Scanner**: Recursive file system walker with support for MP4, MKV, AVI, etc.
- **Thumbnails**: Extract frames using FFmpeg or VLCJ's thumbnailer.
- **Persistence**: SQLite storage for video metadata and resume positions.

### 4.4 Private Vault
- **Mechanism**: Move files to a hidden application data folder (`.vplayer/vault/`).
- **Access**: PIN-protected UI. Metadata stored in a separate encrypted-key SQLite table.

### 4.5 GIF & Screenshots
- **Screenshots**: VLCJ's `saveSnapshot` method.
- **GIFs**: FFmpeg command: `ffmpeg -ss [start] -t [duration] -i [input] -vf "fps=10,scale=320:-1:flags=lanczos" -c:v gif [output]`.

## 5. UI/UX Design
- **Theme**: Premium Dark (Background: #121212, Primary: #00ADB5, Text: #EEEEEE).
- **Layout**: 
  - Left: Collapsible Sidebar (Library, Playlists, Vault, Settings).
  - Center: Content Area (Grid of videos or Player).
  - Bottom: Contextual Playback Bar (only visible during playback).

## 6. Project Structure
```
VPlayer/
├── pom.xml
├── src/main/java/com/vplayer/
│   ├── Main.java
│   ├── app/
│   ├── player/
│   ├── ui/
│   ├── models/
│   ├── database/
│   ├── services/
│   ├── utils/
│   └── ...
├── src/main/resources/
│   ├── css/
│   ├── fxml/
│   └── icons/
```

## 7. Roadmap
1. Setup Maven & Basic Window.
2. Implement VLCJ Player Integration.
3. Build Media Library & SQLite persistence.
4. Add Playback Controls & Shortcuts.
5. Implement Subtitle & Audio management.
6. Add Vault, GIF Creator, and Screenshots.
7. Final UI Polish & Testing.
