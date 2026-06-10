#!/usr/bin/env bash
# run_tests.sh - Execute unit and instrumentation tests for ScrollTax Android project
set -e

# Navigate to project root (where gradlew is located)
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd "$SCRIPT_DIR"

echo "Cleaning project..."
./gradlew clean

echo "Running unit tests..."
./gradlew test

# Start Android emulator (API 33 Pixel 5) if not already running
# Assumes avd named "pixel_5_api33" exists
EMULATOR_NAME="pixel_5_api33"
if ! adb devices | grep -q "device"; then
  echo "Starting emulator $EMULATOR_NAME..."
  emulator -avd $EMULATOR_NAME -no-snapshot-load -no-window &
  # Wait for device to be ready
  echo "Waiting for device..."
  adb wait-for-device
fi

echo "Running instrumentation (UI) tests..."
./gradlew connectedAndroidTest

echo "All tests completed."
