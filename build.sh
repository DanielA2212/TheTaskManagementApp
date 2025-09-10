#!/bin/bash
set -euo pipefail

echo "Building Task Management Application (Maven package)..."

if command -v mvn >/dev/null 2>&1; then
  mvn -q -DskipTests package
  echo "Build completed. Executable JAR: target/task-management-app-1.0.0.jar"
else
  echo "Maven is not installed. Please install Maven or build via IntelliJ (Maven -> package)." >&2
  exit 1
fi
