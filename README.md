RPMachine
=========

An updated version of [the original RPMachine](https://github.com/BridgeAPIs/RPMachine).

## Differences from the main project

The main project was supposed to be used in "any" configuration and was, as an effect, quite generic. In practice, it meant 
that the server supported BukkitBridge as a DB backend, and supported two Minecraft versions. That's it.

Because I revived this project mostly for personal use, and also because no-one will ever want to setup
BukkitBridge to run this server, I dropped BukkitBridge from the dependencies, and dropped the backend from the code.

An other important update is that I dropped previous MC versions compatibility. I intend to play in 1.14.4 for now, and 
so that's the version this plugin will support. If I ever update, the plugin will be updated and no backward compat will 
be kept.

Last but not least: the other project is 3-4 years old. This one is "new". I intend to remove a lot of boilerplate code,
improve the rest, and add a lot of cool features. 
