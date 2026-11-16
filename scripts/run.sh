#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/java-env.sh
mkdir -p target/classes
mapfile -t sources < <(find src -name '*.java')
"$javac_bin" --release 17 -encoding UTF-8 -d target/classes "${sources[@]}"
exec "$java_bin" -cp 'target/classes:lib/*' app.MainCU28 "$@"
