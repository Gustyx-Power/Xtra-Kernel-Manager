# Xtra Kernel Manager - Changelog

All notable changes to this project will be documented in here.

---

## [3.1-Release] - NO ETA

### Triple Layout System
- **Three Distinct Design Languages** - Choose your preferred visual experience
  - **Frosted UI** (formerly Liquid) - Signature glassmorphic design with wavy blob ornaments and neon aesthetics
  - **Material UI** - Clean, standard Material 3 design following Google's guidelines
  - **Classic UI** - NEW! Minimalist dark theme with "Nebula Core" color palette inspired by Google AI Stitch
    - Deep matte foundation with cyan/teal accents
    - Consistent card-based layout with rounded corners
    - Optimized for readability and performance
- **Layout Switcher** - Seamlessly switch between layouts in Settings
- **Per-Layout Screens** - Each layout has its own dedicated tuning screens for consistent experience

### Classic Layout Features
- **CPU Tuning Screen**
  - System Performance header with core management
  - Per-cluster governor and frequency controls
  - Independent core online/offline management
  - Set on Boot toggle with bottom padding
- **GPU Tuning Screen**
  - System Core Status header with GPU Tuner branding
  - Frequency control with min/max sliders and lock functionality
  - Power level management with TDP controls
  - GPU Renderer selection with proper reboot flow
- **Smart Frequency Lock Screen**
  - Unique classic design with color-coded cluster cards
  - Policy selection (Manual/Smart/Game/Battery Saving)
  - Frequency selection dialog with visual feedback
- **Memory Tuning Screen**
  - RAM Overview with health status and usage visualization
  - ZRAM configuration with compression algorithm selection
  - Swap file management with size controls
  - I/O Scheduler per-device configuration
  - Set on Boot toggle

### GPU Renderer Management (All Layouts)
- **Enhanced Renderer Selection Flow**
  - Material Layout: Updated to use proper dialog flow
  - Classic Layout: Implemented with Classic design
  - Frosted Layout: Already had proper implementation
- **Renderer Options**
  - OpenGL - Traditional graphics API
  - Vulkan - Modern low-level API
  - ANGLE - OpenGL ES over Vulkan/D3D
  - SkiaGL - Skia rendering with OpenGL backend
  - SkiaVulkan - Skia rendering with Vulkan backend

### Removed Features
- **Thermal Policy** - Removed from all layouts due to persistent issues
  - UI state synchronization problems across navigation
  - Checkmark display inconsistencies
  - Flickering when selecting policies
  - ViewModel instance conflicts between screens
  - Feature caused crashes on back navigation in Classic and Material layouts

### Safety Features
  - Reboot confirmation dialog with current → new renderer display
  - Warning about compatibility issues
  - Verification dialog with processing state
  - Success/failure feedback with reboot options
  - Property verification after setting

### I/O Scheduler Improvements
- **Per-Device Configuration** (All Layouts)
  - Frosted: Updated from single global scheduler to per-device
  - Material: Already had per-device support
  - Classic: Implemented with per-device support
- **Block Device Detection**
  - Automatic detection of all block devices (sda, sdb, sdc, etc.)
  - Individual scheduler selection per device
  - Real-time scheduler switching
  - Device status indicators

### Setup Screen Improvements
- **Accessibility Permission**
  - Changed from required to optional
  - Removed mandatory accessibility dialogs from Material and Frosted home screens
  - Users can now skip accessibility permission during setup
  - App functions normally without accessibility service

### UI/UX Enhancements
- **Consistent Navigation**
  - All tuning screens properly routed based on selected layout
  - CPU Tuning, GPU Tuning, Memory Tuning, and Smart Frequency Lock
  - Smooth transitions between screens
- **Bottom Padding**
  - Added proper padding to prevent navigation bar overlap
  - Applied to all Classic layout screens
- **Color Consistency**
  - Classic layout uses unified ClassicColors palette throughout
  - Proper color usage (Good, Moderate, Error, Primary, Secondary, Accent)
  - No undefined colors (removed "Warning" references)

### Bug Fixes
- Fixed method call from `setCoreOnline` to `setCpuCoreOnline` in Classic CPU Tuning
- Fixed icon reference from `Icons.Rounded.BoltRounded` to `Icons.Rounded.Bolt`
- Fixed color references in Classic GPU Tuning (Warning → Moderate/Error)
- Fixed missing imports for coroutines in GPU renderer dialogs
- Fixed navigation routing for memory tuning in Classic layout
- Fixed I/O scheduler in Frosted to show all block devices

