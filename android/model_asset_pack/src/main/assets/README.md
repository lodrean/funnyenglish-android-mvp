# Gemma 2B IT Model Asset Pack

Place the quantized model file here before building the App Bundle:

```
model_asset_pack/src/main/assets/
  └── gemma-2b-it-cpu-int4.bin   (~2.5 GB)
```

## Why CPU version?

The GPU version (`gemma-2b-it-gpu-int4.bin`) requires OpenCL support and fails on devices 
with incompatible Qualcomm drivers (e.g. `clSetPerfHintQCOM` missing).

The CPU version works on **all devices** but is slower and larger (~2.5GB vs ~1.3GB).

## Download

Download the CPU INT4 model from Kaggle:
- https://www.kaggle.com/models/google/gemma/tfLite/gemma-2b-it-cpu-int4

Or via Kaggle Hub (requires Kaggle API token):
```bash
pip install kagglehub
python -c "import kagglehub; kagglehub.model_download('google/gemma/tfLite/gemma-2b-it-cpu-int4')"
```

Then copy the `.bin` file to:
```
funnyenglish/android/model_asset_pack/src/main/assets/gemma-2b-it-cpu-int4.bin
```

## Note

This file is **gitignored** because it is too large (~2.5GB).
Make sure to include it when building the release AAB for Google Play:

```bash
./gradlew :app:bundleRelease
```

The asset pack is configured as `install-time`, so the model will be downloaded automatically when the user installs the app from Google Play.

For RuStore / debug builds without asset pack, the app falls back to downloading from GitHub Releases.
