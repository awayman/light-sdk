# Light Phone SDK Repository Exploration

## 1. Build/Test Commands and Project Setup

### Build Configuration
- **Root Config**: [build.gradle.kts](build.gradle.kts) - plugin definitions, compilation settings, GitHub Packages authentication
- **SDK Version**: 0.0.9 (from [gradle.properties](gradle.properties))
- **Target Android**: API 33 (minSdk) → 36 (compileSdk, targetSdk)
- **Kotlin**: 2.3.20, JVM target 17

### Build Setup Requirements
```bash
# GitHub Packages authentication (required for SDK dependencies):
# Option 1: Environment variables
GITHUB_ACTOR=your_username
GITHUB_TOKEN=your_token

# Option 2: local.properties
gpr.user=your_username
gpr.key=your_token
```

### Key Build Tasks
- **Assemble tool**: `./gradlew :tool:assembleDebug` / `assembleRelease`
- **Run tests**: `./gradlew test` 
- **Lint rules**: Custom lint rules auto-injected into builds via [:lint-rules](lint-rules/)
- **Signing**: Development signing config included (lightsdk-dev keystore in [:tool](tool/))

### Module Structure
- **[:sdk:client](sdk/client/)** - Main SDK library (what tool devs import)
- **[:sdk:shared](sdk/shared/)** - Shared models & constants
- **[:sdk:ui](sdk/ui/)** - Compose component library (custom Light UI)
- **[:sdk:server](sdk/server/)** - Server-side bridge for privileged operations
- **[:sdk:emulator](sdk/emulator/)** - LightOS emulator app (system app for testing)
- **[:tool](tool/)** - Template tool/sample application
- **[:plugin](plugin/)** - Gradle plugin with KSP processor (handles metadata & validation)
- **[:lint-rules](lint-rules/)** - Custom Android lint rules enforcing SDK restrictions
- **[:examples](examples/)** - Example tools (authenticator, weather, ui-demo)

---

## 2. SDK Architecture

### Core Components

#### SDK Client Library ([:sdk:client](sdk/client/))
Entry point for tool developers. Exposes:
- `LightScreen` & `SimpleLightScreen` - Base navigation/UI classes
- `LightViewModel` - MVVM base for state management
- `SealedLightActivity` - Wrapped activity context (prevents direct Activity access)
- APIs for: push notifications, file sharing, database, background work, keyboard input

#### UI Library ([:sdk:ui](sdk/ui/))
Custom Material-based component library:
- `LightText`, `LightIcon`, `LightTextField`, `LightButton` - Core components
- `LightTheme`, `LightThemeController` - Theme management (supports light/dark)
- `LightTopBar`, `LightBottomBar` - Navigation UI
- `LightGrid`, `LightScrollView`, `LightClickable` - Layout primitives
- `LightQrCodeScanner` - Camera-based QR scanning
- `LightFullscreenModal` - Modal dialogs
- Keyboard integration via `com.thelightphone.lp3keyboard` library

#### Shared Module ([:sdk:shared](sdk/shared/))
IPC types for tool ↔ LightOS communication:
- `LightServiceMethod` - RPC definitions for privileged operations (e.g., `SetRingtone`)
- `LightServerData` - Push notification registration state
- `LightResult` - Unified result type with error handling

#### LightOS Emulator ([:sdk:emulator](sdk/emulator/))
- System app that runs on Android emulator to simulate LightOS
- Requires platform test keys & writable system partition setup
- Used for local testing before hardware deployment

### Data Flow
```
Tool ([:tool]) 
  ↓ (uses)
SDK Client ([:sdk:client])
  ├─→ UI Components ([:sdk:ui])
  ├─→ Shared Models ([:sdk:shared])
  └─→ LightOS Server (via RPC)
       └─→ Emulator or Real LightOS ([:sdk:emulator] or on-device)
```

---

## 3. Kotlin/Android Patterns & Best Practices

### MVVM Architecture (Mandated)
```kotlin
// ViewModel: manages state & business logic
class HomeScreenViewModel(private val fileShare: LightFileShare) : LightViewModel<Unit>() {
    val ringtones = MutableStateFlow<List<String>>(emptyList())
    
    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        ringtones.value = fileShare.list("ringtones")
    }
}

// Screen: binds UI to ViewModel
@InitialScreen  // KSP annotation (see plugin system)
class HomeScreen(sealedActivity: SealedLightActivity) : 
    LightScreen<Unit, HomeScreenViewModel>(sealedActivity) {
    
    override val viewModelClass = HomeScreenViewModel::class.java
    override fun createViewModel() = HomeScreenViewModel(lightContext.fileShare)
    
    @Composable
    override fun Content() {
        val ringtones by viewModel.ringtones.collectAsState()
        // UI code here
    }
}
```

