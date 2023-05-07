Globalops Remote Admin RElay Client Library - GORARE CL v0.1 By Artkayek
------------------------------------------------------------------------

Features:
----------
- Simplifies the whole process of connecting and communicating with gorare.
- Parses GOSSIP events and delivers them in a handy form to your plugins (not all events are included at this point, wip).
- Keeps track of the current player list (based on a performant mix of playerinfo and gossip)

How to use:
------------
- Add this jar to your java application library
- Extend the GossipEventListener class and create an instance of your new class

Settings
---------
See settings.cfg for more information.


Changelog
---------
v0.1
- Initial release