### Technical Improvements
- **Code Organization**
  - Separated Classic screens into dedicated files
  - Consistent composable structure across layouts
  - Reusable components for sliders, dropdowns, and cards
- **State Management**
  - Proper state handling for expandable cards
  - Local state for sliders to prevent stuttering
  - Coroutine-based async operations for GPU renderer changes
- **Import Optimization**
  - Added missing imports for new features
  - Organized imports by category
  - Removed unused imports

---

## [3.0-Release] - February 10, 2026

### Liquid UI vs Material
- **Dual Design System** - Choose your preferred visual style
  - **Liquid UI** - The new signature layout with Wavy Blob ornaments, neon aesthetics, and glassmorphism
  - **Material UI** - Clean, standard Material 3 design for purists
- **Dynamic Components**
  - **Responsive Recent Cards** - Auto-sizing cards with colored backgrounds
  - **Animated Battery** - Fluid battery level visualization
  - **Modern Bottom Bar** - Smooth transition animations and glass effect

### Game Space
- **Game Monitor Service** - Dedicated background service for game detection
- **Expressive Overlay** - Feature-rich in-game overlay
  - **Performance Stats** - Real-time FPS, CPU/GPU Load, Battery, and Temperature
  - **Quick Controls** - Brightness slider, DND, Ringer Mode, Call rejection
  - **Tools** - Screenshot, Memory Boost, Touch Guard (Disable Gestures)
- **Performance Modes** - Quick switch between Powersave, Balanced, and Performance profiles

### Thermal Manager
- **Thermal Policy Control** - Advanced thermal management system
  - **Policy Selection** - Choose from preset thermal policies
  - **Threshold Visualization** - View Emergency, Warning, Restore, and Critical temperature limits
  - **Glassmorphic UI** - Modern card-based selection with dynamic coloring

### Security & Privacy
- **Banking Protection (Hide Accessibility)**
  - **Dedicated Module** - Hide accessibility services from banking apps (e.g., SeaBank)
  - **App Selection** - New settings screen to filter which apps to hide from
  - **Hybrid Approach** - Combines Xposed hooks on `SettingsProvider` and `PackageManager`
  - **Optimized Caching** - Reduced redundant queries for better performance
  - **Scope Limitation** - Restricted to `android` and `com.android.providers.settings` for stability

### Functional ROM Manager
- **ROM-Specific Features**
  - **Smart Detection** - Automatically shows/hides features based on ROM (e.g., Shimoku)
  - **Universal Features** - Standard tools available for all ROMs
- **New Tools**
  - **Display Size Changer** - Adjust DPI on the fly with a dedicated card
  - **Developer Options** - Quick access shortcut (root required)

### Core & Performance
- **Advanced CPU Management**
  - **Independent Core Control** - Manage individual cores separately from clusters
  - **Real-time Status** - Live online/offline monitoring for each core
  - **Optimized Governor** - Improved application logic and refresh timing
- **Enhanced GPU Info**
  - **Vulkan API Detection** - Accurate version reporting via PackageManager
  - **Renderer Badges** - Real-time indicators for Vulkan/OpenGL renderers
  - **Compute Units** - Estimation based on model and sysfs
  - **Memory Usage** - Improved detection for dedicated/shared GPU memory

### Technical Improvements
- **Build Stability** - Fixed R8/ProGuard minification errors for release builds (`YukiHookAPI`, `KavaRef`)
- **Preference Storage** - Migrated to String-based storage for reliable XSharedPreferences support
- **Debug Tools** - Added `DebugLog` utility for conditional logging
- **Cleanup** - Removed outdated cards (SELinux) and updated team profiles


---

## [2.3-Release] - December 25, 2025

### New Features
- **Screen Saturation Control** - Adjust display color saturation with slider (0.5 - 2.0)
  - Real-time saturation adjustment using SurfaceFlinger
  - Quick presets: sRGB (1.0), P3 (1.1), Vivid (1.3)
  - Persistent settings across reboots
- **Holiday Celebrations** - Festive popup for Christmas, New Year, Ramadan, and Eid al-Fitr
- **Per-Game Control** - Auto-start FPS overlay when game is launched
  - GameMonitorService for automatic game detection
  - Add/remove games from monitored list

