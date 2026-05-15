# VPlayer - Premium Media Player

VPlayer is a modern, premium video player built with JavaFX and VLCJ. It offers a sleek interface, secure vault storage, and intelligent playback features.

## 🚀 Features

- **Premium UI**: Dark-themed, glassmorphic design with smooth transitions and animations.
- **VLC-Powered Playback**: Robust support for all major video formats via the powerful libVLC engine.
- **Smart Resume**: Automatically asks to resume from where you left off or start over.
- **Video Vault**: Securely store and play videos in a password-protected vault.
- **Advanced Controls**:
  - Precision seeking (10s skip).
  - Multi-speed playback (0.25x to 4x).
  - Subtitle and Audio track selection.
  - Fullscreen and window management.
  - Keyboard shortcuts (Space for Play/Pause, Arrows for seeking, etc.).
- **Automatic Progress Tracking**: Your playback position is saved automatically.

## 🛠️ Technology Stack

- **Java 17+**
- **JavaFX**: For the modern user interface.
- **VLCJ**: Java wrapper for libVLC.
- **SQLite**: For database management and progress tracking.
- **Maven**: For dependency management.

## 📋 Prerequisites

Before running VPlayer, ensure you have the following installed:

1. **Java Development Kit (JDK) 17** or higher.
2. **VLC Media Player (64-bit)**: VPlayer requires the native libVLC libraries to be installed on your system.
3. **Maven**: For building the project.

## ⚙️ Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/V_Player.git
   cd V_Player
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.vplayer.Main"
   ```

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
| :--- | :--- |
| `SPACE` | Play / Pause |
| `LEFT ARROW` | Skip Backward (10s) |
| `RIGHT ARROW` | Skip Forward (10s) |
| `UP ARROW` | Increase Volume |
| `DOWN ARROW` | Decrease Volume |
| `F` | Toggle Fullscreen |
| `M` | Mute |

## 📁 Project Structure

```text
V_Player/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/vplayer/
│   │   │       ├── app/            # Application entry point
│   │   │       ├── database/       # DB Management & Repositories
│   │   │       ├── models/         # Data models
│   │   │       ├── player/         # VLCJ implementation & Surface
│   │   │       ├── services/       # Business logic (Vault, Library, etc.)
│   │   │       └── ui/             # JavaFX Controllers
│   │   └── resources/
│   │       ├── css/                # Styling
│   │       └── fxml/               # UI Layouts
└── pom.xml                         # Maven dependencies
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License.
