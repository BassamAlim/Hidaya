
## VersePlayerService.onCreate uses `runBlocking`

File: `app/src/main/java/bassamalim/hidaya/features/quran/reader/versePlayer/VersePlayerService.kt:134`

`onCreate` loads recitation data, sura names, etc. via `runBlocking` because `MediaSessionCompat.setCallback` (called inside `initSession()`) must be wired before the system can dispatch any media-button or `onPlayFromMediaId` event. Making this async would create a race: an early media-button intent could arrive while `mediaSession`/`apm`/metadata are still null.

### Options to come back to

1. **Gate callbacks on a `CompletableDeferred<Unit>`** — `mediaSession.setCallback` wraps every method to `await()` initialization. Adds latency to first event but eliminates the block.
2. **Synchronous DAO calls** — expose blocking variants of the repository reads used here so `runBlocking` is no longer crossing suspending boundaries. Still blocks main, but at least removes the coroutine machinery.
3. **Two-phase init** — register the MediaSession in `onCreate` with a stub callback, swap to the real callback after async load completes. Cleanest but more code.

Recommended path: option 1 if first-play latency is acceptable (likely <100ms), otherwise option 3.

## Domain classes still bridge `Activity` to MediaBrowser callbacks

Files:
- `features/quran/reader/QuranReaderDomain.kt`
- `features/recitations/player/RecitationPlayerDomain.kt`

The `MediaBrowserCompat.ConnectionCallback.onConnected()` callback fires async after `mediaBrowser.connect()`, and inside it we need an `Activity` to call `MediaControllerCompat.setMediaController(activity, controller)`. The Domain therefore stores a `pendingActivity: Activity?` between `connect()` and `onConnected()`, cleared on disconnect.

This is a holdover — Domain should not touch `Activity` at all. The proper fix is to move the MediaBrowser+MediaController plumbing into the ViewModel (or a presentation-layer helper) and have Domain expose only suspend/Flow APIs over the resulting controller state. Deferred because it touches three ViewModels and would be a larger refactor.
