# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Chirp is a Kotlin Multiplatform chat application (Compose Multiplatform UI) targeting Android, iOS, and Desktop (JVM). Package namespace root: `com.chatapp`.

## Common commands

- Android: `./gradlew :androidApp:assembleDebug`
- Desktop, hot reload: `./gradlew :desktopApp:hotRun --auto`
- Desktop, standard run: `./gradlew :desktopApp:run`
- iOS: open `/iosApp` in Xcode and run from there (no CLI build path for iOS UI)
- Build everything: `./gradlew build`
- Compile a single module: `./gradlew :core:data:compileKotlinMetadata` (swap the module path and target task as needed)

There are no automated tests in this repository currently — don't assume a test task exists for a module unless you've confirmed a `test`/`androidTest`/`iosTest` source set is present for it.

## Module architecture

This is a Gradle multi-module KMP project with a convention-plugin build system (`build-logic/convention`) and a version catalog (`gradle/libs.versions.toml`). Modules are layered by feature and by architectural role:

- `shared/` — app entry point wiring: Koin DI root (`di/InitKoin.kt`, `di/AppModule.kt`), and `navigation/NavigationRoot.kt` which composes each feature's nav graph.
- `core:domain` — platform-agnostic business rules, no framework dependencies. Defines the shared `Result<D, E: Error>` / `EmptyResult<E>` types (`util/Result.kt`) and the `DataError` sealed hierarchy (`util/DataError.kt`, `Remote`/`Local`/`Connection` variants) used for error handling across the whole app instead of exceptions.
- `core:data` — cross-feature data concerns (networking via Ktor `HttpClient`, e.g. `network/HttpClientExt.kt` with platform-specific engine variants per source set).
- `core:presentation` — shared presentation helpers (e.g. mapping `DataError` to user-facing text via `util/DataErrorToUiText.kt`).
- `core:designsystem` — shared Compose UI components/theme (dialogs, result layouts, etc.).
- `feature:<name>:domain` / `feature:<name>:data` / `feature:<name>:presentation` / `feature:<name>:database` — each feature (`auth`, `chat`) is split the same way: domain has use cases/repositories interfaces, data implements them (chat also has a Room `database` module), presentation has Compose screens + ViewModels.
- `androidApp/`, `desktopApp/`, `iosApp/` — platform application shells that consume `shared`.

### Per-feature module DI convention

Each layer module exposes a Koin `module { ... }` (e.g. `ChatDataModule.kt`, `ChatPresentationModule.kt`, `AuthPresentationModule.kt`, `corePresentationModule.kt`), often with platform-specific `.android.kt` / `.ios.kt` / `.jvm.kt` actuals for platform bindings. These are aggregated into `InitKoin.kt` in `shared`.

### Presentation pattern (MVI-style)

Each screen under `feature/*/presentation` follows a consistent 4-file layout, e.g. for `chat_detail`:
- `ChatDetailState.kt` — immutable UI state
- `ChatDetailAction.kt` — user intents dispatched to the ViewModel
- `ChatDetailEvent.kt` — one-off side effects emitted by the ViewModel (navigation, snackbars, etc.)
- `ChatDetailViewModel.kt` — holds state, reduces actions, emits events
- `ChatDetailScreen.kt` — Compose UI, collects state and forwards actions

Feature navigation graphs live under `presentation/navigation` (e.g. `ChatGraphRoutes.kt`, `chatGraph()` extension on `NavGraphBuilder`) and are wired together in `shared/.../navigation/NavigationRoot.kt`.

### Error handling convention

Don't throw/catch exceptions across layer boundaries for expected failure modes. Repository/data-source functions return `Result<D, E>` from `core:domain`, using `DataError.Remote`/`Local`/`Connection` enums. Compose/presentation `onFailure`/`map`/`onSuccess` extension functions on `Result` for control flow, then convert `DataError` to UI text in `core:presentation` rather than passing raw errors to Compose.

### KMP source set hierarchy

The custom hierarchy template (`build-logic/convention/.../convention/HierarchyTemplate.kt`) defines two intermediate source-set groups beyond the default `commonMain`:
- `mobile` (Android + iOS) — group for code shared only between mobile targets, e.g. `mobileMain`
- `native` → `apple` (iOS + macOS) → `ios` — for native/Apple-specific sharing

Expect to see `mobileMain`, `iosMain`, `jvmMain`/`desktopMain`, `androidMain`, and target-specific source sets (e.g. `iosArm64Main`) alongside `commonMain` per module, with `expect`/`actual` declarations bridging them (e.g. `HttpClientExt.*.kt`, `DatabaseFactory.*.kt`).

### Convention plugins

Gradle module type is selected by applying one of these plugin IDs (see `build-logic/convention/src/main/kotlin`), rather than configuring Compose/KMP/Android boilerplate per module:
- `com.chatapp.convention.kmp.library` — plain KMP library module (e.g. `core:domain`)
- `com.chatapp.convention.cmp.library` — KMP + Compose Multiplatform module
- `com.chatapp.convention.cmp.feature` — `cmp.library` plus Koin + navigation + lifecycle deps wired in automatically (used by `feature/*/presentation` modules)
- `com.chatapp.convention.cmp.application` — application module (`androidApp`, `desktopApp`)
- `com.chatapp.convention.android.application` / `.compose` — Android app conventions
- `com.chatapp.convention.room` — adds Room + KSP to a module (chat `database` module)
- `com.chatapp.convention.buildkonfig` — BuildKonfig codegen (e.g. for injecting API base URLs/secrets at build time)

When adding a new module, prefer applying the matching convention plugin over hand-configuring `kotlin { }`/`android { }` blocks.