### Compose-First UI Development
- **No XML layouts** - Pure Kotlin/Compose
- **Custom theme system** - Use `LightTheme`, `LightThemeTokens` (not Material3)
- **Component reuse** - SDK provides all primitive components via `LightText`, `LightIcon`, etc.
- **State management** - Combine Compose `State` with Coroutine `Flow`s

### Navigation Pattern
```kotlin
// Navigate to new screen
navigateTo({ activity ->
    DetailScreen(activity)
}, resultCallback = { result ->
    // Handle result from child screen
})

// Return with result
goBack(result = myResult)
```
- All navigation flows through LightScreen APIs
- Back button is provided by framework
- No Android `startActivity()`, `Intent`, or `ComponentName` access

### Coroutines & Async
```kotlin
viewModelScope.launch {
    val result = callRemoteServiceMethod(
        LightServiceMethod.SetRingtone,
        request
    )
}
```
- Standard `kotlinx.coroutines` library
- `viewModelScope` automatically cleaned up on screen destroy
- Remote calls return `LightResult<T>` with error info

### Database (Room)
```kotlin
// Available via sdk:client
api(libs.androidx.room.runtime)
api(libs.androidx.room.ktx)
```
- Standard Room with KSP code generation
- Full SQLite capabilities
- Data persisted to tool's private directory

### Push Notifications (UnifiedPush)
```kotlin
@EntryPoint  // KSP annotation
object ToolEntryPoint : LightEntryPoint {
    override suspend fun onToolCreate(serverData: StateFlow<LightServerData?>) {
        // Initialize push credentials here
    }
    
    override suspend fun onPushNotification(data: ByteArray) {
        // Handle incoming notifications
    }
}
```
- Uses **UnifiedPush** protocol (open standard, not FCM)
- Entry point called once at tool startup
- Registration data includes server URL and push token

---

## 4. Plugin System & KSP Processor

### What the Plugin Does

The `com.thelightphone.light-sdk` Gradle plugin ([plugin/](plugin/)) provides:

#### A. Metadata Validation & Code Generation
- Parses `lighttool.toml` at build time
- Validates tool ID, label, version, permissions against policy
- Injects values into `AndroidManifest.xml` & `BuildConfig`
- Prevents invalid tools from being built locally

#### B. KSP-Based Symbol Processing
Finds and validates three key annotations:

1. **`@InitialScreen`** (required, exactly 1)
   - Marks the first screen shown when tool launches
   - Must be applied to a `LightScreen` subclass
   - KSP generates factory for automatic instantiation

2. **`@EntryPoint`** (optional, exactly 0-1)
   - Marks the object implementing `LightEntryPoint`
   - Called once at tool startup
   - Handles push notification setup & incoming messages

3. **`@LightJob`** (optional, many)
   - Applied to top-level properties of type `LightJobHandler`
   - Creates a registry of background jobs keyed by name
   - Used for background work scheduling

#### C. Generated Registry
The processor generates `com.thelightphone.sdk.generated.LightSdkRegistry`:
```kotlin
object LightSdkRegistry {
    val initialScreenFactory: ((SealedLightActivity) -> SimpleLightScreen<*>)? = ...
    val entryPoint: LightEntryPoint? = ...
    val jobs: Map<String, LightJobHandler> = mapOf(...)
}
```

#### D. Lint Validation (Custom Android Lint Rules)
Enforces API restrictions at compile time via [:lint-rules](lint-rules/):
- **Blocked imports**: `android.app.*`, `android.content.Intent`, reflection, etc.
- **Blocked code patterns**: 
  - `LocalContext.current` → Use LightScreen APIs instead
  - `startActivity()` → Use `LightScreen.navigateTo()` instead
  - Reflection (`.javaClass`, `.getDeclaredMethod()`, etc.)
  - System services (`getSystemService()`, `contentResolver`)
  - Service/BroadcastReceiver registration

---

## 5. Project Restrictions & Conventions

### Allowed Dependencies (Allowlist)
From [plugin/src/main/kotlin/.../LightSdkPlugin.kt](plugin/src/main/kotlin/com/thelightphone/plugin/LightSdkPlugin.kt):

