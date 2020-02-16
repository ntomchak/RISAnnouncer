package io.github.ntomchak;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;

public class AnnouncementScheduler {
  private HashMap<UUID, Integer> runnables;
  
  public AnnouncementScheduler() {
    runnables = new HashMap<UUID, Integer>();
  }
  
  public void putTask(UUID uuid, int id) {
    runnables.put(uuid, id);
  }
  
  public int numRunnables() {
    return runnables.size();
  }
  
  public void unloadPlayer(UUID uuid) {
    Bukkit.getScheduler().cancelTask(runnables.get(uuid));
    runnables.remove(uuid);
  }
  
  public boolean containsPlayer(UUID uuid) {
    return runnables.containsKey(uuid);
  }
  
  public void scheduleNextAnnouncement(PlayerData data, UUID uuid) {
    int configInterval = RISAnnouncer.getInstance().getConfig().getInt("byPlaytime." + data.getGroupPath() + ".interval");
    AnnounceTask task = new AnnounceTask(data, uuid, this);
    task.runTaskLater(RISAnnouncer.getInstance(), configInterval * 1 * 20L);
    putTask(uuid, task.getTaskId());
  }
}
