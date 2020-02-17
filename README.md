# RISAnnouncer
This plugin is intended for Minecraft servers that are monetized by selling in-game perks. Instead of most announcements plugins, which select a random message from a list, then send it globally to every player, this one allows for more targeted per-player announcements. It sends different messages at different intervals to players based on their playtime and perks they have already purchased. For example, newer players can receive fewer advertisements and more informational announcements to introduce them to the server's features. Then, as they play longer and become more likely to want extras, they will receive more advertisement messages. The advertisement messages will only advertise things they have not purchased yet. If the server sells /fly, /sell hand, and /repair, and the player has purchased /fly, they will only receive advertisements for /sell hand and /repair.

Testing this on an actual Minecraft server, I found that this increased per-player revenue by 10-20% compared to a standard announcements plugin.

Future features:
* Track click-through rate of different advertisement messages
* A/B Testing
* Target messages based on in-game situation (if their tools are breaking, advertise /repair, as an example)

# config.yml
### Miscellaneous
```
timeAfterLogin: 15
```
Players will receive a message this many seconds after connecting to the server. Then, how long until the next message will be sent to them is determined by the interval defined in byPlaytime.
### Play time groups
Players will be placed in the highest play time group that they have less play time than. For example, if the play time groups are 10, 60, 120, and 1440, a player with 70 minutes of play time will be in the 120 group. This group determines the ratio of information, advertisement, and group-specific messages they will receive, as well as the interval they receive messages at. The group-specific messages allow to give messages based on play time, for example, players who have played less than 1 minute might receive a welcome message.
```
byPlaytime:
  # note, these play time numbers are just examples, you can put any number of numbers here
  10:
    groupSpecificMessages:
    - 'msg 1'
    # interval between messages, in seconds
    interval: 200
    # ratios that determines the relative frequency of announcement types players in this group receive 
    # must be positive numbers, can sum to anything < 2.1 billion
    # in this example, players with less than 10 minutes of play time will not receive promotion messages,
    # 20% of messages they receive will be from the group-specific category, and 80% will be 
    # from the information category.
    promotionRatio: 0
    groupSpecificRatio: 20
    informationRatio: 80
  1440:
    groupSpecificMessages:
    - 'msg 1'
    interval: 300
    promotionRatio: 0
    groupSpecificRatio: 10
    informationRatio: 90
  # aboveHighestTime must be present, in this example players above 1440 minutes of play time will be in aboveHighestTime
  aboveHighestTime:
    groupSpecificMessages:
    - 'msg 1'
    interval: 300
    promotionRatio: 0
    groupSpecificRatio: 10
    informationRatio: 90
```
### Promotion messages
These messages will be sent to players to promote perks they do not have yet.
```
listProportion: 0.75
```
listProportion must be a number from 0.0 - 1.0, it determines the ratio of messages that advertise multiple perks in a list, or just one at a time.

The plugin uses a player's permissions groups to determine which perks they have. In this example, players in the 'fly' group will only receive the messages for the unlimitedhomes and nick groups. If they only don't have one group, they will receive messages only from the group they don't have. If they aren't missing any groups, they'll receive an information message rather than a promotion one.
```
  groups:
  
    # These permissions groups are just examples, the name in this file should be the same as the permissions group people get put into
    # when they have purchased that perk, and there can be a lot more than 3 groups
    fly:
    
      # when perks are randomly chosen to be promoted in a message, 
      # the frequencies they're chosen at are weighted based on this number
      frequency: 100
      
      # short description which will appear when the messages are promoted in comma separated lists
      list:
      - '/fly'
      
      # if there are multiple messages here, one will be randomly chosen to send
      messages:
      - 'Purchase /fly at shop.example.com'
      - 'I believe you can /fly. (insert your website here)'
      
    unlimitedhomes:
      frequency: 75
      
      # if there are multiple items here, one will be randomly chosen
      list:
      - 'unlimited homes'
      - 'unlimited sethomes'
      
      messages:
      - 'Get unlimited homes at shop.example.com'
      
    nick:
      frequency: 50
      list:
      - '/nick'
      - 'set your nickname'
      messages:
      - 'Get /nick at shop.example.com'
      
  # When it displays things in a list, it will be in this format:  
  # (listTemplate1)(item 1)(listTemplate2)(item 2)(listTemplate3)
  listTemplate1: ''
  listTemplate2: ', '
  listTemplate3: ', and more at shop.example.com'
```
