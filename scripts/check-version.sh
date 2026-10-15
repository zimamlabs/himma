#!/usr/bin/env bash
# Fails when versionName (build.gradle.kts) and APP_VERSION (HimmaApp.kt) differ.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
a="$(sed -n 's/.*versionName = "\(.*\)".*/\1/p' composeApp/build.gradle.kts | head -1)"
b="$(sed -n 's/.*const val APP_VERSION = "\(.*\)".*/\1/p' composeApp/src/commonMain/kotlin/dev/himma/app/HimmaApp.kt | head -1)"
[ -n "$a" ] && [ "$a" = "$b" ] || { echo "version mismatch: versionName=$a APP_VERSION=$b" >&2; exit 1; }
echo "version $a"
