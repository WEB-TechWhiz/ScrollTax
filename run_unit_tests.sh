#!/usr/bin/env bash
# run_unit_tests.sh - Execute Gradle unit tests and capture output to unit_test_output.log
set -e
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd "$SCRIPT_DIR"
./gradlew clean test | tee unit_test_output.log
