package io.github.ntomchak;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;

public class IncreasePlaytimeScheduler {
  private HashMap<UUID, Integer> runnables;
  private HashMap<Integer, Integer> nextGroups;

  public IncreasePlaytimeScheduler() {
    runnables = new HashMap<UUID, Integer>();
    nextGroups = getNextPlayerGroups();
  }
  
  public int numRunnables() {
    return runnables.size();
  }

  private HashMap<Integer, Integer> getNextPlayerGroups() {
    List<Integer> playtimeGroups = RISAnnouncer.getInstance().getPlaytimeGroups();
    HashMap<Integer, Integer> nextGroups = new HashMap<Integer, Integer>();
    for (int i = 0; i < playtimeGroups.size() - 1; i++) {
      nextGroups.put(playtimeGroups.get(i), playtimeGroups.get(i + 1));
    }
    nextGroups.put(playtimeGroups.get(playtimeGroups.size() - 1), -1);
    nextGroups.put(-1, -2);
    return nextGroups;
  }

  public int nextPlaytimeGroup(int current) {
    return nextGroups.get(current);
  }

  private void putTask(UUID uuid, int id) {
    runnables.put(uuid, id);
  }

  public void unloadPlayer(UUID uuid) {
    Integer taskId = runnables.get(uuid);
    if (taskId != null) {
      Bukkit.getScheduler().cancelTask(taskId);
      runnables.remove(uuid);
    }
  }

  public boolean containsPlayer(UUID uuid) {
    return runnables.containsKey(uuid);
  }

  public void scheduleNextIncrease(PlayerData data, UUID uuid) {
    int startGroup = data.playtimeGroup();
    int nextGroup = nextPlaytimeGroup(startGroup);
    int playTime = (int) ((float) Bukkit.getPlayer(uuid).getStatistic(Statistic.PLAY_ONE_MINUTE) / (float) 1200);
    if (nextGroup != -2) {
      IncreasePlaytimeGroupTask task = new IncreasePlaytimeGroupTask(data, uuid, nextGroup, this);
      task.runTaskLater(RISAnnouncer.getInstance(), (startGroup - playTime) * 60 * 20L);
      putTask(uuid, task.getTaskId());
    }
  }
}
