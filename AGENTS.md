# Light SDK Agent Instructions

This repository provides a framework for building tools (lightweight applications) for the Light Phone III. These instructions help AI coding agents understand the project structure, conventions, and constraints.

## Quick Reference

| Command | Purpose |
|---------|---------|
| `./gradlew check` | Build all modules + run tests (run BEFORE PR) |
| `./gradlew :tool:assembleDebug` | Build debug APK for testing tool |
| `./gradlew :tool:assembleRelease` | Build release APK |
| `./gradlew test` | Run all tests |

**Requirements**: GitHub token for private package access (via `local.properties` or `GITHUB_ACTOR`/`GITHUB_TOKEN` env vars). See [**setup-github-token**](.github/skills/setup-github-token/SKILL.md) skill for setup guidance.

## Project Structure

```
light-sdk/
├── sdk/
│   ├── client/        # Main SDK library (LightScreen, LightViewModel, APIs)
│   ├── ui/            # Compose component library (LightText, LightIcon, etc.)
│   ├── shared/        # IPC models and service method definitions
│   ├── emulator/      # LightOS system app for Android emulator testing
│   └── server/        # Backend server utilities
├── tool/              # Developer's main application workspace (edit this)
├── plugin/            # Custom Gradle plugin (KSP processor for validation & code gen)
├── lint-rules/        # Custom lint rules enforcing API restrictions
├── examples/          # Reference tools (authenticator, weather, ui-demo)
└── docs/              # Architecture and design decision documentation
```

## Architecture: MVVM + Compose

Light tools use a strict MVVM pattern with Kotlin Compose for UI. All entry points are defined via KSP annotations:

```kotlin
@InitialScreen  // Entry point—shown when tool launches
class HomeScreen(activity: SealedLightActivity) : 
    LightScreen<Unit, HomeScreenViewModel>(activity) {
    
    override fun createViewModel() = HomeScreenViewModel(lightContext.fileShare)
    
    @Composable
    override fun Content() {
        LightTheme {
            LightText("Hello, Light Phone!", color = LightText.Color.White)
        }
    }
}

class HomeScreenViewModel(fileShare: FileShare) : LightScreenViewModel() {
    private val _state = MutableStateFlow<String>("initial")
    val state = _state.asStateFlow()
    
    fun updateState(newState: String) {
        viewModelScope.launch {
            _state.emit(newState)
        }
    }
}
```

**Key patterns:**
- `@InitialScreen` marks the tool's entry point (one required per tool)
- `@EntryPoint` marks handlers for push notifications
- `@LightJob` marks background tasks
- Use `LightTheme` + Light design tokens (NOT Material3)
- State management via `Flow<T>` with `viewModelScope`
- Navigation via `navigateTo()` and `goBack(result)`

See [HomeScreen example in tool module](tool/src/main/kotlin/com/thelightphone/) for full example. Use [**generate-light-screen-template**](.github/skills/generate-light-screen-template/SKILL.md) skill to quickly scaffold new screens.

## API Restrictions (Sandbox Design)

Light tools run in a sandbox for **privacy, security, and consistent UX**. The following are **permanently blocked**:

| Blocked | Reason |
|---------|--------|
| `android.app.*` (Activities, Services, BroadcastReceivers) | Custom system manages app lifecycle |
| `Intent` launching, `startActivity()` | No arbitrary app switching allowed |
| `getSystemService()`, `contentResolver` | No system access; use Light SDK APIs |
| Reflection (`.javaClass`, `Class.forName()`) | Security constraint |
| AppCompat library | Using Light design system instead |

**Allowed dependencies** (curated whitelist): Kotlin stdlib, Compose, Room, Ktor, Coroutines, WorkManager, UnifiedPush.

**Allowed permissions**: `INTERNET`, `CAMERA`, `RECORD_AUDIO`, `LOCATION`, `WAKE_LOCK`, `VIBRATE`, `POST_NOTIFICATIONS`.

These restrictions are enforced via:
1. Custom Lint rules (compile-time checks)
2. KSP plugin validation
3. `plugin/build.gradle.kts` blocklist