### Improvements
- Battery Reset notification at 100% charge
- Localized GameControlSection strings (EN/ID)
- Auto-start GameMonitorService on boot

### Bug Fixes
- Removed Game Overlay from Per-App Profile (moved to Misc)
- Improved overlay service logic

---

## [2.2-Release] - December 10, 2025

### Major Update
- **GPU Lock Frequency** - Lock GPU to specific frequency
- **Performance Profile Quick Settings** - Switch modes from notification panel
- **Rust JNI Backend** - All real-time data backend now uses Rust JNI for improved performance and reliability
  - Faster data processing and lower memory overhead
  - Native performance for CPU, GPU, and system monitoring
  - Enhanced stability for real-time metrics
- Fix Powerlevel GPU Reset
- Fix Crash Battery Notification & Per-App-Profile on Android 14+
- New animated Toggle Switch Button

---

## [2.1-Release] - December 10, 2025

### Major Update
- **Refresh Rate Control** on Per-App Profile
- **Lock GPU Frequency** feature
- Fix Animation Dropdown on Home Screen

---

## [2.0-Release] - December 8, 2025

### Major New Features

#### Per-App Profile System
- Create custom profiles for specific apps
- Set CPU governor per app (performance, balanced, powersave, etc.)
- Set thermal preset per app
- Dynamic refresh rate control per app:
  - 60Hz device: No refresh rate option
  - 90Hz device: 60Hz and 90Hz options
  - 120Hz device: 60Hz, 90Hz, and 120Hz options
- Auto-apply profiles when app is launched
- Background service monitors foreground apps
- Usage stats permission integration
- Enhanced modern dialog UI with gradient headers
- Animated chip selection for refresh rates
- Full localization support (EN/ID)

#### Enhanced Game Control
- Performance modes now include thermal presets:
  - **Performance**: CPU Performance + Dynamic Thermal
  - **Balanced**: CPU Schedutil + Thermal 20
  - **Battery**: CPU Powersave + Incalls Thermal
- Improved overlay permission handling
- Auto-request overlay permission when enabling

#### Root Permission Check
- App now checks for root access at startup
- Displays informative dialog if root not granted
- Shows instructions for Magisk, KernelSU, APatch
- App exits gracefully if root not available

#### Auto Data Reset on Update
- Automatically clears preferences on app update
- Prevents crashes from incompatible old data
- Toast notification when settings are reset
- Version tracking for update detection

### UI/UX Enhancements

#### Enhanced Splash Screen
- Expressive multi-ring loading animation with pulsing effects
- Animated floating background orbs with breathing effect
- New app name and version chip design
- Dynamic status messages during initialization

#### HomeScreen Improvements
- Redesigned header with version on separate line
- Build date displayed (format: YYYY.MM.dd)
- Consistent chip style across app

#### Icon Redesign
- **Simplified Icon Colors** - Addressed user feedback from versions 1.4-1.6
  - Reduced excessive colors in icons that users found distracting
  - Cleaner, more minimalist icon design
  - Better visual consistency across the app
  - Improved readability and focus on content
  - Fresh new look based on user suggestions

### Technical Improvements
- Build Date auto-generated at compile time
- Debug APK Telegram upload feature
- Improved overlay permission flow in Game Control
- Better error handling for root shell commands
- AppProfileService runs as foreground service
- QUERY_ALL_PACKAGES permission for app detection
- PACKAGE_USAGE_STATS for foreground app monitoring

### Localization
- Full Indonesian (Bahasa Indonesia) translation
- All new features fully localized
- Per-App Profile UI strings
- Root Required dialog
- Update dialogs
- Splash screen status messages
- All toast messages

### Bug Fixes
- Fixed overlay permission not being checked before starting service
- Fixed potential crashes when updating from older versions
- Improved app detection using launcher intent filter
- Fixed foreground service compatibility for Android O+

### Permissions Added
- `PACKAGE_USAGE_STATS`: Monitor foreground apps
- `QUERY_ALL_PACKAGES`: Detect installed apps (Android 11+)

---

## [1.6]

### New Features
- **Enhanced FPS Meter** - Improved frame rate monitoring
  - Better accuracy and performance
  - Lower overhead on system resources
  - Smoother overlay rendering
- **TCP Congestion Control** - Network optimization
  - Multiple congestion control algorithms support
  - Real-time switching between algorithms
  - Improved network throughput and latency
  - Better performance for downloads and streaming

