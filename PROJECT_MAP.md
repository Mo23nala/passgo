# PROJECT_MAP

## [ASSUMPTIONS & SIMPLICITY FIRST]

- **Assumption 1:** The user wants a local-first approach with E2EE sync, prioritizing security over server-side features.
- **Assumption 2:** A single unified monorepo is appropriate for managing shared core logic and multiple platform targets.
- **Assumption 3:** "Zero-Knowledge" implies the server only ever receives and stores AES-256-GCM encrypted blobs and does not have access to the master password or data encryption keys.
- **Simplicity First:** We will use Zustand for state management to reduce boilerplate. We will use SQLite/IndexedDB for local persistence, avoiding heavier ORMs unless absolutely necessary.

## [TECH_STACK]

- **Core Engine & Shared Logic**: TypeScript `6.0.3`
- **Web & Desktop UI**: React `19.2.7`
- **Desktop Packaging**: Tauri `@tauri-apps/api 2.11.1`
- **Mobile UI**: React Native `0.86.0`
- **State Management**: Zustand `5.0.14`
- **Cryptography**: WebCrypto API & libsodium (libsodium-wrappers `0.8.4`)
- **Database (Local)**: SQLite `5.1.1` / IndexedDB (Web)
- **Build Tooling**: Vite `8.1.3` (Web), Expo `@expo/cli 57.0.4` (Mobile), Tauri CLI `@tauri-apps/cli 2.11.4` (Desktop)

*Note: All package versions verified against official npm sources using shell commands for the current system date (2026-07).*

## [SYSTEM_FLOW]

### API Data Flow (Verifiable Goals)

1. **Goal 1: Local Vault Initialization**
   - User provides Master Password.
   - System derives Master Key (Argon2id).
   - System generates random Data Encryption Key (DEK).
   - DEK is encrypted with Master Key.

2. **Goal 2: Item Creation & Encryption**
   - User creates a Password Item.
   - System encrypts item payload (AES-256-GCM) using DEK.
   - Encrypted payload is saved to local persistent storage.

3. **Goal 3: Synchronization (Zero-Knowledge)**
   - System authenticates with Remote Server.
   - System pushes encrypted payload to Remote Server.
   - Server stores ciphertext (Server has no knowledge of DEK or Master Key).

4. **Goal 4: Cross-Device Retrieval**
   - Second device fetches encrypted payload.
   - Device decrypts DEK using its locally derived Master Key.
   - Device decrypts payload and displays item.

## [ARCHITECTURE]

### Domain-Driven Architecture (Simplicity First)

- `core/`: (Shared across all platforms)
  - `crypto/`: E2EE, Key Derivation, Encryption/Decryption.
  - `domain/`: Business logic, Entities (Vault, Item, User).
  - `sync/`: API clients, Sync conflict resolution.
- `apps/web/`: Web application & Browser Extension (React).
- `apps/mobile/`: iOS & Android (React Native/Expo).
- `apps/desktop/`: Windows & macOS wrappers (Tauri).
- `shared-ui/`: Platform-agnostic UI components (where viable without over-engineering).

*Note: We will avoid excessive file fragmentation. Core logic resides in focused files per domain.*

## [SAFE_LOGGING_STRATEGY]

- **Implementation**: Lightweight asynchronous logger.
- **Levels**: `ERROR` (Actionable failures), `WARN` (Non-fatal issues), `INFO` (Critical state changes). No `DEBUG` or `TRACE` in production.
- **Mechanism**: Non-blocking queue, flushing periodically or on application exit. Never logs sensitive data (keys, passwords, raw payloads).

## [ORPHANS & PENDING]

- **Pending**: Await user approval on the proposed Architecture and System Flow before initiating codebase scaffolding.
- **Pending**: Define API contracts for server synchronization.
- **Pending**: Browser extension specific build configurations.

## [MILESTONE_ROADMAP]

### Phase 1: Core Cryptography & Local Persistence

- **Goal**: Implement Goal 1 (Local Vault Initialization) & Goal 2 (Item Creation).
- **Deliverables**:
  - `core/crypto/` module with Argon2id and AES-256-GCM.
  - Local SQLite database schemas for Vault and Items.
  - Zustand state slice for unlocked vault session.

### Phase 2: User Interface & Platform Integration

- **Goal**: Cross-platform UI scaffolding for Web, Desktop, and Mobile.
- **Deliverables**:
  - React/React Native shared UI components.
  - Desktop (Tauri) and Mobile (Expo) build pipelines.
  - Screens for Authentication, Vault Dashboard, and Item Detail.

### Phase 3: Zero-Knowledge Synchronization

- **Goal**: Implement Goal 3 (Synchronization) & Goal 4 (Cross-Device Retrieval).
- **Deliverables**:
  - API schemas and contracts for server communication.
  - `core/sync/` module for encrypted payload push/pull and conflict resolution.
  - Server implementation capable of storing/serving AES-256-GCM blobs.

### Phase 4: Polish & Advanced Features

- **Goal**: Enterprise readiness and premium UX features.
- **Deliverables**:
  - Biometric authentication integration.
  - Password generation algorithms.
  - Weak-password analysis and health monitoring.
