# Mascot

A Kotlin Multiplatform, faceless companion: an amorphous blob that expresses emotion purely
through motion, posture and color. Shape, colors, speed and body language are configured by the host app.

Targets: Android, iOS (`iosArm64`, `iosSimulatorArm64`) and JVM (desktop).

## Modules

| Module | Depends on | Responsibility |
|---|---|---|
| `mascot-core` | coroutines only | Domain model, motion math, `MascotAnimator` (deterministic simulation), `MascotController` (state + reactions). No UI toolkit. |
| `mascot-compose` | `mascot-core`, Compose Multiplatform | `Mascot(...)` composable: frame loop and Canvas renderer. |

Because the core has no UI dependencies, a SwiftUI or other native renderer can be built on `MascotAnimator`/`MascotFrame` without touching the domain logic.

## Usage

```kotlin
// ViewModel (or any presentation-layer state holder)
class HomeViewModel : ViewModel() {
    val mascot = MascotController(initialMood = MascotEmotion.Calm)

    fun onStreakExtended() {
        mascot.setMood(MascotEmotion.Happy)               // persistent
        mascot.react(MascotEmotion.Excited, 1_500)       // one-off, returns to mood afterwards
    }
}

// UI
Mascot(
    controller = viewModel.mascot,
    modifier = Modifier.size(160.dp),
    appearance = MascotAppearance(
        shape = MascotShape.Droplet.copy(seed = userId.hashCode()), // stable per-user individual
        palette = MascotPalette(body = colors.primary, highlight = colors.primaryContainer, glow = colors.tertiary),
        speed = 0.8f,
    ),
    contentDescription = "Your companion is happy",
)
```

When no reactions are needed, drive it straight from UI state: `Mascot(mood = uiState.mood, modifier = ...)`.

## Customization

| What | API |
|---|---|
| Silhouette | `MascotShape(aspectRatio, irregularity, lobes, bottomWeight, seed, resolution)`; presets `Orb`, `Blob`, `Droplet`, `Cloud` |
| Colors | `MascotPalette(body, highlight, glow)`; per-emotion via `MascotAppearance.emotionPalettes` |
| Speed | `MascotAppearance.speed` (global tempo, `0` freezes), `transitionSeconds` (emotion blend time) |
| Body language | `MotionProfiles` (replace entirely) or `MotionProfiles.Default.withOverrides(mapOf(...))` |

All config types are immutable data classes that validate their ranges and throw `IllegalArgumentException` on invalid input. Appearance changes apply live, without resetting motion.

Emotions: `Neutral`, `Calm`, `Happy`, `Excited`, `Curious`, `Surprised`, `Sad`, `Sleepy`.

## Design notes

- **State vs. effects.** Mood is a `StateFlow` because it survives re-subscription. Reactions go through a `SharedFlow` with no replay, so an old celebration isn't replayed after a configuration change or when the mascot first appears.
- **Deterministic animation.** `MascotAnimator.step(dt)` is pure time integration: no clocks or coroutines. Phases are integrated rather than derived from absolute time, so changing rates mid-animation never causes jumps. Large deltas are clamped (`MAX_STEP_SECONDS`) to avoid jumps after a pause.
- **Rendering cost.** One outline evaluation per frame (~16 trig calls per outline point), a reused `Path`, and a frame-tick state read only in the draw phase, so each frame invalidates drawing, not composition.

## Build

```bash
./gradlew build                  # all targets (iOS requires macOS + Xcode)
./gradlew :mascot-core:jvmTest   # fast core unit tests
```
