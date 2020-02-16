package io.github.ntomchak;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.math3.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.permission.Permission;

public class RISAnnouncer extends JavaPlugin implements Listener {
  private static RISAnnouncer instance;
  private static Permission perms;
  private List<Integer> playtimeGroups;
  private Set<String> perkGroups;
  private HashMap<UUID, PlayerData> playersData;
  private AnnouncementScheduler announcementScheduler;
  private IncreasePlaytimeScheduler playtimeScheduler;

  public void onEnable() {
    saveDefaultConfig();
    instance = this;
    RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
    getServer().getPluginManager().registerEvents(this, this);
    perms = rsp.getProvider();
    playersData = new HashMap<UUID, PlayerData>();

    loadConfig();
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    if (cmd.getName().equalsIgnoreCase("announcer")) {
      if (sender.hasPermission("risannouncer.admin")) {
        if (args.length == 0) {
          // help
        } else {
          String args0 = args[0].toLowerCase();
          switch (args0) {
          case "help":
            // help
            break;
          case "debug":
            debug(sender);
            break;
          case "reload":
            reload(args);
            sender.sendMessage("RISAnnouncer reloaded.");
            break;
          case "player":
            sender.sendMessage(playerCommand(sender, args));
            break;
          case "updatepermgroups":
            updateCommand(sender, args);
            break;
          }
        }
      }
    }
    return true;
  }
  
  private void debug(CommandSender sender) {
    sender.sendMessage("play time groups: " + playtimeGroups.toString());
    sender.sendMessage("perk groups: " + perkGroups.toString());
    sender.sendMessage("players in data hashmap: " + playersData.size());
    sender.sendMessage("# announcement runnables: " + announcementScheduler.numRunnables());
    sender.sendMessage("# play time increase runnables: " + playtimeScheduler.numRunnables());
  }

  private void updateCommand(CommandSender sender, String[] args) {
    if (args.length == 2) {
      Player p = Bukkit.getPlayer(args[1]);
      if (p != null) {
        getPlayerData(p.getUniqueId()).updateGroups(playerPerkGroups(p));
        System.out.println("updated anonunce groups for " + args[1]);
      }
    }
  }

  private String playerCommand(CommandSender sender, String[] args) {
    if (args.length == 1)
      return "announcer player <player>";
    Player p = Bukkit.getPlayer(args[1]);
    String msg = "";
    System.out.println(playersData.size());
    msg += playersData.get(p.getUniqueId()).toString();
    msg += "\npresent in announcerunnable map: " + announcementScheduler.containsPlayer(p.getUniqueId());
    msg += "\npresent in updaterunnable map: " + playtimeScheduler.containsPlayer(p.getUniqueId());
    msg += "\nplaytime: " + getPlaytime(p);
    return msg;
  }

  public static RISAnnouncer getInstance() {
    return instance;
  }

  @EventHandler
  public void onLogin(PlayerJoinEvent e) {
    loadPlayer(e.getPlayer(), getConfig().getInt("timeAfterLogin"));
  }

  public int timeToRankUp(Player p) {
    return playersData.get(p.getUniqueId()).playtimeGroup() - getPlaytime(p);
  }

  @EventHandler
  public void onDisconnect(PlayerQuitEvent e) {
    unloadPlayer(e.getPlayer());
  }

  public static Permission getPermissions() {
    return perms;
  }

  // player, int (seconds)
  private void loadPlayer(Player p, int timeToFirstMsg) {
    PlayerData pData = placePlayer(p);
    UUID uuid = p.getUniqueId();

    playersData.put(uuid, pData);

    AnnounceTask announceTask = new AnnounceTask(pData, uuid, announcementScheduler);
    announceTask.runTaskLater(this, timeToFirstMsg * 20L);
    announcementScheduler.putTask(uuid, announceTask.getTaskId());

    playtimeScheduler.scheduleNextIncrease(pData, uuid);
  }

  private void unloadPlayer(Player p) {
    UUID uuid = p.getUniqueId();

    playersData.remove(uuid);

    announcementScheduler.unloadPlayer(uuid);

    playtimeScheduler.unloadPlayer(uuid);
  }

  private void reload(String[] args) {
    int secondsToFirstMsg = 60;
    if(args.length == 2) {
      try {
        secondsToFirstMsg = Integer.parseInt(args[1]);
      } catch(NumberFormatException e) {
        
      }
    }
    for(Player p : Bukkit.getOnlinePlayers()) {
      unloadPlayer(p);
    }
    reloadConfig();
    loadConfig();
    for (Player p : Bukkit.getOnlinePlayers()) {
      loadPlayer(p, secondsToFirstMsg);
    }
  }

  private void loadConfig() {
    playtimeGroups = determinePlaytimeGroups();
    announcementScheduler = new AnnouncementScheduler();
    playtimeScheduler = new IncreasePlaytimeScheduler();
    perkGroups = getConfig().getConfigurationSection("promotionMessages.groups").getKeys(false);
  }

  public PlayerData getPlayerData(UUID uuid) {
    return playersData.get(uuid);
  }

  // returns playtime in minutes
  private int getPlaytime(Player p) {
    return (int) ((float) p.getStatistic(Statistic.PLAY_ONE_MINUTE) / (float) 1200);
  }

  private int playersPlaytimeGroup(Player p) {
    int playTime = getPlaytime(p);
    int playTimeGroup = -1;
    for (Integer groupTime : playtimeGroups)
      if (playTime <= groupTime) {
        playTimeGroup = groupTime;
        break;
      }
    return playTimeGroup;
  }

  private PlayerData placePlayer(Player p) {
    PlayerData pData = new PlayerData(playerPerkGroups(p), playersPlaytimeGroup(p));
    return pData;
  }

  public List<Pair<String, Double>> playerPerkGroups(Player p) {
    List<Pair<String, Double>> groupsNotIn = new ArrayList<Pair<String, Double>>();
    for (String group : perkGroups)
      if (!perms.playerInGroup(p, group))
        groupsNotIn.add(new Pair<String, Double>(group,
            Double.valueOf(getConfig().getInt("promotionMessages.groups." + group + ".frequency"))));
    return groupsNotIn;
  }

  private List<Integer> determinePlaytimeGroups() {
    Set<String> groupStrings = getConfig().getConfigurationSection("byPlaytime").getKeys(false);
    ArrayList<Integer> groups = new ArrayList<Integer>();
    for (String s : groupStrings) {
      try {
        groups.add(Integer.parseInt(s));
      } catch (NumberFormatException e) {
      }
    }
    groups.sort(new Comparator<Integer>() {
      public int compare(Integer i1, Integer i2) {
        return Integer.compare(i1, i2);
      }
    });
    for (Integer i : groups)
      System.out.println(i);
    return groups;
  }

  public List<Integer> getPlaytimeGroups() {
    return playtimeGroups;
  }
}
