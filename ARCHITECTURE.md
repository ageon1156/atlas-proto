# Meshtastic Android Architecture

This document provides a comprehensive overview of the Meshtastic Android application architecture, explaining how the code is organized and how the major components interact.

## Table of Contents

1. [Overview](#overview)
2. [Architecture Pattern](#architecture-pattern)
3. [Module Structure](#module-structure)
4. [Data Flow](#data-flow)
5. [Key Components](#key-components)
6. [Communication Layer](#communication-layer)
7. [Dependency Injection](#dependency-injection)
8. [Build Flavors](#build-flavors)

## Overview

Meshtastic Android is a native Android client application for communicating with Meshtastic mesh radio devices. It enables users to send messages, share locations, and configure devices over a decentralized mesh network using LoRa radios.

**Key Technologies:**
- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM + Repository Pattern
- **DI:** Hilt (Dagger-based)
- **Database:** Room
- **Async:** Kotlin Coroutines & Flow
- **Communication Protocol:** Protocol Buffers (Protobuf)

## Architecture Pattern

The application follows **Model-View-ViewModel (MVVM)** architecture combined with the **Repository Pattern** and **reactive programming** using Kotlin Flow.

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                    │
│  - Composables observe ViewModels                           │
│  - Material 3 components                                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ observes StateFlow/Flow
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                    ViewModel Layer                           │
│  - @HiltViewModel annotated                                 │
│  - Exposes UI state as StateFlow                            │
│  - Business logic & UI state transformations                │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ calls methods
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                   Repository Layer                           │
│  - @Singleton data sources                                  │
│  - Aggregates data from multiple sources                    │
│  - Exposes StateFlow for reactive updates                   │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ reads/writes
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                     Data Layer                               │
│  - Room Database (local persistence)                        │
│  - DataStore (preferences)                                  │
│  - RadioInterfaceService (device communication)             │
└─────────────────────────────────────────────────────────────┘
```

### Design Principles

1. **Unidirectional Data Flow (UDF):** Data flows down from repositories to ViewModels to UI, events flow up
2. **Single Source of Truth:** Each piece of data has one authoritative source
3. **Separation of Concerns:** Clear boundaries between UI, business logic, and data layers
4. **Dependency Inversion:** Higher-level modules don't depend on lower-level modules; both depend on abstractions
5. **Reactive Programming:** Components react to state changes rather than polling

## Module Structure

The project is organized into three main categories:

### Core Modules (`core/`)

Shared library modules providing common functionality across the app:

| Module | Purpose |
|--------|---------|
| `core:analytics` | Analytics tracking (Google-flavor specific) |
| `core:common` | Shared utilities, extensions, helpers |
| `core:data` | Repositories, data sources, business logic |
| `core:database` | Room database, DAOs, entities, migrations |
| `core:datastore` | Preferences using DataStore |
| `core:di` | Hilt dependency injection modules |
| `core:model` | Domain models, data classes |
| `core:navigation` | Navigation routes and type-safe arguments |
| `core:network` | HTTP networking, MQTT client |
| `core:prefs` | Preference data classes and managers |
| `core:proto` | Protocol Buffer definitions (Git submodule) |
| `core:service` | Service-related interfaces and state |
| `core:strings` | Centralized string resources (Compose Multiplatform Resources) |
| `core:ui` | Shared UI components, themes, ViewModels |

### Feature Modules (`feature/`)

Independent, self-contained features with their own ViewModels and UI:

| Module | Purpose |
|--------|---------|
| `feature:intro` | App introduction/onboarding screens |
| `feature:messaging` | Chat interface, message history |
| `feature:map` | Node map visualization (Google Maps or OSMDroid) |
| `feature:node` | Node list, details, and metrics |
| `feature:settings` | Radio configuration, app settings |
| `feature:firmware` | Device firmware updates |
| `feature:emergency` | Emergency broadcast features |
| `feature:sos` | SOS messaging functionality |

### Main Application Module (`app/`)

Contains the Android application components:
- `MainActivity` - Entry point activity
- `MeshService` - Foreground service managing mesh connectivity
- Service handlers and managers (packet processing, node management)
- AIDL interface definitions

### Build Logic (`build-logic/`)

Custom Gradle convention plugins for consistent build configuration across modules.

## Data Flow

### From Mesh Radio Device → UI

```
┌─────────────────────────────────────────────────────────────┐
│  Mesh Radio Device (Bluetooth/WiFi/USB)                     │
└──────────────────────┬──────────────────────────────────────┘
                       │ Raw byte stream
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  RadioInterfaceService (@Singleton)                         │
│  - BluetoothRepository / NetworkRepository / UsbRepository  │
│  - Emits receivedData SharedFlow                            │
└──────────────────────┬──────────────────────────────────────┘
                       │ Raw bytes
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  MeshMessageProcessor                                        │
│  - Parses protobuf (FromRadio)                              │
│  - Handles early packet buffering                           │
│  - Logs packets to MeshLog                                  │
└──────────────────────┬──────────────────────────────────────┘
                       │ FromRadio proto
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  FromRadioPacketHandler                                      │
│  - Routes by packet type (config, nodeInfo, user, position) │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┬──────────────┐
        │              │              │              │
┌───────▼───────┐ ┌────▼────┐ ┌──────▼──────┐ ┌─────▼─────┐
│ ConfigHandler │ │ Router  │ │ PacketHandler│ │ NodeMgr   │
└───────┬───────┘ └────┬────┘ └──────┬──────┘ └─────┬─────┘
        │              │              │              │
        └──────────────┼──────────────┴──────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  Repositories (NodeRepository, PacketRepository)            │
│  - Update StateFlows                                        │
│  - Persist to Room DB                                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  Room Database                                               │
│  - NodeEntity, MyNodeEntity, Packet, etc.                   │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ StateFlow emissions
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  ViewModels                                                  │
│  - Transform to UI state                                    │
│  - Expose StateFlow<UiState>                                │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  UI (Jetpack Compose)                                       │
│  - Observes StateFlow                                       │
│  - Renders UI based on state                                │
└─────────────────────────────────────────────────────────────┘
```

### From UI → Mesh Radio Device

```
┌─────────────────────────────────────────────────────────────┐
│  UI (Jetpack Compose)                                       │
│  - User action (send message, change config)                │
└──────────────────────┬──────────────────────────────────────┘
                       │ Event
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  ViewModel                                                   │
│  - Validates input                                          │
│  - Calls repository method                                  │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  Repository / ServiceRepository                              │
│  - Calls MeshService via AIDL                               │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  MeshService                                                 │
│  - MeshCommandSender builds protobuf                        │
│  - Enqueues ToRadio message                                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ ToRadio proto bytes
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  RadioInterfaceService                                       │
│  - Sends via active connection                              │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  Mesh Radio Device (Bluetooth/WiFi/USB)                     │
└─────────────────────────────────────────────────────────────┘
```

## Key Components

### MeshService

**Type:** Android Foreground Service  
**Scope:** @AndroidEntryPoint  
**Lifecycle:** Started on device connection, runs in foreground with notification

**Responsibilities:**
- Coordinates all mesh-related operations
- Manages RadioInterfaceService lifecycle
- Hosts MeshRouter, handlers, and managers
- Exposed to UI via AIDL interface (`IMeshService`)
- Ensures mesh connectivity survives UI lifecycle

**Key Components:**
- `MeshRouter` - Central orchestrator for device state
- `MeshNodeManager` - In-memory node cache
- `MeshMessageProcessor` - Parses incoming protobuf messages
- `MeshCommandSender` - Builds and sends commands to device
- `FromRadioPacketHandler` - Routes incoming packets
- `PacketHandler` - Processes data packets (messages)
- Various managers: Connection, History, Location, MQTT, Traceroute

### RadioInterfaceService

**Type:** Singleton Service  
**Scope:** @Singleton

**Responsibilities:**
- Abstracts device communication layer
- Manages Bluetooth, WiFi, and USB connections
- Emits raw byte streams via SharedFlow
- Handles connection state and errors

**Implementations:**
- `BluetoothRepository` - Bluetooth Low Energy (BLE) connection
- `NetworkRepository` - WiFi/network connection
- `UsbRepository` - USB serial connection

### Repositories

**Type:** Data Layer Singletons  
**Scope:** @Singleton

**Key Repositories:**

#### NodeRepository
- **Purpose:** Manages node information (devices in the mesh)
- **Provides:** `StateFlow` for myNodeInfo, ourNodeInfo, nodeList, nodeDBbyNum
- **Sources:** Room DB + in-memory cache (MeshNodeManager)

#### PacketRepository
- **Purpose:** Manages message/packet persistence
- **Provides:** Paging support for message history
- **Sources:** Room DB (PacketDao)

#### ServiceRepository
- **Purpose:** Manages connection to MeshService
- **Provides:** IMeshService binding, connection state, command interface
- **Handles:** Service lifecycle, reconnection, traceroute

#### RadioConfigRepository
- **Purpose:** Manages device configuration
- **Provides:** StateFlow for all radio settings (LoRa, Channel, Module configs)
- **Sources:** DataStore + device config

### ViewModels

**Type:** UI State Managers  
**Scope:** @HiltViewModel

ViewModels transform repository data into UI state and handle user events.

**Examples:**
- `MessageViewModel` - Chat interface state
- `NodeListViewModel` - Node list display
- `RadioConfigViewModel` - Configuration screens
- `MapViewModel` - Map visualization

**Pattern:**
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {
    val uiState: StateFlow<UiState> = repository.data
        .map { data -> UiState.Success(data) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
}
```

### Room Database

**Purpose:** Local data persistence  
**Version:** 32 (with auto-migrations from v3)

**Key Entities:**
- `NodeEntity` - Mesh nodes information
- `MyNodeEntity` - Local device info
- `Packet` - Messages and telemetry
- `ContactSettings` - Per-contact preferences
- `MeshLog` - Debugging logs
- `QuickChatAction` - Quick reply templates
- `ReactionEntity` - Message reactions

**Key DAOs:**
- `NodeInfoDao` - Node CRUD operations
- `PacketDao` - Message queries with paging
- `MeshLogDao` - Log management

## Communication Layer

The app supports three connection types to mesh radios:

### Bluetooth (Primary)

- **Technology:** Bluetooth Low Energy (BLE)
- **Service UUID:** Meshtastic custom GATT service
- **Characteristics:** TX (write), RX (notify), FromNum (read)
- **Implementation:** `BluetoothRepository`

### Network/WiFi

- **Technology:** TCP/IP socket connection
- **Use Case:** Network-attached radios, WiFi-enabled devices
- **Implementation:** `NetworkRepository`

### USB Serial

- **Technology:** USB CDC (Communication Device Class)
- **Use Case:** Direct wired connection to radio
- **Implementation:** `UsbRepository`

All three implementations emit data through the same `RadioInterfaceService` interface, providing a unified abstraction layer.

### Protocol Buffers

Communication uses Protocol Buffers (protobuf) for efficient serialization:

- **ToRadio:** Commands sent from app to device
- **FromRadio:** Data received from device to app
- **MeshPacket:** Over-the-air packet format

Protobuf definitions are in `core:proto` module (Git submodule from meshtastic/protobufs).

## Dependency Injection

The app uses **Hilt** (Dagger-based) for dependency injection.

### Scopes

| Annotation | Scope | Lifetime |
|------------|-------|----------|
| `@Singleton` | Application | App lifecycle |
| `@ActivityScoped` | Activity | Activity lifecycle |
| `@ViewModelScoped` | ViewModel | ViewModel lifecycle |

### Key Entry Points

- `@HiltAndroidApp` - `MeshUtilApplication` (Application class)
- `@AndroidEntryPoint` - `MainActivity`, `MeshService`
- `@HiltViewModel` - All feature ViewModels

### Module Organization

Modules are defined in `core:di`:
- **AppModule** - Application-level singletons
- **DatabaseModule** - Room DB provision
- **NetworkModule** - Networking dependencies
- **DispatcherModule** - Coroutine dispatchers

### Lazy Injection

Used to break circular dependencies:
```kotlin
@Inject lateinit var lazyRepository: Lazy<Repository>

// Access only when needed
val repository = lazyRepository.get()
```

## Build Flavors

The app has two product flavors for different distribution channels:

### Google Flavor

**Target:** Google Play Store  
**Includes:**
- Google Play Services
- Google Maps for mapping
- Firebase Crashlytics
- DataDog analytics

### F-Droid Flavor

**Target:** F-Droid (FOSS repository)  
**Includes:**
- OSMDroid for mapping (open-source alternative)
- No proprietary services
- No analytics/crashlytics

**Flavor-Specific Code:**
- `src/google/` - Google-specific implementations
- `src/fdroid/` - F-Droid-specific implementations
- Common code in `src/main/`

**Build Variants:**
- `googleDebug`, `googleRelease`
- `fdroidDebug`, `fdroidRelease`

## Threading & Coroutines

All asynchronous operations use Kotlin Coroutines:

- **Dispatchers.Main** - UI operations
- **Dispatchers.IO** - Network, database, file I/O
- **Dispatchers.Default** - CPU-intensive operations

ViewModels use `viewModelScope` which automatically cancels coroutines when the ViewModel is cleared.

## Testing Strategy

- **Unit Tests:** JUnit tests in `src/test/`
- **Instrumented Tests:** Android tests in `src/androidTest/`
- **UI Tests:** Compose UI testing with `ComposeTestRule`

## Further Reading

- [HOW_IT_WORKS.md](HOW_IT_WORKS.md) - Detailed component interactions
- [CONTRIBUTING.md](CONTRIBUTING.md) - Development guidelines
- [AGENTS.md](AGENTS.md) - AI agent development guide
- [debugging-android.md](debugging-android.md) - Debugging tips

---

For questions or clarifications, please open a discussion on the [Meshtastic Forums](https://github.com/orgs/meshtastic/discussions) or join the [Discord](https://discord.gg/meshtastic).
