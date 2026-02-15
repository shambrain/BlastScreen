# Build Fix Report

- Migrated to Kotlin DSL multi-module structure.
- Removed legacy Firebase perf flags; using official plugin `com.google.firebase.firebase-perf`.
- Added Firebase plugins in root/app with BOM-managed artifacts.
- Separated Android concerns into app + core modules.

## Codex diagnostics

### Push/remote sequence output

```text
origin	https://github.com/shambrain/BlastScreen.git (fetch)
origin	https://github.com/shambrain/BlastScreen.git (push)
work
fatal: could not read Username for 'https://github.com': No such device or address
```

### Gradle diagnostics output

```text
/bin/bash: line 2: ./gradlew: No such file or directory
/bin/bash: line 3: ./gradlew: No such file or directory
```
