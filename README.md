# About Link
An application for reading, logging, and visualizing OBD data using bluetooth ELM327 based protocols.

# Prerequisites
Link has only been tested on Android 6.0 and newer. Support for older android versions is limited, and anything older than 4.1 should be considered unsupported. The minimum supported version in the Play Store version is 5.0.

If developing, you will need Android Studio with the ability to simulate an android device, or have a real android phone to do live testing with.

# Using Link

## Getting Started
The easiest way to install it is from the Play Store. https://play.google.com/store/apps/details?id=beze.link
Future releases may come packaged with the apk to install directly to your phone, but currently there is no support for it. If you want to build the APK manually, you can, and install it that way. This would be useful mostly for developers.

## Viewing Data
Link will remember last connected devices, and automatically connect to them. When the app starts, it will be on the connect page and attempt connecting. If it is your first time connecting to an ELM327 device, you should select your previously paired device and tap Connect. When successfully connected, the Connect button will turn green and say "Disconnect".

Currently, it is recommended to stay on the connect page until a successful connection happens. If the connection does not succeed within three tries, it will alert the user. If this happens, you can try tapping Disconnect, or restarting the app. Some cheaper, knock-off ELM327 devices may even need to be unplugged and re-plugged in to be reconnected.

Once connected, the menu will show a "PIDs" option. This is where you select parameters to view on your vehicle. Not all parameters are supported by any given vehicle, nor are the PIDs verified against your vehicle. At this time it is up to the user to know which PIDs are or are not supported by their vehicle.

After selecting your PIDs, go to the Menu and tap Data. The values will then live update as long as you stay on this view.


