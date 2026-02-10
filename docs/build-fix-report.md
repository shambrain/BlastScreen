# Build Fix Report

- Migrated to Kotlin DSL multi-module structure.
- Removed legacy Firebase perf flags; using official plugin `com.google.firebase.firebase-perf`.
- Added Firebase plugins in root/app with BOM-managed artifacts.
- Separated Android concerns into app + core modules.
