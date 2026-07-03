# Build and Test Tool

## Purpose

Streamline the build and test workflow for Light Phone tools. Provides common commands with error parsing, quick validation, and clear feedback on build/test failures.

## When to Use

- Building your tool for testing
- Running tests before submitting PR
- Debugging build failures
- Quick validation of code changes
- Preparing for release builds

## Common Workflows

### Quick Build (Debug APK)

```bash
./gradlew :tool:assembleDebug
```

**Use when**: Developing locally, testing on emulator or device

**Output**: APK at `tool/build/outputs/apk/debug/tool-debug.apk`

**May fail if**:
- Syntax errors in Kotlin
- Lint violations (API restrictions)
- Missing dependencies
- GitHub token not configured

### Full Test Suite

```bash
./gradlew check
```

**Use when**: Before submitting PR, major refactoring, preparing for release

**Runs**:
- Compilation for all modules
- Unit tests (`src/test/kotlin/`)
- Lint checks (API restrictions, code style)
- Coverage analysis (if configured)

**Must pass** before any PR is accepted.

### Release Build (Signed APK)

```bash
./gradlew :tool:assembleRelease
```

**Use when**: Preparing for distribution

**Requires**:
- Keystore configured (see signing section below)
- All tests passing
- Version bumped in `build.gradle.kts`

**Output**: APK at `tool/build/outputs/apk/release/tool-release.apk`

### Unit Tests Only

```bash
./gradlew test
```

**Use when**: Testing business logic without full build

**Runs**: All `src/test/kotlin/` tests

### Integration Tests (Emulator)

```bash
./gradlew connectedAndroidTest
```

**Use when**: Testing with `sdk/emulator` (LightOS system app)

**Requires**:
- Android emulator running
- LightOS system app installed

## Common Build Errors & Fixes

### Error: Credentials are missing

```
Could not resolve com.thelightphone:...
Credentials are missing
```

**Fix**: Set GitHub token (see [Setup GitHub Token skill](../setup-github-token/SKILL.md))

```bash
export GITHUB_ACTOR=your_username
export GITHUB_TOKEN=your_token
./gradlew clean :tool:assembleDebug
```

### Error: Restricted API usage

```
error: Restricted API usage (LightSDKRestrictions)
  android.app.Activity is not accessible in Light SDK tools
```

**Fix**: Remove blocked import or use Light SDK alternative

See [Check API Restrictions skill](../check-api-restrictions/SKILL.md)

### Error: Compilation failed

```
error: Unresolved reference: MyViewModel
  MyScreen.kt:12
```

**Fix**: Ensure all classes are imported and syntax is correct

```bash
./gradlew clean :tool:assembleDebug
```

### Error: Test failure

```
FAILED com.thelightphone.myapp.HomeScreenViewModelTest > updateState()
```

**Fix**: Review test output and fix code or test

```bash
./gradlew test --info
```

## Build Optimization Tips

### Parallel Builds

```bash
./gradlew check -x test  # Skip tests
./gradlew :tool:assembleDebug --parallel
```

### Incremental Builds

```bash
./gradlew :tool:assembleDebug  # Only rebuilds changed files
```

### Clean Rebuild (if stuck)

```bash
./gradlew clean :tool:assembleDebug
```

### View Dependency Tree

```bash
./gradlew :tool:dependencies
```

## Release Build Signing

To build a release APK, configure signing in `tool/build.gradle.kts`:

```gradle
android {
    signingConfigs {
        release {
            storeFile file("keystore.jks")
            storePassword "your_keystore_password"
            keyAlias "light-tool"
            keyPassword "your_key_password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

**Security**: Never commit keystore files or passwords to git.

## Pre-PR Checklist

Before submitting a pull request:

```bash
# 1. Run full test suite
./gradlew check

# 2. Build release APK
./gradlew :tool:assembleRelease

# 3. Verify no lint violations
./gradlew lintDebug

# 4. Check API restrictions
grep -r "android.app\|Intent\|getSystemService" tool/src/main/kotlin/
# Should return nothing

# 5. Review build output
ls -lh tool/build/outputs/apk/debug/
ls -lh tool/build/outputs/apk/release/
```

All should complete successfully.

## Deployment & Testing

### Test on Android Emulator

```bash
# 1. Create emulator matching Light Phone specs
# Settings: 1080x1240, 3.92", Android API 34, no Google Play

# 2. Start emulator
emulator -avd my_light_phone

# 3. Install debug APK
adb install tool/build/outputs/apk/debug/tool-debug.apk

# 4. Launch tool
adb shell am start -n com.thelightphone.myapp/.HomeScreen
```

### Test on Physical Light Phone III

```bash
# 1. Enable USB debugging on device
# Settings > Developer Options > USB Debugging

# 2. Connect device
adb devices  # Verify device appears

# 3. Install APK (debug or release)
adb install tool/build/outputs/apk/debug/tool-debug.apk

# 4. Sideload (for release APKs without Light server signing)
adb install -r tool/build/outputs/apk/release/tool-release.apk
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Build slow | Try `./gradlew --parallel` or incremental rebuild |
| Tests timeout | Increase JVM heap: `export GRADLE_OPTS=-Xmx2048m` |
| Emulator not found | Run `emulator -list-avds`, verify emulator name in config |
| APK won't install | Ensure same package name, try `adb uninstall` first |

---

**Related**:
- [Gradle Wrapper](gradlew) — Main build script
- [Tool Module](tool/) — Your application workspace
- [Examples](examples/) — Reference implementations
