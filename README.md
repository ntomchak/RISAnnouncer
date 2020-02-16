# RISAnnouncer
This plugin is intended for Minecraft servers that are monetized by selling in-game perks. Instead of most announcements plugins, which select a random message from a list, then send it globally to every player, this one allows for more targeted per-player announcements. It sends different messages to players based on their playtime and perks they have already purchased. It also allows for players to receive messages at different intervals based on those parameters. For example, newer players can receive fewer advertisements and more informational announcements to introduce them to the server's features. Then, as they play longer and become more likely to want extra features, they will receive more advertisement messages. The advertisement messages will only advertise things that they have not purchased yet. If the server sells /fly, /sell hand, and /repair, and the player has purchased /fly, they will only receive advertisements for /sell hand and /repair.

Testing this on an actual Minecraft server, I found that this increased per-player revenue by 10-20%.

Future features:
* Track click-through rate of different advertisement messages
* A/B Testing
* Target messages based on in-game situation (if their tools are breaking, advertise /repair, as an example)
