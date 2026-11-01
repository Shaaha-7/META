# Ustad: See inside any machine

Android prototype for Meta Wearables Studio UAE. Built on Meta's CameraAccess sample (Wearables Device Access Toolkit 1.0.1).

**Flow:** glasses photo → faces blurred on device → Gemini identifies the machine and ranks the hidden failing parts → exploded view highlights the part → technician proves it with a quick test → repair coach speaks the fix (English / Hindi / Arabic).

## Setup

1. Open this folder in Android Studio and let Gradle sync.
2. Get a free Gemini API key at https://aistudio.google.com/apikey.
3. Add to `local.properties` (it is gitignored, so never commit the key):

   ```properties
   GEMINI_API_KEY=your_key_here
   # optional: override the model
   # GEMINI_MODEL=gemini-flash-latest
   ```

4. Run on an Android phone (Android 12+). Developer Mode does not require Meta credentials.

## Run with Mock Device Kit (no glasses needed)

1. Launch Ustad, tap the **ladybug** (debug) icon, then **Enable MockDeviceKit**.
2. **Pair Ray-Ban Meta**, then turn on **Power** and **Donned**.
3. Camera source: **phone back camera** (point it at the AC) or a **video file** (H.265).
4. **Start session** → **Preview** → tap **capture**. Ustad opens with the diagnosis.

Convert a demo video to H.265 on Windows:

```
ffmpeg -i ac_leak.mp4 -c:v libx265 -tag:v hvc1 -vf "scale=540:960" -an ac_leak_h265.mp4
```

## Where the Ustad code lives

| File | What it does |
|---|---|
| `ustad/GeminiClient.kt` | Prompts + Gemini API calls (inspect, repair steps) |
| `ustad/UstadModels.kt` | JSON → diagnosis / repair models |
| `ustad/PrivacyFilter.kt` | On-device face blur before upload |
| `ustad/UstadVoice.kt` | Text-to-speech in EN / HI / AR |
| `ui/UstadScreen.kt` | Look → Reveal → Confirm → Fix screen |
| `assets/ustad/split_ac_indoor.html` | Exploded view of a split AC indoor unit |
| `ui/CameraScreen.kt` | Hook: captured photo opens Ustad |

## Notes

- The free Gemini tier has a small daily request limit, so don't burn requests before the demo.
- Hindi and Arabic speech need those voices installed on the phone (Settings → Text-to-speech).
- The API key is compiled into the debug app. That's fine for a prototype; production would call a backend.