### Removed Features
- **Hide Developer Options** - Removed due to compatibility issues
  - Feature did not work correctly on several systems
  - Caused inconsistent behavior across different Android versions
  - Removed to improve app stability and reliability
- **Cache Cleaner** - Removed based on user feedback
  - Users suggested this feature was unnecessary
  - Android system already handles cache management efficiently
  - Removed to simplify app and focus on core kernel management features

---

## [1.5]

### New Features
- **FPS Meter** - Real-time frame rate monitoring overlay
  - On-screen FPS display
  - Minimal performance impact
  - Customizable position and appearance
- **OTA Update System** - Over-the-air update mechanism
  - Seamless app updates without manual download
  - Automatic update checking
  - Background download support
- **Idle Count in Notification** - CPU idle state monitoring
  - Display CPU idle state count in persistent notification
  - Real-time idle state tracking
  - Power consumption insights
- **Battery Status in Statusbar** - Enhanced battery information
  - Show battery percentage in system status bar
  - Display battery temperature
  - Quick access to battery stats
- **Cache Cleaner** - System cache cleaning utility
  - Support for both root and non-root modes
  - Safe cache clearing without affecting user data
  - Multiple cache types support (system, app, dalvik)
  - Storage space recovery
- **Hide Developer Options** - Privacy feature
  - Toggle to hide developer settings from system
  - Prevent unauthorized access to developer features
  - Easy enable/disable functionality
- **GPU Power Level Control** - Graphics performance tuning
  - Adjust GPU power states
  - Multiple power level options
  - Balance between performance and battery life
- **Battery Details in Info** - Comprehensive battery information
  - Health status and condition
  - Voltage and current readings
  - Battery capacity and wear level
  - Charging status and technology type
  - Temperature monitoring

### UI Improvements
- **GlassMorphism Design** - Modern visual effects
  - Combined with Material 3 for Android 12+
  - Frosted glass effect on cards and surfaces
  - Enhanced visual consistency across the app
  - Improved depth and layering
- **Improved Card Layouts** - Better organization
  - Cleaner card designs
  - Smoother animations and transitions
  - Better spacing and padding

### Bug Fixes
- Fixed odd card layout issues in RAM Control popup
- Improved stability and performance
- Better memory management
- Enhanced error handling

---

## [1.4]

### Major Changes
- **Enhanced UI/UX** - Complete visual design overhaul
  - Modern Material Design 3 implementation
  - Improved color schemes and typography
  - Smoother animations and transitions
  - Better visual hierarchy and consistency
- **Profiler Integration** - Quick performance switching
  - Quick Settings Tile for instant profile switching
  - Home screen Widget for easy access
  - Multiple performance profiles support
  - One-tap profile activation
- **Comprehensive CPU Monitoring** - Advanced CPU management
  - Real-time frequency monitoring per core
  - Per-cluster frequency control
  - Governor selection and tuning
  - CPU usage statistics and graphs
  - Historical data tracking
- **Real-time Temperature Monitoring** - Thermal management
  - CPU temperature tracking per core
  - Thermal zone monitoring
  - Temperature alerts and warnings
  - Overheat protection
- **Intelligent Battery Optimization** - Power management
  - Adaptive battery management
  - Power consumption analysis
  - Battery health monitoring
  - Smart charging recommendations
- **Performance Monitoring Dashboard** - System overview
  - System-wide performance metrics
  - Real-time resource usage graphs
  - Historical data tracking
  - CPU, GPU, RAM, and storage monitoring
- **Enhanced Memory Management** - RAM optimization
  - Automatic memory cleanup
  - RAM usage optimization
  - Background process management
  - Memory leak detection
- **Custom Kernel Parameter Tuning** - Advanced tweaking
  - Advanced kernel parameter editor
  - Sysfs interface for kernel tweaking
  - Safe parameter validation
  - Backup and restore functionality

### Minor Changes
- **Improved Stability** - Better error handling throughout the app
- **Enhanced Root Permission Management** - Better error messages and handling
- **Optimized App Startup** - Reduced startup time and memory footprint
- **Enhanced Security** - Improved permission handling and validation
- **Better Logging** - Enhanced debugging capabilities for troubleshooting

---

## [1.3]

### Major Changes
- **CPU Core Control** - Individual CPU core management
  - Enable/disable specific CPU cores
  - Per-core frequency control
  - Core online/offline status monitoring
  - Real-time core state display