**Core**:
- `org.jetbrains.kotlin:kotlin-stdlib`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`
- `org.jetbrains.kotlinx:kotlinx-io`

**Android**:
- `androidx.compose.*` (UI framework)
- `androidx.activity:activity-compose`
- `androidx.lifecycle` (ViewModel, ViewModelCompose)
- `androidx.datastore-preferences` (preferences storage)
- `androidx.room` (database)
- `androidx.work` (background work)
- `androidx.core:core-splashscreen`
- `androidx.startup`
- `androidx.annotation`

**Networking**:
- `io.ktor` (client: core, okhttp, content-negotiation, serialization)
- `com.squareup.okhttp3:okhttp`

**Messaging**:
- `org.unifiedpush.android:connector` (push notifications)

**Special**:
- `com.thelightphone.lp3keyboard:ui` (Light Phone keyboard)

**Any others require explicit approval from Light.**

### Allowed Plugins
- `com.android.application` / `com.android.library`
- `org.jetbrains.kotlin.android` / `.jvm`
- `org.jetbrains.kotlin.plugin.compose`
- `org.jetbrains.kotlin.plugin.serialization`
- `com.google.devtools.ksp`
- `com.thelightphone.light-sdk` (the main plugin)

### Allowed Permissions (Allowlist)
From [plugin/.../LightToolMetadata.kt](plugin/src/main/kotlin/com/thelightphone/plugin/LightToolMetadata.kt):

```toml
# In lighttool.toml:
permissions = [
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.WAKE_LOCK",
    "android.permission.VIBRATE",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
]
```
**Cannot use**: `CONTACTS`, `CALENDAR`, `FILES`, `CALL_LOG`, etc.

### Blocked APIs & Patterns

**No Direct Activity Access**:
- Cannot import or cast `android.content.Context`, `android.app.Activity`, `ComponentActivity`
- Cannot call `startActivity()`, `startService()`, `bindService()`
- Cannot use `LocalContext.current` (use `lightContext` from `LightScreen` instead)

**No Reflection**:
- `.javaClass`, `.java`, `Class.forName()`, `.getDeclaredMethod()`, `.getField()` all blocked
- Lint rules flag these at compile time

**No System Services**:
- `getSystemService()` blocked
- `contentResolver` access blocked
- `registerReceiver()` blocked

**No AppCompat or Legacy Frameworks**:
- Must use Compose UI
- Cannot mix androidx.appcompat

### Tool Metadata File ([lighttool.toml](tool/lighttool.toml))
```toml
[tool]
id          = "com.thelightphone.app"           # Dotted Java package ID, 2+ segments
label       = "Light SDK Tool"                  # 1-50 chars, no <, >, control chars
versionCode = 1                                 # 1-2,100,000,000, strictly increasing
versionName = "1.0"                             # [A-Za-z0-9._+-], ≤30 chars
permissions = ["android.permission.INTERNET"]  # From allowlist only
serverPackage = "com.thelightphone.sdk.emulator"  # Target LightOS package (emulator or com.lightos)
```

**Policy Validation**:
- `versionCode` must always increase on new builds
- `id` must be globally unique (verified by Light build server)
- Permissions must match allowlist exactly
- File size ≤ 32KB

### Project Ethos
From README:
> "You can and should use current Android best practices: Kotlin for all source code, Compose for UI, Coroutines for async programming, and MVVM architecture."

But with **intentional API restrictions** to:
1. **Sandbox tools** - Prevent unauthorized system access
2. **Ensure privacy** - No contact/calendar/file system access by default
3. **Maintain consistency** - Force use of Light design system & navigation model
4. **Protect users** - Reflections, services, broadcasts can be abused
5. **Reduce bloat** - "Light" experience means minimal dependencies

---

## 6. Key Files & Documentation

### Documentation
- [README.md](README.md) - Main overview, quickstart, sharing process
- [docs/design_decisions/](docs/design_decisions/) - (currently empty, planned for ADRs)
- [docs/tool_metadata/](docs/tool_metadata/) - Tool metadata specification
- [docs/system_app/](docs/system_app/) - LightOS emulator setup instructions
- [docs/repo/](docs/repo/) - (planned for repo architecture)

### Core SDK Sources
- [sdk/client/src/main/kotlin/com/thelightphone/sdk/](sdk/client/src/main/kotlin/com/thelightphone/sdk/)
  - `LightScreen.kt` - Base screen/navigation class
  - `LightViewModel.kt` - Base ViewModel
  - `LightActivity.kt` - Internal activity wrapper
  - `LightPushManager.kt` - Push notification handling
  - `LightDb.kt` - Database utilities
  - `LightFileShare.kt` - File access API
  - `LightKeyboardManager.kt` - Custom keyboard integration
  - `LightWork.kt` - Background job support

- [sdk/ui/src/main/kotlin/com/thelightphone/sdk/ui/](sdk/ui/src/main/kotlin/com/thelightphone/sdk/ui/)
  - Component library (LightText, LightIcon, LightButton, etc.)
  - Theme system (LightTheme, LightThemeTokens)
  - Composables for QR scanning, modals, scrolling

### Plugin Sources
- [plugin/src/main/kotlin/com/thelightphone/plugin/LightSdkPlugin.kt](plugin/src/main/kotlin/com/thelightphone/plugin/LightSdkPlugin.kt) - Main plugin class, dependency/plugin allowlists
- [plugin/src/main/kotlin/com/thelightphone/plugin/LightSdkProcessor.kt](plugin/src/main/kotlin/com/thelightphone/plugin/LightSdkProcessor.kt) - KSP processor, registry generation
- [plugin/src/main/kotlin/com/thelightphone/plugin/LightToolMetadata.kt](plugin/src/main/kotlin/com/thelightphone/plugin/LightToolMetadata.kt) - lighttool.toml parsing & validation
- [plugin/src/main/kotlin/com/thelightphone/plugin/ManifestGenerator.kt](plugin/src/main/kotlin/com/thelightphone/plugin/ManifestGenerator.kt) - AndroidManifest generation

### Lint Rules
- [lint-rules/src/main/kotlin/com/thelightphone/lint/ActivityAccessDetector.kt](lint-rules/src/main/kotlin/com/thelightphone/lint/ActivityAccessDetector.kt) - Flags direct Activity access
- [lint-rules/src/main/kotlin/com/thelightphone/lint/LightJobDetector.kt](lint-rules/src/main/kotlin/com/thelightphone/lint/LightJobDetector.kt) - Validates @LightJob usage
- [lint-rules/src/main/kotlin/com/thelightphone/lint/LightSdkIssueRegistry.kt](lint-rules/src/main/kotlin/com/thelightphone/lint/LightSdkIssueRegistry.kt) - Registers custom lint issues

### Example Tools
- [examples/authenticator/](examples/authenticator/) - TOTP/HOTP generator with QR camera
- [examples/weather/](examples/weather/) - Weather data fetching
- [examples/ui-demo/](examples/ui-demo/) - Component showcase
- [tool/](tool/) - Template tool skeleton for new developers

---

## 7. Build & Test Workflow Summary

### Local Development
1. **Clone & setup**:
   ```bash
   git clone https://github.com/lightphone/light-sdk.git
   cd light-sdk
   ```

2. **Add GitHub token** (local.properties or env vars)

3. **Run lint & unit tests**:
   ```bash
   ./gradlew test
   ```

4. **Build tool APK**:
   ```bash
   ./gradlew :tool:assembleDebug
   ```

5. **Test on emulator**:
   - Standard Android emulator: Full compatibility, some LightOS features unavailable
   - LightOS Emulator app: Best local experience, requires system app setup

### LightOS Integration
- **Deployment**: Not yet live (July 1, 2026 - work in progress)
- **Future**: Light build server will auto-build, sign, and share tools
- **Toolsigning**: Platform key for official apps, dev key for sideload testing
- **Distribution**: User permission control (Light-approved, SDK-built, or Any)

---

## 8. Architecture Decisions & Design Patterns

### Why Sealed Activity Context?
The `SealedLightActivity` wrapper prevents:
- Direct Activity context leaks
- Reflection/casting attacks
- Accidental use of forbidden Activity APIs

### Why MVVM + Compose + Kotlin?
- **Modern Android best practice**: Offsets learning curve for new developers
- **Safe state management**: ViewModels survive configuration changes naturally
- **UI consistency**: Compose forces consistent rendering (no XML fragmentation)
- **Coroutine integration**: First-class async/await support

### Why KSP Annotation Processing?
- **Build-time validation**: Catches misconfigurations before runtime
- **Registry generation**: No reflection needed at startup
- **Safer than reflection**: Type-safe, AOT-compiled
- **Better tooling support**: IDEs understand KSP annotations

### Why Custom Lint Rules?
- **Static verification**: Catches API violations before they ship
- **Clear error messages**: Points to correct SDK API to use
- **Scalable governance**: Easy to add new restrictions as platform evolves
- **Zero runtime cost**: Purely compile-time checks

---

## 9. Quick Reference: Common Tasks

| Task | Command |
|------|---------|
| Build debug APK | `./gradlew :tool:assembleDebug` |
| Build release APK | `./gradlew :tool:assembleRelease` |
| Run unit tests | `./gradlew test` |
| Run lint checks | `./gradlew lint` |
| Publish SDK to local Maven | `./gradlew publishToMavenLocal` (internal only) |
| Clean build | `./gradlew clean` |
| Rebuild lint rules | `./gradlew :lint-rules:build` |
| Check dependencies | `./gradlew dependencies` |

---

## 10. What's Not Yet Implemented (As of July 1, 2026)

Per README: "This repo is work-in-progress and will remain so for a while."

**Pending**:
- ✅ SDK scaffolding & client library
- ✅ Compose UI components
- ✅ Plugin system & validation
- ✅ Emulator infrastructure
- ❌ Live LightOS deployment system
- ❌ Community tool signing/approval workflow
- ❌ Dashboard for tool distribution
- ❓ Additional APIs (likely expanding allowlist over time)

Recommendations:
- Frequently `git pull` for updates
- Test on emulator first (easier iteration)
- Prepare for migration to Maven Central (currently GitHub Packages)
- Keep tools lightweight & privacy-respecting
