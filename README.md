# Japan VPN Android App

A secure and fast VPN client for Android that connects to servers in Japan. Built with modern Android development practices and a focus on security and user experience.

## Features

- 🔒 Secure VPN connection with strong encryption
- 🌍 Multiple server locations across Japan
- 📊 Real-time connection statistics
- 🔄 Auto-reconnect capability
- 🎨 Modern UI with dark/light theme support
- 🛡️ Kill switch feature
- 📱 Split tunneling support

## Technical Stack

- **Language:** Kotlin
- **UI Framework:** Android XML layouts
- **Architecture:** MVVM
- **VPN Implementation:** Android VPNService
- **Dependencies:**
  - AndroidX Core KTX
  - Material Design Components
  - Lifecycle Components
  - Kotlin Coroutines
  - Retrofit for networking
  - DataStore for preferences

## Requirements

- Android 7.0 (API level 24) or higher
- Gradle 8.2.0
- Android Studio Arctic Fox or newer

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Architecture

The app follows MVVM architecture pattern:

- **View Layer:** Activities and XML layouts
- **ViewModel Layer:** MainViewModel for business logic
- **Service Layer:** JapanVpnService for VPN functionality
- **Repository Layer:** Server management and preferences

## Security Features

- AES-256 encryption for VPN tunnel
- Certificate pinning for API communication
- Secure storage for credentials
- Kill switch to prevent data leaks

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Material Design for UI components
- Android VPNService documentation
- OpenVPN/WireGuard protocols