- **RAM Control** - Advanced memory management
  - Swappiness adjustment (0-100)
  - Dirty ratio configuration
  - Memory pressure monitoring
  - Real-time RAM usage statistics
- **Miscellaneous Section** - New utility features collection
  - System tweaks and optimizations
  - Additional kernel parameters
  - Various system utilities
- **Swap Size Adjustment** - Dynamic swap file management
  - Create and resize swap files
  - Swap priority configuration
  - Swap usage monitoring
  - Support for multiple swap files

### Bug Fixes
- **Resolved Tuning Functionalities Issues** - Fixed problems from Version 1.2
  - CPU tuning stability improvements
  - Governor switching fixes
  - Frequency control reliability
- **Improved Root Operations** - Better error handling for root commands
- **Enhanced Stability** - Fixed crashes and freezes

### UI Changes
- **Updated User Interface** - Tidied up and improved layouts
  - Better navigation and layout consistency
  - Improved visual feedback for user actions
  - Cleaner card designs
  - Better spacing and organization

### Removed Features
- **Terminal Feature Removed** - Replaced with better alternatives
  - Root shell access moved to dedicated tools
  - Better command execution interface

---

## [1.2-Release]

### New Features
- **CPU Core On/Off Control** - Toggle individual CPU cores
  - Manual core management for power saving
  - Per-core online/offline control
  - Real-time core status display
- **RAM Tunables** - Advanced memory configuration
  - Swappiness parameter adjustment
  - Dirty ratio and background ratio settings
  - Memory compaction controls
- **Compression Algorithm Selection** - ZRAM compression options
  - Multiple algorithm support (lz4, lzo, zstd, etc.)
  - Real-time algorithm switching
  - Compression ratio monitoring

### Improvements
- Enhanced stability for tuning operations
- Improved root command execution
- Better error handling and user feedback

---

## [1.1-Release]

### New Features
- **GPU Control** - Graphics processor management
  - GPU frequency monitoring
  - GPU governor selection
  - GPU load and usage statistics
  - Basic GPU tuning capabilities

### UI Improvements
- Refined user interface from 1.0-Release
- Better layout organization
- Improved visual consistency
- Enhanced navigation flow
- Cleaner card designs

### Bug Fixes
- Fixed various UI inconsistencies
- Improved app stability
- Better resource management

---

## [1.0-Release]

### Major Changes
- **Complete Architecture Migration** - Migrated from XML layouts to Jetpack Compose
  - Modern declarative UI framework
  - Improved performance and responsiveness
  - Reduced app size and memory usage
  - Better UI consistency across devices
- **Material Expressive UI** - New design language implementation
  - Material Design 3 components
  - Dynamic color theming
  - Smooth animations and transitions
  - Modern card-based layouts

### Features
- **CPU Control** - Basic CPU management functionality
  - Frequency monitoring and control
  - Governor selection
  - CPU usage statistics
- **Info Feature** - Minimal system information display
  - Basic device information
  - Kernel version
  - System specifications

### Changelog
- Hotfix for CPU Control problems from Beta version
- Major update from the previous Beta version
- Material Expressive UI implementation
- No GPU Control yet (added in 1.1-Release)
- Minimal information in Info feature

### Known Limitations
- No GPU Control yet (planned for 1.1-Release)
- Limited information in Info feature
- Basic feature set compared to later versions

### Technical Notes
- Complete rewrite of UI layer using Jetpack Compose
- Improved root permission handling
- Better memory management
- Enhanced stability over Beta version

---

## [1.0-Beta] - Initial Release

### Initial Features
- **XML Layout Technology** - Traditional Android View system
  - Basic UI implementation
  - Standard Android layouts
  - Initial feature set

### Known Issues
- **UI Inconsistency** - Inconsistent user interface across different screen sizes and devices
- **Heavy Application Performance**
  - High memory usage and consumption
  - Slow UI rendering and response times
  - Laggy animations and transitions
  - Poor overall performance
- **Limited Functionality** - Basic feature set with minimal capabilities
- **Stability Issues** - Frequent crashes and errors with root operations

### Technical Notes
- First public release as Beta version
- Proof of concept implementation
- Foundation for future development
- XML-based UI architecture (later completely replaced with Jetpack Compose in 1.0-Release due to performance and consistency issues)
