#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/java-env.sh
mkdir -p target/classes target/test-classes
mapfile -t sources < <(find src -name '*.java')
mapfile -t tests < <(find tests -name '*.java')
"$javac_bin" --release 17 -encoding UTF-8 -d target/classes "${sources[@]}"
"$javac_bin" --release 17 -encoding UTF-8 -cp target/classes -d target/test-classes "${tests[@]}"
for suite in CU28Test WebTest DatosEjemploTest; do
  "$java_bin" -cp 'target/classes:target/test-classes:lib/*' "$suite"
done
