# Priority Messaging System

## Overview

The priority messaging system allows users to assign urgency levels to outgoing
messages. The priority value is passed through the application stack to the
Meshtastic radio firmware, which handles actual transmission scheduling and
airtime allocation based on the priority.

## Priority Levels

Defined via `MeshPacket.Priority` in the protobuf layer:

| Level        | Value | Usage                                      |
|--------------|-------|--------------------------------------------|
| UNSET        | 0     | Treated as DEFAULT by firmware              |
| BACKGROUND   | 1     | Non-critical ops (positions, history)       |
| DEFAULT      | 64    | Normal user messages                        |
| HIGH         | 100   | Urgent communications                       |
| ALERT        | 110   | Emergency / critical situations             |
| RELIABLE     | 120   | Admin / protocol packets                    |

Users can select Normal (DEFAULT), High, or Critical Alert (ALERT) from the
message compose UI. Other levels are assigned automatically by the system for
internal packet types.

## Architecture

### 1. UI Layer

**File:** `feature/messaging/src/main/kotlin/org/meshtastic/feature/messaging/Message.kt`

`MessagePrioritySelector` (line 855) renders three filter-chip buttons. The
selected priority is held in `rememberSaveable` state (line 184) and resets to
DEFAULT after each send (line 465). When "Critical Alert" is selected a red
warning icon is shown to indicate reserved airtime usage.

### 2. Event & ViewModel

**Files:**
- `feature/messaging/src/main/kotlin/org/meshtastic/feature/messaging/MessageScreenEvent.kt`
- `feature/messaging/src/main/kotlin/org/meshtastic/feature/messaging/MessageViewModel.kt`

The `SendMessage` event (line 24) carries the message text, an optional reply
ID, and the priority integer. `MessageViewModel.sendMessage()` (line 161)
creates a `DataPacket`, sets `this.priority = priority`, and routes it to the
service layer via AIDL.

### 3. Data Model

**File:** `core/model/src/main/kotlin/org/meshtastic/core/model/DataPacket.kt`

`DataPacket` (line 49) stores the priority as an `Int` field (line 71) alongside
destination, payload bytes, message status, hop limit, and other metadata.

Message status progresses through these states:
`UNKNOWN → QUEUED → ENROUTE → DELIVERED / ERROR`

For received messages the status is set to `RECEIVED`.

### 4. Service & Action Handler

**File:** `app/src/main/java/com/geeksville/mesh/service/MeshActionHandler.kt`

`handleSend()` (line 180) delegates to the command sender, broadcasts a status
update, persists the packet, and records analytics.

### 5. Command Sender & Queuing

**File:** `app/src/main/java/com/geeksville/mesh/service/MeshCommandSender.kt`

This is the core routing logic:

- **`sendData()`** (line 121) — Validates payload size, assigns a packet ID,
  then checks the connection state:
  - **Connected:** calls `sendNow()`.
  - **Disconnected:** calls `enqueueForSending()`.
- **`sendNow()`** (line 144) — Converts the priority int to
  `MeshPacket.Priority` via `forNumber()`, builds a `MeshPacket` with the
  priority field, and calls `packetHandler.sendToRadio()`.
- **`enqueueForSending()`** (line 164) — Buffers the packet in
  `offlineSentPackets`, a `CopyOnWriteArrayList`. Only text messages, alerts,
  and waypoints are retained.
- **`processQueuedPackets()`** (line 170) — Called on reconnection. Iterates the
  queue, sends each packet (preserving original priority), and removes
  successfully sent entries.

### 6. Automatic Priority Assignment

Different packet types receive automatic priority values:

| Packet type       | Priority   | Location                            |
|-------------------|------------|-------------------------------------|
| Position updates  | BACKGROUND | `MeshCommandSender.kt` lines 206, 225 |
| Admin packets     | RELIABLE   | `MeshCommandSender.kt` line 407       |
| History requests  | BACKGROUND | `MeshHistoryManager.kt` line 125      |

### 7. Reception

**File:** `app/src/main/java/com/geeksville/mesh/service/MeshDataHandler.kt`

`handleReceivedData()` (line 98) converts the incoming `MeshPacket` to a
`DataPacket` (preserving the priority), sets status to `RECEIVED`, and
broadcasts it through the app.

## Message Flow Diagram

```
User selects priority in UI
        │
        ▼
MessagePrioritySelector  (Message.kt:855)
        │
        ▼
SendMessage event        (MessageScreenEvent.kt:24)
        │
        ▼
MessageViewModel.sendMessage()  (MessageViewModel.kt:161)
        │
        ▼
DataPacket created with priority  (DataPacket.kt:49)
        │
        ▼
IMeshService.send()      (IMeshService.aidl:74)
        │
        ▼
MeshActionHandler.handleSend()  (MeshActionHandler.kt:180)
        │
        ▼
MeshCommandSender.sendData()  (MeshCommandSender.kt:121)
        ├── Connected ──▶ sendNow() ──▶ radio
        └── Offline ────▶ enqueueForSending() ──▶ offlineSentPackets
                                                        │
                                          (on reconnect) │
                                                        ▼
                                          processQueuedPackets() ──▶ radio
```

## Key Design Decisions

1. **No application-level priority queue ordering.** Messages are sent in FIFO
   order. The priority value is forwarded to the radio firmware which handles
   actual transmission scheduling.

2. **Offline resilience.** Queued messages retain their original priority when
   retransmitted after reconnection.

3. **Thread safety.** The offline queue uses `CopyOnWriteArrayList` for safe
   concurrent access from the service and connection threads.

4. **Selective queuing.** Only text messages, alerts, and waypoints are buffered
   for offline send. Transient data like position updates are discarded.

5. **Firmware-level enforcement.** Airtime prioritization (preempting
   lower-priority packets, reserving channel time for alerts) is handled by the
   Meshtastic radio firmware, not by the Android application.
