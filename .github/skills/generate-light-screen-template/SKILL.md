# Generate Light Screen Template

## Purpose

Quickly scaffold a new `LightScreen` and `LightScreenViewModel` pair for adding new screens to a Light Phone tool. This accelerates navigation flow development following the MVVM + Compose pattern.

## When to Use

- Adding a new screen/page to a Light Phone tool
- Need to follow consistent screen architecture
- Want to avoid boilerplate and copy-paste errors

## Generated Structure

Creates two files:

1. **Screen file** (`MyScreen.kt`): UI layer with `@Composable` content
2. **ViewModel file** (`MyScreenViewModel.kt`): Business logic and state management

## Template Files

### MyScreen.kt

```kotlin
package com.thelightphone.myapp.ui.screen

import androidx.compose.runtime.Composable
import com.thelightphone.sdk.client.screen.LightScreen
import com.thelightphone.sdk.client.activity.SealedLightActivity
import com.thelightphone.sdk.ui.component.LightTheme
import com.thelightphone.sdk.ui.component.LightText

class MyScreen(activity: SealedLightActivity) : 
    LightScreen<Unit, MyScreenViewModel>(activity) {
    
    override fun createViewModel() = MyScreenViewModel()
    
    @Composable
    override fun Content() {
        LightTheme {
            LightText("Hello, MyScreen!", color = LightText.Color.White)
        }
    }
}
```

### MyScreenViewModel.kt

```kotlin
package com.thelightphone.myapp.ui.screen

import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.client.viewmodel.LightScreenViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyScreenViewModel : LightScreenViewModel() {
    
    private val _state = MutableStateFlow<String>("initial")
    val state: StateFlow<String> = _state.asStateFlow()
    
    fun updateState(newValue: String) {
        viewModelScope.launch {
            _state.emit(newValue)
        }
    }
}
```

## Integration Steps

After generating the template:

1. **Customize the ViewModel**: Add your business logic, API calls, database queries
2. **Update the UI**: Replace `LightText` placeholder with real Compose components
3. **Add navigation**: From another screen, use `navigateTo { MyScreen(it) }`
4. **Test**: Build with `./gradlew :tool:assembleDebug` and verify navigation works

## Example: Navigation from HomeScreen

```kotlin
@Composable
override fun Content() {
    LightTheme {
        LightButton(
            onClick = { navigateTo { MyScreen(it) } },
            label = "Go to My Screen"
        )
    }
}
```

## Common Patterns

### State Flow Pattern

```kotlin
private val _data = MutableStateFlow<List<String>>(emptyList())
val data = _data.asStateFlow()

fun loadData() {
    viewModelScope.launch {
        val result = repository.getData()
        _data.emit(result)
    }
}
```

### Passing Data Between Screens

If `MyScreen` needs input data, pass it through the ViewModel constructor:

```kotlin
class MyScreen(activity: SealedLightActivity, private val itemId: String) : 
    LightScreen<Unit, MyScreenViewModel>(activity) {
    
    override fun createViewModel() = MyScreenViewModel(itemId)
}
```

## Style Guidelines

- Use `LightTheme` and Light design tokens (not Material3)
- Keep ViewModels focused—one responsibility per ViewModel
- Use `viewModelScope` to avoid memory leaks
- Expose state as `StateFlow` (immutable from UI perspective)
- Handle errors in ViewModel, not in UI

---

**Related**: 
- [HomeScreen example](tool/src/main/kotlin/com/thelightphone/)
- [SDK Client Reference](sdk/client/README.md)
- [Examples](examples/)
