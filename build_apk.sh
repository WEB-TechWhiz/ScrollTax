#!/bin/bash
# Scroll Tax Android Build Script
# This script builds the Scroll Tax Android application

set -e

echo "========================================"
echo "  Scroll Tax Android Build Script"
echo "========================================"
echo ""

# Check prerequisites
check_prerequisites() {
    echo "Checking prerequisites..."

    if [ -z "$ANDROID_HOME" ]; then
        echo "ERROR: ANDROID_HOME environment variable is not set"
        echo "Please set it to your Android SDK path, e.g.:"
        echo "  export ANDROID_HOME=/Users/username/Library/Android/sdk"
        exit 1
    fi

    if ! command -v java &> /dev/null; then
        echo "ERROR: Java is not installed or not in PATH"
        exit 1
    fi

    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "Java version: $JAVA_VERSION"

    if [ ! -d "$ANDROID_HOME/build-tools" ]; then
        echo "ERROR: Android SDK not found at $ANDROID_HOME"
        exit 1
    fi

    echo "Prerequisites check passed!"
    echo ""
}

# Clean build
clean_build() {
    echo "Cleaning previous builds..."
    ./gradlew clean
    echo "Clean complete!"
    echo ""
}

# Build debug APK
build_debug() {
    echo "Building Debug APK..."
    ./gradlew assembleDebug

    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        echo ""
        echo "Debug APK built successfully!"
        echo "Location: app/build/outputs/apk/debug/app-debug.apk"
        ls -lh app/build/outputs/apk/debug/app-debug.apk
    else
        echo "ERROR: Debug APK build failed"
        exit 1
    fi
    echo ""
}

# Build release APK
build_release() {
    echo "Building Release APK..."

    # Check for signing config
    if [ ! -f "app/scrolltax.keystore" ]; then
        echo "Creating debug keystore for release build..."
        keytool -genkey -v -keystore app/scrolltax.keystore -alias scrolltax -keyalg RSA -keysize 2048 -validity 10000 -storepass scrolltax -keypass scrolltax -dname "CN=Scroll Tax, OU=Development, O=ScrollTax, L=City, ST=State, C=US"
    fi

    ./gradlew assembleRelease

    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        echo ""
        echo "Release APK built successfully!"
        echo "Location: app/build/outputs/apk/release/app-release.apk"
        ls -lh app/build/outputs/apk/release/app-release.apk
    else
        echo "ERROR: Release APK build failed"
        exit 1
    fi
    echo ""
}

# Install on device
install_apk() {
    echo "Installing APK on connected device..."

    if ! command -v adb &> /dev/null; then
        echo "WARNING: adb not found in PATH. Skipping installation."
        return
    fi

    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        adb install -r app/build/outputs/apk/debug/app-debug.apk
        echo "APK installed successfully!"
    else
        echo "ERROR: APK not found. Build it first."
    fi
    echo ""
}

# Run tests
run_tests() {
    echo "Running unit tests..."
    ./gradlew test
    echo "Tests complete!"
    echo ""
}

# Main menu
show_menu() {
    echo "Build Options:"
    echo "  1. Build Debug APK"
    echo "  2. Build Release APK"
    echo "  3. Build and Install Debug APK"
    echo "  4. Run Tests"
    echo "  5. Clean Build"
    echo "  6. Full Build (Clean + Debug + Tests)"
    echo "  0. Exit"
    echo ""
}

# Main execution
main() {
    check_prerequisites

    if [ $# -eq 0 ]; then
        show_menu
        read -p "Enter option: " choice
    else
        choice=$1
    fi

    case $choice in
        1)
            build_debug
            ;;
        2)
            build_release
            ;;
        3)
            build_debug
            install_apk
            ;;
        4)
            run_tests
            ;;
        5)
            clean_build
            ;;
        6)
            clean_build
            build_debug
            run_tests
            ;;
        0)
            echo "Exiting..."
            exit 0
            ;;
        *)
            echo "Invalid option"
            ;;
    esac
}

main "$@"