Use [**check-api-restrictions**](.github/skills/check-api-restrictions/SKILL.md) skill to scan code for blocked APIs before build.

→ See [CONTRIBUTING.md](CONTRIBUTING.md) for requesting new libraries or APIs.

## Tool Metadata (`lighttool.toml`)

Every tool must declare its metadata in `lighttool.toml` at the module root:

```toml
[tool]
id = "com.thelightphone.my-tool"
name = "My Tool"
version = "1.0.0"
author = "Your Name"

[server]
package = "com.thelightphone.my_tool.server"

[permissions]
required = ["INTERNET"]
optional = ["CAMERA"]
``` Use [**validate-tool-metadata**](.github/skills/validate-tool-metadata/SKILL.md) skill to verify format and required fields.

The KSP plugin validates this at build time.

## Data & Persistence

- **Local storage**: Room (SQLite) in app-private directory
- **Networking**: Ktor HTTP client (use `lightContext.httpClient`)
- **Notifications**: UnifiedPush (NOT FCM)
- **Shared files**: `lightContext.fileShare` (limited sandbox access)

## Testing

- **Unit tests**: `src/test/kotlin/` (standard JUnit + Mockk)
- **Integration tests**: Use Android emulator with `sdk/emulator` LightOS system app
- Run all tests: `./gradlew test`
- **Must pass** before PR submission

## Development Workflow

1. **Start**: Edit `tool/src/main/kotlin/com/thelightphone/` (HomeScreen + ViewModel)
2. **Add new screens**: Create `MyScreen : LightScreen<*, MyScreenViewModel>` + factory (use [**generate-light-screen-template**](.github/skills/generate-light-screen-template/SKILL.md) skill)
3. **Navigate**: Use `navigateTo { MyScreen(it) }`
4. **Build & Test**: `./gradlew :tool:assembleDebug` (see [**build-and-test-tool**](.github/skills/build-and-test-tool/SKILL.md) skill)
5. **Test**: Run on emulator or physical device (with ADB sideload)
6. **Verify**: `./gradlew check` passes (lint, tests, build)
7. **Submit PR**: Link to GitHub issue, ensure all tests pass

## Important Notes

- **This repo is WIP** (as of July 1, 2026). Tool deployment infrastructure is still being built.
- **No public API changes**, dependency updates, or architecture changes** in PRs (see [CONTRIBUTING.md](CONTRIBUTING.md)).
- Use ADB sideload for testing on physical Light Phone III hardware; emulator testing preferred.
- All communication in issues/PRs must be from humans (no AI-generated content; see [AI/LLM Policy](CONTRIBUTING.md#aillm-policy)).

## Skills & Automation

Specialized skills automate common Light SDK tasks. Browse the full registry:

→ **[Skills Registry](.github/skills/README.md)** — Quick lookup by task, full troubleshooting guides, and code examples

**Quick links to key skills:**
- [**build-and-test-tool**](.github/skills/build-and-test-tool/SKILL.md) — Build APKs, run tests, deploy to emulator/device
- [**generate-light-screen-template**](.github/skills/generate-light-screen-template/SKILL.md) — Scaffold new screens with MVVM pattern
- [**check-api-restrictions**](.github/skills/check-api-restrictions/SKILL.md) — Scan for blocked APIs before build
- [**validate-tool-metadata**](.github/skills/validate-tool-metadata/SKILL.md) — Verify `lighttool.toml` format
- [**setup-github-token**](.github/skills/setup-github-token/SKILL.md) — First-time GitHub token setup

## Documentation

- [README.md](README.md) — Project overview and quickstart
- [CONTRIBUTING.md](CONTRIBUTING.md) — Contribution guidelines and policies
- [docs/](docs/) — Architecture decisions, system app setup, design decisions
- [sdk/client/README.md](sdk/client/README.md) — SDK client library reference
- [examples/](examples/) — Reference tool implementations

---

**When stuck**: 
1. Browse the [Skills Registry](.github/skills/README.md) for task-based or error-based guidance
2. Check the examples/ folder or docs/ for similar patterns
3. The project is rapidly evolving—run `git pull` frequently for updates
