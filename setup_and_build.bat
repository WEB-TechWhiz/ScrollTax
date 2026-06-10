@echo off

rem Set Android SDK root directory
set "ANDROID_SDK_ROOT=C:\Users\dell\Downloads\android-sdk"
mkdir "%ANDROID_SDK_ROOT%" 2>nul

rem Point ANDROID_HOME and JAVA_HOME to the SDK root
set "ANDROID_HOME=%ANDROID_SDK_ROOT%" && set "JAVA_HOME=C:\Program Files\Java\jdk-17" && set "PATH=%JAVA_HOME%\\bin;%PATH%"

rem Install required SDK packages using cmdline-tools
echo y | echo y | call "C:\Users\dell\Downloads\commandlinetools-win-14742923_latest\latest\bin\sdkmanager.bat" --sdk_root="%ANDROID_SDK_ROOT%" "platform-tools" "platforms;android-33" "build-tools;33.0.0"

rem Accept all SDK licenses
echo y | call "C:\Users\dell\Downloads\commandlinetools-win-14742923_latest\latest\bin\sdkmanager.bat" --sdk_root="%ANDROID_SDK_ROOT%" --licenses

rem After SDK is ready, build the full APK (clean + debug + release + tests)
bash -c "export ANDROID_HOME=%ANDROID_SDK_ROOT% && export JAVA_HOME=\"%JAVA_HOME%\" && export PATH=\"%JAVA_HOME%\\bin;%PATH%\" && ./build_apk.sh 6"
