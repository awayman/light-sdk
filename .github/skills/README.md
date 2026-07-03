# Light SDK Skills Registry

This directory contains specialized skills that automate common Light Phone SDK development tasks. Skills provide step-by-step guidance, code examples, troubleshooting, and quick fixes for recurring workflows.

## Quick Lookup by Task

| I want to... | Skill | Time |
|--------------|-------|------|
| Build/test my tool | [**build-and-test-tool**](#build-and-test-tool) | 5–30 min |
| Add a new screen to my app | [**generate-light-screen-template**](#generate-light-screen-template) | 5 min |
| Check for API violations | [**check-api-restrictions**](#check-api-restrictions) | 5 min |
| Validate tool metadata | [**validate-tool-metadata**](#validate-tool-metadata) | 2 min |
| Set up GitHub token (first time) | [**setup-github-token**](#setup-github-token) | 10 min |

## Skills by Workflow

### Onboarding & Setup

#### [setup-github-token](setup-github-token/SKILL.md)

**Purpose**: Guide first-time setup of GitHub Personal Access Token for private package access.

**When to use**:
- First-time light-sdk setup
- Build fails with "Credentials are missing"
- Token expired or needs rotation

**Includes**:
- Step-by-step GitHub token creation
- Environment variable / `local.properties` configuration
- Token verification and troubleshooting
- Security best practices

**Related commands**: `GITHUB_ACTOR`, `GITHUB_TOKEN`, `gpr.user`, `gpr.key`

---

### Development & Navigation

#### [generate-light-screen-template](generate-light-screen-template/SKILL.md)

**Purpose**: Scaffold a new `LightScreen` + `LightScreenViewModel` pair following MVVM + Compose patterns.

**When to use**:
- Adding a new screen/page to your tool
- Need consistent screen architecture
- Want to avoid boilerplate and copy-paste errors

**Includes**:
- Screen class template with `@Composable` content
- ViewModel class template with `Flow<T>` state
- Integration steps (navigation, testing)
- Common patterns (state flow, data passing)

**Generates**:
- `MyScreen.kt` (UI layer)
- `MyScreenViewModel.kt` (business logic & state)

---

### Validation & Quality

#### [validate-tool-metadata](validate-tool-metadata/SKILL.md)

**Purpose**: Validate `lighttool.toml` metadata files for syntax, required fields, and permissions.

**When to use**:
- Creating or modifying a tool
- Before running `./gradlew check`
- Verifying permissions match code usage

**Includes**:
- Required schema validation (id, name, version, author, server package)
- Permission list verification (INTERNET, CAMERA, RECORD_AUDIO, etc.)
- Common TOML errors and fixes
- KSP plugin validation context

**Validates**:
- Tool ID format: `com.thelightphone.*`
- Required vs optional permissions
- TOML syntax

---

#### [check-api-restrictions](check-api-restrictions/SKILL.md)

**Purpose**: Scan code for blocked imports and API calls before compilation. Catches sandbox violations early.

**When to use**:
- After significant code changes
- Before submitting PR
- When integrating new third-party libraries
- If unsure whether an API is allowed

**Includes**:
- Complete list of blocked APIs (Activities, Intents, Services, reflection, etc.)
- Allowed alternatives for each blocked API
- Manual scanning with `grep` patterns
- Common violations & fixes
- Pre-PR verification checklist

**Blocks**:
- `android.app.*`, `Intent`, `startActivity()`, `getSystemService()`, reflection, AppCompat

**Allows**:
- `LightScreen`, `LightViewModel`, `LightTheme`, Compose, Room, Ktor, Coroutines

---

### Build & Testing

#### [build-and-test-tool](build-and-test-tool/SKILL.md)

**Purpose**: Streamline build and test workflow with common commands, error parsing, and quick validation.

**When to use**:
- Building locally for testing
- Running tests before PR
- Debugging build failures
- Preparing for release

**Includes**:
- Common build commands with explanations
- Full test suite (`./gradlew check`)
- Debug APK build (`./gradlew :tool:assembleDebug`)
- Release APK build with signing
- Unit tests, integration tests, emulator testing
- Common build errors & fixes
- Pre-PR verification checklist
- Deployment to emulator / physical device

**Quick workflows**:
```bash
./gradlew :tool:assembleDebug      # Debug APK
./gradlew check                     # Full test suite
./gradlew :tool:assembleRelease    # Release APK
./gradlew test                      # Unit tests only
```

---

## Organization

```
.github/skills/
├── README.md                                    (this file)
├── setup-github-token/
│   └── SKILL.md                                 (GitHub token setup guide)
├── generate-light-screen-template/
│   └── SKILL.md                                 (Screen scaffolding templates)
├── validate-tool-metadata/
│   └── SKILL.md                                 (lighttool.toml validation)
├── check-api-restrictions/
│   └── SKILL.md                                 (API violation scanning)
└── build-and-test-tool/
    └── SKILL.md                                 (Build & test workflow)
```

## Related Documentation

- **[AGENTS.md](../AGENTS.md)** — High-level agent instructions for AI coding agents
- **[docs/](../../docs/)** — Architecture, design decisions, system app setup
- **[examples/](../../examples/)** — Reference tool implementations (authenticator, weather, ui-demo)
- **[CONTRIBUTING.md](../../CONTRIBUTING.md)** — Contribution guidelines, API extension requests
- **[README.md](../../README.md)** — Project overview and quickstart

## Common Workflows at a Glance

### Starting a New Tool

1. Read [AGENTS.md](../AGENTS.md) for project overview
2. Use [setup-github-token](setup-github-token/SKILL.md) if first time
3. Edit `tool/src/main/kotlin/com/thelightphone/`
4. Use [generate-light-screen-template](generate-light-screen-template/SKILL.md) for new screens
5. Use [build-and-test-tool](build-and-test-tool/SKILL.md) to build/test

### Before Submitting PR

1. Use [check-api-restrictions](check-api-restrictions/SKILL.md) to verify no API violations
2. Use [validate-tool-metadata](validate-tool-metadata/SKILL.md) to check `lighttool.toml`
3. Use [build-and-test-tool](build-and-test-tool/SKILL.md) to run full test suite
4. Verify `./gradlew check` passes

### Troubleshooting Build Issues

| Error | Skill | Link |
|-------|-------|------|
| "Credentials are missing" | setup-github-token | [View](setup-github-token/SKILL.md#troubleshooting) |
| Restricted API usage | check-api-restrictions | [View](check-api-restrictions/SKILL.md#common-violations--fixes) |
| Compilation failed | build-and-test-tool | [View](build-and-test-tool/SKILL.md#common-build-errors--fixes) |
| Invalid `lighttool.toml` | validate-tool-metadata | [View](validate-tool-metadata/SKILL.md#common-issues) |

## Tips for Using Skills

1. **Read the full skill** if this is your first time—examples and troubleshooting are detailed
2. **Use quick links** in the skill headers for fast navigation to specific sections
3. **Check "When to Use"** at the top of each skill to see if it applies to your current task
4. **Refer to examples** for concrete code patterns—they're tested and ready to adapt
5. **Link to troubleshooting** from the skill when you're stuck

## Adding New Skills

To add a new skill to this registry:

1. Create a directory: `.github/skills/your-skill-name/`
2. Add `SKILL.md` with frontmatter:
   ```markdown
   # Your Skill Name
   
   ## Purpose
   
   [Clear, one-sentence description]
   
   ## When to Use
   
   - [Scenario 1]
   - [Scenario 2]
   ```
3. Update this README with a row in the relevant section
4. Link to the skill from `AGENTS.md` if it's agent-facing

---

**Last updated**: 2026-07-02  
**Light SDK WIP Status**: See [README.md](../../README.md#important-july-1-2026-update)
