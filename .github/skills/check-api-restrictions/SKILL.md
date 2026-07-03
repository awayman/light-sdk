# Check API Restrictions

## Purpose

Scan tool code for blocked imports and API calls before compilation. Catches sandbox violations early, preventing build failures and ensuring compliance with Light Phone security model.

## When to Use

- After significant code changes or refactoring
- Before submitting a PR
- When integrating new third-party libraries
- If you're unsure whether an API is allowed

## Blocked APIs (Will Not Compile)

These are permanently restricted due to sandbox design:

### Activities & Lifecycle
- `android.app.Activity`, `android.app.Service`, `android.app.BroadcastReceiver`
- `android.content.Intent`, `startActivity()`, `startService()`
- `getActivity()`, `getApplication()`

### System Access
- `getSystemService()`, `contentResolver`, `registerReceiver()`
- `Context.getFilesDir()`, `Context.getCacheDir()`
- Reflection: `.javaClass`, `Class.forName()`, `Method.invoke()`

### UI Libraries
- `androidx.appcompat.*` (AppCompat library)
- Material Design 2/3 (`com.google.android.material.*`)
- Custom Activity/Fragment-based views

### Data Access
- `SharedPreferences` (use Room instead)
- File I/O via standard Android APIs
- Direct access to system resources

## Allowed Alternatives

| Blocked | Use Instead |
|---------|------------|
| `Intent` / Activities | `LightScreen` navigation + `navigateTo()` |
| `getSystemService()` | `lightContext.*` APIs (httpClient, fileShare, etc.) |
| `contentResolver` | Room (SQLite) or `lightContext.fileShare` |
| AppCompat | `LightTheme` + Light design tokens |
| SharedPreferences | Room database in app-private directory |

## Scanning Your Code

### Manual Check

Search for common blocked patterns in your tool module:

```bash
grep -r "android.app\|Intent\|startActivity\|getSystemService\|androidx.appcompat" tool/src/main/kotlin/
```

Should return zero results.

### Compile-Time Check

The build will fail with lint errors if violations are detected:

```bash
./gradlew :tool:assembleDebug
```

Look for errors like:

```
error: Restricted API usage (LightSDKRestrictions)
  android.app.Activity is not accessible in Light SDK tools
  at MyScreen.kt:12
```

## Common Violations & Fixes

### Issue: Using Intent

```kotlin
// ❌ BLOCKED
val intent = Intent(context, OtherActivity::class.java)
startActivity(intent)

// ✅ ALLOWED
navigateTo { MyScreen(it) }
```

### Issue: Using SharedPreferences

```kotlin
// ❌ BLOCKED
val prefs = context.getSharedPreferences("app", MODE_PRIVATE)
prefs.edit().putString("key", "value").apply()

// ✅ ALLOWED (Room)
@Entity
data class Setting(
    @PrimaryKey val key: String,
    val value: String
)

@Dao
interface SettingDao {
    @Insert fun insert(setting: Setting)
    @Query("SELECT * FROM Setting WHERE key = :key") suspend fun get(key: String): Setting?
}
```

### Issue: Using AppCompat

```kotlin
// ❌ BLOCKED
import androidx.appcompat.app.AppCompatActivity
class MyActivity : AppCompatActivity()

// ✅ ALLOWED
import com.thelightphone.sdk.client.screen.LightScreen

class MyScreen(activity: SealedLightActivity) : 
    LightScreen<Unit, MyScreenViewModel>(activity)
```

### Issue: Reflection

```kotlin
// ❌ BLOCKED
val clazz = Class.forName("com.example.MyClass")
val method = clazz.getMethod("doSomething")

// ✅ ALLOWED
// Use direct calls or dependency injection instead
myObject.doSomething()
```

## Requesting New APIs

If you need an API that's currently blocked:

1. Open a GitHub issue describing why you need it
2. Explain the security/privacy implications
3. Propose an alternative if possible
4. Light team will evaluate for future sandbox versions

See [CONTRIBUTING.md](CONTRIBUTING.md#feature-requests) for details.

## Verification Checklist

Before building or submitting PR:

- [ ] No `android.app.*` imports
- [ ] No `Intent` or `startActivity` usage
- [ ] No `getSystemService()` or `contentResolver`
- [ ] No reflection (`.javaClass`, `Class.forName()`)
- [ ] No AppCompat imports
- [ ] All screens extend `LightScreen`
- [ ] All ViewModels extend `LightScreenViewModel`
- [ ] Using `LightTheme` for UI, not Material
- [ ] Data persisted via Room, not SharedPreferences
- [ ] Networking via `lightContext.httpClient`, not custom libraries
- [ ] `./gradlew :tool:assembleDebug` completes without lint errors

---

**Related**:
- [Sandbox Design Overview](docs/design_decisions/)
- [Lint Rules Source](lint-rules/)
- [Allowed Dependencies](gradle/libs.versions.toml)
