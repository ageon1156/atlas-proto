# How Meshtastic Android Works

This document provides a detailed walkthrough of how the Meshtastic Android application works, explaining the key workflows and component interactions.

## Table of Contents

1. [Application Startup](#application-startup)
2. [Device Connection](#device-connection)
3. [Message Flow](#message-flow)
4. [Node Discovery](#node-discovery)
5. [Configuration Management](#configuration-management)
6. [Location Sharing](#location-sharing)
7. [Firmware Updates](#firmware-updates)
8. [Background Operations](#background-operations)

## Application Startup

### 1. App Launch Sequence

```
1. MeshUtilApplication.onCreate()
   ├─ Initialize Hilt DI
   ├─ Initialize maps (platform-specific)
   ├─ Schedule MeshLog cleanup worker
   └─ Async: Initialize DatabaseManager with device address

2. MainActivity.onCreate()
   ├─ Install splash screen
   ├─ Enable edge-to-edge display
   ├─ Set Compose content
   │  ├─ Observe theme preference
   │  ├─ Apply OrganicMeshtasticTheme
   │  └─ Check app intro completion
   ├─ Inject MeshServiceClient
   └─ Inject UiPreferencesDataSource

3. setContent { }
   ├─ Show AppIntroductionScreen (first launch)
   └─ Or show MainScreen (subsequent launches)
```

### 2. Service Initialization

When `MainScreen` is displayed:

```kotlin
// MeshServiceClient automatically binds in onCreate()
MeshServiceClient.serviceConnection
    ├─ Context.bindService(Intent(IMeshService))
    ├─ Wait for onServiceConnected()
    └─ Emit connectionState = CONNECTED
```

If MeshService is not running, it starts on demand when:
- User taps "Connect" to a device
- App receives a USB intent
- Boot complete (if previously connected)

## Device Connection

### Bluetooth Connection Workflow

```
User selects device from list
    ↓
UIViewModel.setDeviceAddress(address)
    ↓
RadioConfigRepository.setDeviceAddress(address)
    ↓
Triggers MeshService to start (if not running)
    ↓
MeshService.onCreate()
    ├─ Initialize all handlers and managers
    ├─ Create RadioInterfaceService
    └─ Start foreground with notification
    ↓
RadioInterfaceService.connect(address)
    ↓
BluetoothRepository.connect()
    ├─ Get BluetoothDevice from address
    ├─ Create GATT connection
    ├─ Discover services
    ├─ Enable notifications on RX characteristic
    └─ Emit connectionState = CONNECTED
    ↓
Start listening for data
    ├─ RX characteristic notifications
    ├─ Parse bytes into protobuf
    └─ Emit to receivedData SharedFlow
    ↓
MeshService starts processing incoming data
```

### Connection State Flow

```kotlin
RadioInterfaceService.connectionState: StateFlow<ConnectionState>
    ↓ observed by
ServiceRepository.connectionState
    ↓ observed by
ViewModels (UIViewModel, NodeListViewModel, etc.)
    ↓ observed by
UI composables
    └─ Display connection status icon/banner
```

**ConnectionState enum:**
- `DISCONNECTED` - Not connected to any device
- `CONNECTED` - Successfully connected and communicating
- `DEVICE_SLEEP` - Device is in low-power mode
- `DEVICE_DISCONNECTED` - Device was connected but lost connection

## Message Flow

### Sending a Message

```
User types message in MessagingScreen
    ↓
MessageViewModel.sendMessage(text, destination)
    ↓
PacketRepository.sendMessage()
    ↓
ServiceRepository.send()
    ↓ AIDL call
MeshService.send()
    ↓
MeshCommandSender.sendMessage()
    ├─ Build MeshPacket protobuf
    │  ├─ Set id = generatePacketId()
    │  ├─ Set to = destination node
    │  ├─ Set wantAck = true
    │  ├─ Encrypt payload
    │  └─ Set channel = selectedChannel
    ├─ Wrap in ToRadio protobuf
    └─ Send to RadioInterfaceService.sendBytes()
    ↓
RadioInterfaceService.sendBytes()
    ├─ Write to TX characteristic (BLE)
    │ Or send via TCP socket (Network)
    │ Or write to USB serial (USB)
    └─ Emit to meshPacketOutgoingFlow
    ↓
PacketHandler.handleOutgoingPacket()
    ├─ Save to database as QUEUED
    └─ Start ACK timeout timer
    ↓
Device transmits over LoRa mesh
    ↓
Wait for ACK...
    ↓
Receive routing response (ACK)
    ↓
MeshMessageProcessor.parseFromRadio()
    ↓
FromRadioPacketHandler.handlePacket()
    ├─ Identify as ROUTING packet
    └─ Update packet status to DELIVERED
    ↓
PacketRepository updates database
    ↓
UI shows delivery checkmark
```

### Receiving a Message

```
Device receives LoRa packet from mesh
    ↓
Device sends FromRadio protobuf via BLE/Network/USB
    ↓
RadioInterfaceService.receivedData SharedFlow
    ↓
MeshMessageProcessor.parseFromRadio()
    ├─ Deserialize protobuf bytes
    ├─ Check if database is ready (nodeDBReady)
    ├─ If not ready, buffer in earlyPackets queue
    └─ Log to MeshLog database
    ↓
FromRadioPacketHandler.handlePacket()
    ├─ Route based on packet.portnum
    └─ For TEXT_MESSAGE_APP → PacketHandler
    ↓
PacketHandler.handleReceivedData()
    ├─ Decrypt payload using channel key
    ├─ Parse as Text message
    ├─ Check for duplicate (dedup by packetId)
    ├─ Extract sender node number
    └─ Create Packet entity
    ↓
PacketRepository.insert(packet)
    ├─ Save to Room DB
    └─ Trigger notification (MeshServiceNotificationsImpl)
    ↓
PacketRepository.messagesFlow emits new list
    ↓
MessageViewModel.messages StateFlow updates
    ↓
MessagingScreen recomposes with new message
    ↓
Android notification appears (if app in background)
```

## Node Discovery

Nodes are discovered through several mechanisms:

### 1. NodeInfo Broadcast

Every node periodically broadcasts `NODEINFO_APP` packets containing:
- Node number (unique ID)
- User information (short name, long name)
- Hardware model
- Role (CLIENT, ROUTER, etc.)

```
Remote node broadcasts NodeInfo
    ↓
Receive FromRadio with NODEINFO_APP
    ↓
FromRadioPacketHandler → PacketHandler
    ↓
PacketHandler.handleReceivedUser()
    ├─ Extract User protobuf
    ├─ Create/update NodeEntity
    └─ Call MeshNodeManager.setNodeInfo()
    ↓
MeshNodeManager updates in-memory cache
    ├─ nodeDBbyNodeNum: Map<Int, NodeInfo>
    ├─ nodeDBbyID: Map<String, NodeInfo>
    └─ Emit change events
    ↓
NodeRepository updates database
    ├─ NodeInfoDao.upsert(NodeEntity)
    └─ Emit nodeList StateFlow
    ↓
NodeListViewModel.nodes StateFlow updates
    ↓
UI displays node in list
```

### 2. Position Updates

Nodes share location via `POSITION_APP` packets:

```
Receive FromRadio with POSITION_APP
    ↓
PacketHandler.handleReceivedPosition()
    ├─ Parse Position protobuf (lat, lon, alt, etc.)
    ├─ Update NodeEntity.position
    └─ Call MeshNodeManager.setPosition()
    ↓
NodeRepository.nodeList emits updated nodes
    ↓
MapViewModel.nodes updates with new positions
    ↓
Map shows updated node locations
```

### 3. Telemetry Updates

Device and environment metrics via `TELEMETRY_APP`:

```
Receive TELEMETRY_APP packet
    ↓
PacketHandler.handleReceivedTelemetry()
    ├─ Parse Telemetry protobuf
    │  ├─ DeviceMetrics (battery, voltage, etc.)
    │  └─ EnvironmentMetrics (temp, humidity, etc.)
    ├─ Update NodeEntity.deviceMetrics
    └─ Save to database
    ↓
NodeDetailViewModel.metrics updates
    ↓
UI displays charts and current values
```

## Configuration Management

### Reading Device Configuration

```
App connects to device
    ↓
MeshConfigFlowManager.startConfigFlow()
    ├─ Request all config types
    └─ Build configuration state machine
    ↓
For each ConfigType (Device, Position, Power, Network, etc.):
    ├─ MeshCommandSender.getConfig(type)
    ├─ Send ToRadio.getConfigRequest
    └─ Wait for response
    ↓
Receive FromRadio.config
    ↓
MeshConfigHandler.handleConfig()
    ├─ Parse Config protobuf
    ├─ Match config type
    └─ Save to DataStore
    ↓
RadioConfigRepository updates StateFlows
    ├─ deviceConfig: StateFlow<DeviceConfig>
    ├─ positionConfig: StateFlow<PositionConfig>
    ├─ loraConfig: StateFlow<LoRaConfig>
    └─ ... (all config types)
    ↓
RadioConfigViewModel.uiState updates
    ↓
Settings screens display current values
```

### Writing Configuration

```
User changes setting in UI
    ↓
RadioConfigViewModel.updateConfig(newConfig)
    ↓
RadioConfigRepository.setConfig(config)
    ├─ Save to DataStore (local cache)
    └─ Call ServiceRepository.setConfig()
    ↓
MeshService.setConfig()
    ↓
MeshCommandSender.setConfig()
    ├─ Build Config protobuf with changes
    ├─ Wrap in ToRadio.setConfig
    └─ Send to device
    ↓
Device applies configuration
    ↓
Device sends back updated config
    ↓
Receive and update as in "Reading" flow
    ↓
UI shows confirmation / updated values
```

### Channel Management

Channels define encryption keys and mesh routing:

```
RadioConfigViewModel.channels: StateFlow<List<Channel>>
    ↓ sourced from
RadioConfigRepository.channelSetFlow
    ↓ sourced from
DataStore + device
    ↓
User edits channel (name, PSK, role)
    ↓
RadioConfigViewModel.setChannel(index, channel)
    ↓
MeshCommandSender.setChannel()
    ├─ Build Channel protobuf
    ├─ Set index (0-7)
    ├─ Set settings (psk, name, role)
    └─ Send ToRadio
    ↓
Device updates channel configuration
    ↓
All nodes on same PSK can decrypt messages
```

## Location Sharing

### Broadcasting Own Location

```
MeshLocationManager (in MeshService)
    ├─ Observes position config preferences
    │  ├─ GPS update interval
    │  ├─ GPS broadcast enabled
    │  └─ Smart position enabled
    ├─ FusedLocationProviderClient
    │  └─ Request location updates (GPS)
    └─ On location update:
        ├─ Check if enough change/time elapsed
        ├─ Build Position protobuf
        ├─ MeshCommandSender.sendPosition()
        └─ Broadcast as POSITION_APP packet
        ↓
Transmitted to mesh
    ↓
Other nodes receive and display on map
```

### Waypoint Management

Users can drop waypoints (POIs) on the map:

```
User long-presses map location
    ↓
MapViewModel.addWaypoint(lat, lon, name, icon)
    ↓
Build Waypoint protobuf
    ├─ Set id = generateId()
    ├─ Set name, description
    ├─ Set latitudeI, longitudeI
    └─ Set icon emoji
    ↓
MeshCommandSender.sendWaypoint()
    ├─ Wrap in MeshPacket with WAYPOINT_APP
    └─ Send to mesh
    ↓
Other nodes receive waypoint
    ↓
Display as marker on their maps
```

## Firmware Updates

### OTA (Over-The-Air) Update Process

```
FirmwareViewModel.checkForUpdates()
    ↓
NetworkRepository.fetchReleases()
    ├─ HTTP GET to firmware.meshtastic.org API
    ├─ Parse JSON response
    └─ Filter by hardware type
    ↓
Display available versions in UI
    ↓
User selects version and taps "Update"
    ↓
FirmwareViewModel.startUpdate(release)
    ↓
Download firmware binary (.bin file)
    ├─ NetworkRepository.downloadFirmware(url)
    ├─ Track download progress
    └─ Save to cache directory
    ↓
Verify file integrity (SHA256)
    ↓
ServiceRepository.startFirmwareUpdate(file)
    ↓
MeshService.updateFirmware()
    ↓
Split binary into chunks (max 256 bytes each)
    ↓
For each chunk:
    ├─ Build ToRadio with firmware update data
    ├─ Send chunk
    ├─ Wait for ACK
    └─ Update progress
    ↓
After all chunks sent:
    └─ Device reboots and applies update
    ↓
FirmwareViewModel.updateProgress shows 100%
    ↓
Wait for device to reconnect (30-60 seconds)
    ↓
Verify new version from device info
```

## Background Operations

### MeshService Foreground Service

The service runs in foreground to maintain mesh connectivity:

```kotlin
MeshService.onStartCommand()
    ├─ Call startForeground(notification)
    │  └─ Shows persistent notification
    ├─ START_STICKY return mode
    │  └─ System restarts if killed
    └─ Maintain PARTIAL_WAKE_LOCK
       └─ Keep CPU awake for incoming messages
```

### WorkManager Tasks

#### MeshLog Cleanup

Periodically cleans old debug logs:

```
MeshUtilApplication.scheduleMeshLogCleanup()
    ├─ Schedule PeriodicWorkRequest (1 hour interval)
    └─ Enqueue MeshLogCleanupWorker
    ↓
MeshLogCleanupWorker.doWork()
    ├─ Read retention period from prefs
    ├─ Calculate cutoff timestamp
    ├─ MeshLogDao.deleteOlderThan(cutoff)
    └─ Return Result.success()
```

### Notification Management

MeshServiceNotificationsImpl handles:

1. **Foreground Service Notification**
   - Shows connection status
   - Quick actions (disconnect, settings)
   - Updates dynamically with node count

2. **Message Notifications**
   ```
   New message received
       ↓
   MeshServiceNotificationsImpl.notify()
       ├─ Group by sender
       ├─ Create notification with:
       │  ├─ Sender name + message preview
       │  ├─ Reply action (direct reply)
       │  └─ Mark as read action
       ├─ Play notification sound
       └─ NotificationManager.notify()
   ```

3. **Channel-Specific Notifications**
   - User can enable/disable per channel
   - Controlled in ContactSettings

## Data Synchronization

### State Management

All repositories expose `StateFlow` for reactive updates:

```kotlin
// Example: NodeRepository
class NodeRepository @Inject constructor(
    private val nodeInfoDao: NodeInfoDao,
    private val meshNodeManager: MeshNodeManager
) {
    val nodeList: StateFlow<List<NodeInfo>> = 
        combine(
            nodeInfoDao.getNodes(), // Flow from Room
            meshNodeManager.nodesFlow // In-memory updates
        ) { dbNodes, memNodes ->
            // Merge and deduplicate
            (dbNodes + memNodes).distinctBy { it.num }
        }.stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
}
```

### Database Migrations

Room auto-migrations handle schema changes:

```kotlin
@Database(
    entities = [NodeEntity::class, Packet::class, /* ... */],
    version = 32,
    autoMigrations = [
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        // ... through version 32
    ]
)
```

User data persists across app updates without manual migration code.

## Error Handling

### Connection Failures

```
RadioInterfaceService connection fails
    ↓
Emit connectionState = DISCONNECTED
    ↓
ServiceRepository observes state change
    ├─ Attempt reconnection (with exponential backoff)
    ├─ Max retry count: 3
    └─ If fails, show error to user
    ↓
UIViewModel.connectionState updates
    ↓
UI shows "Connection Lost" banner
    └─ Provide "Retry" button
```

### Packet Loss

```
Message sent but no ACK received within timeout (45 seconds)
    ↓
PacketHandler ACK timeout fires
    ↓
Update packet status to ERROR
    ↓
PacketRepository updates database
    ↓
UI shows error indicator (red X icon)
    └─ User can tap to resend
```

### Protobuf Parse Errors

```
Receive malformed data from device
    ↓
MeshMessageProcessor.parseFromRadio() throws
    ↓
Catch exception
    ├─ Log error with raw bytes
    ├─ Save to MeshLog with level ERROR
    └─ Continue listening (don't crash)
```

## Performance Optimizations

### 1. Lazy Initialization

Components initialize only when needed:
- Database: Async initialization in Application.onCreate()
- ViewModels: Created on first screen access
- Repositories: Singleton but dependencies lazy-loaded

### 2. Paging

Large lists use Paging 3:
```kotlin
val messages: Flow<PagingData<Packet>> = 
    packetDao.getMessagesPaged()
        .map { it.toPacket() }
        .cachedIn(viewModelScope)
```

Loads data in chunks (30 items at a time) as user scrolls.

### 3. Flow Sharing

StateFlows share single data source across all observers:
```kotlin
.stateIn(
    scope = externalScope,
    started = SharingStarted.WhileSubscribed(5000), // 5s delay before stopping
    initialValue = emptyList()
)
```

Stops upstream collection 5 seconds after last subscriber.

### 4. Compute Caching

MeshNodeManager maintains in-memory cache to avoid repeated DB queries for frequently accessed nodes.

## Security Considerations

### 1. Encryption

All mesh messages encrypted with:
- **Channel PSK (Pre-Shared Key)** - AES-128/256
- **Default channel** - Well-known key for public mesh
- **Private channels** - User-generated keys

### 2. Permission Handling

Requires runtime permissions:
- `ACCESS_FINE_LOCATION` - For GPS and BLE scanning
- `BLUETOOTH_CONNECT` - BLE device connection
- `BLUETOOTH_SCAN` - BLE device discovery
- `POST_NOTIFICATIONS` - Message notifications (Android 13+)

### 3. No Cloud Storage

All data stored locally:
- Room database: `/data/data/com.geeksville.mesh/databases/`
- DataStore preferences: `/data/data/com.geeksville.mesh/files/datastore/`
- No automatic cloud backup

## Debugging & Logging

### MeshLog

Internal logging system for troubleshooting:

```kotlin
// Components log important events
MeshLog.d("MyComponent", "Event happened: $details")
    ↓
Stored in Room database (MeshLogEntity)
    ↓
Accessible in Debug panel (Settings → Debug)
    ↓
Can export logs as text file
```

### Kermit Logger

Third-party logging library used throughout:

```kotlin
Logger.d { "Debug message" }
Logger.i { "Info message" }
Logger.e(exception) { "Error message" }
```

Logs to Logcat in debug builds.

## Further Reading

- [ARCHITECTURE.md](ARCHITECTURE.md) - High-level architecture overview
- [CONTRIBUTING.md](CONTRIBUTING.md) - How to contribute
- [AGENTS.md](AGENTS.md) - AI agent development guide
- [Meshtastic Documentation](https://meshtastic.org/docs) - Protocol and device docs

---

For questions, join the [Discord](https://discord.gg/meshtastic) or [forum discussions](https://github.com/orgs/meshtastic/discussions).
