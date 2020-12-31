<img align="left" width="100" height="100" src="docs/logo.png">

# IPARCOS

IPARCOS (Indi Protocol Android Remote COntrol Software) is an INDI client for Android.
It features an INDI control panel, a mount and focuser controller and a database of objects to point the telescope.

## Features

* Mount and focuser controller with directional pads and speed buttons
* Database full of objects to which you can point the telescope directly from the app
* Generic control panel compatible with every device (BLOB properties not yet supported)
* Languages: English, French, Italian

### Screenshots

<img align="left" width="250" src="docs/connection.png">
<img align="center" width="250" src="docs/motion.png">
<img align="right" width="250" src="docs/control-panel.png">

## Installing IPARCOS

- ~~From Google Play:~~
  - Coming soon!
- From the .apk file:
  - Allow apps from unknown sources in the Android settings (Settings → Security → Unknown sources)
  - Download and install the latest release from the GitHub project page

## Usage

1. Prerequisite
  - Minimum Android version: 5.0
  - An INDI server must be running on the remote computer.
  - You must have network access to the server. 
    - To achieve this, the Android device and the remote computer can be placed on your home network.
    - Alternatively, you can create a wireless network with your Android device and connect the remote computer to this network (the PC address is likely to be 192.168.43.71)
2. Connection
  - Choose the server address in the list or touch "Add server" to add a new server in the list
  - Optionally, you can change the port number if you do not use the default value for the INDI protocol (7624)
  - Click on "Connect"
3. Use the generic control panel
  - Click on the gear icon in the action bar to display the generic control panel
  - Use the tabs to switch between the devices
  - The properties of the device are displayed in a list. Click on a property to edit it or show the details.
4. Control the telescope motion
  - Click on the icon with four arrows in the action bar to display the telescope motion control panel
  - The buttons will be enabled (red) or disabled (grey) depending on the devices features.
    - The arrows are enabled if one device provides the TELESCOPE_ABORT_MOTION, TELESCOPE_MOTION_NS and/or TELESCOPE_MOTION_WE properties
    - The + and - buttons are enabled if one device provides the standard property to control the speed (TELESCOPE_MOTION_RATE) or the LX200 style property (Slew Rate) or the EQMod style property (SLEWMODE)
    - If the device is not connected, the properties may not appear and the buttons will be disabled

### Developed by

- **Marco Cipriani** (@marcocipriani01)
- **Romain Fafet** (@farom57)

### Used Libraries

- [INDIForJava](https://github.com/INDIForJava/INDIForJava), LGPL v3 license