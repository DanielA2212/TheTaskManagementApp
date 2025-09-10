#!/bin/bash
set -euo pipefail

JAR="target/task-management-app-1.0.0.jar"

echo "Starting Task Management Application..."

if [ ! -f "$JAR" ]; then
  echo "Executable JAR not found at $JAR. Attempting to build with Maven..."
  if command -v mvn >/dev/null 2>&1; then
    mvn -q -DskipTests package
  else
    echo "Maven is not installed. Please install Maven or build the project in IntelliJ (Maven -> package)." >&2
    exit 1
  fi
fi

exec java -jar "$JAR"
