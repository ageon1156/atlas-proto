# Meshtastic Android - Quick Start Guide

This is a quick reference guide to help you understand and navigate the Meshtastic Android codebase. For comprehensive details, see [ARCHITECTURE.md](ARCHITECTURE.md) and [HOW_IT_WORKS.md](HOW_IT_WORKS.md).

## 📁 Project Structure

```
meshtastic-android/
├── app/                          # Main Android application
│   ├── service/                  # MeshService, handlers, managers
│   ├── ui/                       # Main activity and navigation
│   └── repository/               # Device communication layer
├── core/                         # Shared library modules
│   ├── data/                     # Repositories and data sources
│   ├── database/                 # Room DB, DAOs, entities
│   ├── model/                    # Domain models
│   ├── ui/                       # Shared UI components
│   ├── strings/                  # Centralized string resources
│   └── proto/                    # Protocol buffer definitions
├── feature/                      # Feature modules (self-contained)
│   ├── messaging/                # Chat interface
│   ├── map/                      # Node map visualization
│   ├── node/                     # Node list and details
│   ├── settings/                 # Configuration screens
│   └── firmware/                 # Firmware updates
└── build-logic/                  # Custom Gradle plugins
```

## 🏗️ Architecture at a Glance

**Pattern:** MVVM + Repository + Reactive Flow

```
┌─────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose + Material 3)   │
│  - Observes StateFlow                       │
│  - Displays state, sends events             │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│  ViewModel Layer (@HiltViewModel)           │
│  - Transforms data to UI state              │
│  - Handles user events                      │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│  Repository Layer (@Singleton)              │
│  - Single source of truth                   │
│  - Exposes StateFlow<Data>                  │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│  Data Layer                                  │
│  - Room Database (persistence)              │
│  - DataStore (preferences)                  │
│  - MeshService (device communication)       │
└─────────────────────────────────────────────┘
```

## 🔄 Data Flow Overview

### Receiving Messages from Mesh

```
Mesh Radio Device
    ↓ (Bluetooth/WiFi/USB)
RadioInterfaceService
    ↓ (Raw bytes)
MeshMessageProcessor
    ↓ (Parse protobuf)
FromRadioPacketHandler
    ↓ (Route by type)
PacketHandler / NodeManager
    ↓ (Process & persist)
Repository (NodeRepository, PacketRepository)
    ↓ (StateFlow emission)
ViewModel
    ↓ (UI state)
Composable UI
```

### Sending Messages to Mesh

```
User Input in Composable
    ↓
ViewModel.sendMessage()
    ↓
Repository.send()
    ↓
MeshService.send()
    ↓
MeshCommandSender (build protobuf)
    ↓
RadioInterfaceService
    ↓ (Bluetooth/WiFi/USB)
Mesh Radio Device → Broadcast to mesh
```

## 🔑 Key Components

| Component | Purpose | Location |
|-----------|---------|----------|
| **MeshService** | Foreground service managing mesh connectivity | `app/service/MeshService.kt` |
| **RadioInterfaceService** | Device connection abstraction (BLE/Network/USB) | `app/repository/radio/` |
| **MeshMessageProcessor** | Parses incoming protobuf messages | `app/service/MeshMessageProcessor.kt` |
| **NodeRepository** | Manages mesh nodes (single source of truth) | `core/data/repository/NodeRepository.kt` |
| **PacketRepository** | Manages messages and telemetry | `core/data/repository/PacketRepository.kt` |
| **RadioConfigRepository** | Device configuration management | `core/data/repository/RadioConfigRepository.kt` |

## 🛠️ Essential Build Commands

```bash
# Clean build (always start here for reliability)
./gradlew clean

# Format code (REQUIRED before committing)
./gradlew spotlessApply

# Static analysis
./gradlew detekt

# Build debug APKs (both flavors)
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

## 📦 Module Dependencies

```
feature:messaging
    ↓ depends on
core:data (NodeRepository, PacketRepository)
    ↓ depends on
core:database (Room DAOs)
    ↓ depends on
core:model (Domain models)
```

All feature modules are independent and can be built/tested separately.

## 🔌 Device Communication

The app supports three connection types:

1. **Bluetooth (Primary)**
   - BLE GATT connection
   - Service UUID: Meshtastic custom
   - TX/RX characteristics

2. **Network/WiFi**
   - TCP socket connection
   - For WiFi-enabled radios

3. **USB Serial**
   - USB CDC (Communication Device Class)
   - Direct wired connection

All three use Protocol Buffers for data serialization.

## 🎨 UI Development

**Framework:** Jetpack Compose with Material 3

**String Resources:**
```kotlin
// ❌ DON'T use app/res/values/strings.xml
// ✅ DO use Compose Multiplatform Resources

import org.jetbrains.compose.resources.stringResource
import org.meshtastic.core.strings.Res
import org.meshtastic.core.strings.your_string_key

Text(text = stringResource(Res.string.your_string_key))
```

**ViewModel Pattern:**
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {
    val uiState: StateFlow<UiState> = repository.data
        .map { UiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
}
```

## 🧪 Testing Strategy

| Test Type | Location | Command |
|-----------|----------|---------|
| Unit Tests | `src/test/` | `./gradlew test` |
| Instrumented Tests | `src/androidTest/` | `./gradlew connectedAndroidTest` |
| UI Tests | `src/androidTest/` with `ComposeTestRule` | Same as above |

## 🚀 Feature Development Workflow

1. **Explore:** Understand the relevant modules and dependencies
2. **Plan:** Decide which module(s) need changes (core vs feature)
3. **Implement:** 
   - Add strings to `core/strings`
   - Add dependencies to `gradle/libs.versions.toml`
   - Write code following MVVM pattern
4. **Verify:**
   - `./gradlew spotlessApply` (Essential!)
   - `./gradlew detekt`
   - Run relevant tests
5. **Commit:** Changes will be committed automatically via tooling

## 📚 Further Reading

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Deep dive into architecture, patterns, and design decisions
- **[HOW_IT_WORKS.md](HOW_IT_WORKS.md)** - Detailed workflows for all major features
- **[AGENTS.md](AGENTS.md)** - Development guidelines for AI agents
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines
- **[debugging-android.md](debugging-android.md)** - Debugging tips and tricks

## 🤝 Getting Help

- **Discord:** [discord.gg/meshtastic](https://discord.gg/meshtastic)
- **Forum:** [github.com/orgs/meshtastic/discussions](https://github.com/orgs/meshtastic/discussions)
- **Issues:** [github.com/meshtastic/Meshtastic-Android/issues](https://github.com/meshtastic/Meshtastic-Android/issues)

## 🔐 Build Flavors

| Flavor | Purpose | Includes |
|--------|---------|----------|
| **google** | Google Play Store | Google Maps, Firebase, Crashlytics |
| **fdroid** | F-Droid (FOSS) | OSMDroid, no proprietary services |

Build commands use flavor: `./gradlew assembleGoogleDebug` or `./gradlew assembleFdroidRelease`

---

**Quick Tip:** Start with [ARCHITECTURE.md](ARCHITECTURE.md) for the big picture, then dive into [HOW_IT_WORKS.md](HOW_IT_WORKS.md) for specific feature implementations.
