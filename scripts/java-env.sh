#!/usr/bin/env bash
if [[ -z "${JAVA_HOME:-}" ]] && ! command -v javac >/dev/null 2>&1; then
  for candidate in "$HOME"/.jdks/* /usr/lib/jvm/*; do
    if [[ -x "$candidate/bin/javac" ]]; then
      export JAVA_HOME="$candidate"
      break
    fi
  done
fi
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
javac_bin="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
