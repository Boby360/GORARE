# GORARE
Global Operations Remote Admin RElay - GORARE

This is the core server plugin system for the game Global Operations.
To talk about the game and development, join us!
https://discord.gg/DHAkjGmNNN

Features:
----------
- Allows for multiple TCP connections (remote admin connections) to be made with GO Game Server.
- Caching mechanism has been built in for heavy duty commands (serverinfo & playerinfo),
this mechanism is controlable through maxcachetime in settings.cfg .
- Support for GOSSIP protocol (except GOSSIP|ban).
- Support for GORARE protocol (very similar to go protocol)

How to run:
------------
- Configure settings in settings.cfg
- Run "java -jar gorare.jar" from your command line

Credits:
----------
- Artkayek ofcourse for all of the prior development
- Based on serkoon's GOSSIP idea.
- Thanks to Boby for helping with setting up VMWare server for tests.

Settings
---------
See settings.cfg for more information.


Changelog
---------
v0.4b
- Fixed playerinfo parsing when the server contains more than 9 players
- Fixed log file detecting when date changed

v0.4
- Removed a Thread.sleep command that made gorare wait 1 sec before each command (this was put there for debugging and forgot to remove it)

v0.3c
- Fixed the way GORARE detects the amount of players ingame while waiting for playerinfo requests (this fixes the wrong-response-bug) - thanks to Catwalk & Mo3adz for testing
- Fixed minor bugs regarding plugins (ie pingkick)

v0.3b
- Added support for GORARE protocol and fixed some minor bugs.

v0.3
- Rebuilt from scratch, it is now thread-safe and much more performant

v0.2
- various bugfixes
- fixed typo in "COMMAND .. executed on server!" to "COMMAND .. executed on the server!"
- added \0 at end of command strings
- redone tcp stream reading method (now uses go protocol instead of \0 delimiter)
- cleaned up code

v0.1 Initial release
