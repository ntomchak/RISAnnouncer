package io.github.ntomchak;

import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

public class IncreasePlaytimeGroupTask extends BukkitRunnable {

  private PlayerData data;
  private UUID uuid;
  private IncreasePlaytimeScheduler scheduler;
  private int next;

  public IncreasePlaytimeGroupTask(PlayerData data, UUID uuid, int next, IncreasePlaytimeScheduler scheduler) {
    this.data = data;
    this.uuid = uuid;
    this.next = next;
    this.scheduler = scheduler;
  }

  @Override
  public void run() {
    data.setPlaytimeGroup(next);
    scheduler.scheduleNextIncrease(data, uuid);
  }

}
