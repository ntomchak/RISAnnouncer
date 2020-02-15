package ws.toomanyco.risannouncer;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class AnnounceTask extends BukkitRunnable {
  
  private PlayerData data;
  private UUID uuid;
  private AnnouncementScheduler scheduler;
  
  public AnnounceTask(PlayerData data, UUID uuid, AnnouncementScheduler scheduler) {
    this.data = data;
    this.uuid = uuid;
    this.scheduler = scheduler;
  }
  
  @Override
  public void run() {
    Bukkit.getPlayer(uuid).sendMessage(data.getRandomMessage());
    scheduler.scheduleNextAnnouncement(data, uuid);
  }
}
