# Gemma 2B IT Model Asset Pack

Place the quantized model file here before building the App Bundle:

```
model_asset_pack/src/main/assets/
  └── gemma-2b-it-gpu-int4.bin   (~1.3 GB)
```

## Download

Download from Kaggle or Hugging Face:
- https://www.kaggle.com/models/google/gemma/tfLite/gemma-2b-it-gpu-int4
- https://huggingface.co/google/gemma-2b-it

## Note

This file is **gitignored** because it is too large (~1.3GB).
Make sure to include it when building the release AAB for Google Play:

```bash
./gradlew :app:bundleRelease
```

The asset pack is configured as `install-time`, so the model will be downloaded automatically when the user installs the app from Google Play.
