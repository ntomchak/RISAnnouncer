package io.github.ntomchak;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.bukkit.configuration.file.FileConfiguration;

public class PlayerData {
  private EnumeratedDistribution<String> groupsNotIn;
  private List<Pair<String, Double>> groups;
//-1 playtimeGroup means they are higher than the highest time
  private int playTimeGroup;
  private Random r;

  public PlayerData(List<Pair<String, Double>> groups, int playtimeGroup) {
    this.groups = groups;
    if (groups.size() > 0)
      this.groupsNotIn = new EnumeratedDistribution<String>(groups);
    r = new Random();
    playTimeGroup = playtimeGroup;
  }

  public int playtimeGroup() {
    return playTimeGroup;
  }

  public void updateGroups(List<Pair<String, Double>> groups) {
    this.groups = groups;
    if (groups.size() > 0)
      this.groupsNotIn = new EnumeratedDistribution<String>(groups);
  }

  public void setPlaytimeGroup(int newGroup) {
    playTimeGroup = newGroup;
  }

  public String toString() {
    String msg = "groups not in: ";
    // fix this later, there's a nullpointerexception when theyre in every group
    for (Pair<String, Double> group : groupsNotIn.getPmf()) {
      msg += group.getFirst() + "(" + group.getSecond() + "), ";
    }
    msg += "\n";
    msg += "play time group: " + playTimeGroup;
    return msg;
  }

  public String getRandomMessage() {
    FileConfiguration config = RISAnnouncer.getInstance().getConfig();

    String path = "byPlaytime." + getGroupPath();
    int promotion = config.getInt(path + ".promotionRatio");
    int groupSpecific = config.getInt(path + ".groupSpecificRatio");
    int information = config.getInt(path + ".informationRatio");
    int totalProbability = promotion + groupSpecific + information;
    int rand = r.nextInt(totalProbability);
    if (rand < promotion && groups.size() > 0)
      return getPromotionMessage();
    else if (rand < promotion + information)
      return getInformationMessage();
    else
      return getGroupSpecificMessage();
  }

  public String getGroupPath() {
    return playTimeGroup == -1 ? "aboveHighestTime" : Integer.toString(playTimeGroup);
  }

  private String getGroupSpecificMessage() {
    return getRandMsgFromList("byPlaytime." + getGroupPath() + ".groupSpecificMessages");
  }

  private String getPromotionMessage() {
    if (this.groups.size() < 2) {
      return getRandMsgFromList("promotionMessages.groups." + groupsNotIn.sample() + ".messages");
    } else if (r.nextFloat() < RISAnnouncer.getInstance().getConfig().getDouble("promotionMessages.listProportion")) {
      String[] groups = new String[2];
      groupsNotIn.sample(2, groups);
      FileConfiguration config = RISAnnouncer.getInstance().getConfig();
      String te1 = config.getString("promotionMessages.listTemplate1");
      String te2 = config.getString("promotionMessages.listTemplate2");
      String te3 = config.getString("promotionMessages.listTemplate3");
      if (groups[0].equals(groups[1])) {
        if (this.groups.size() == 2) {
          groups[1] = this.groups.get(0).getFirst();
          if (groups[0].equals(groups[1]))
            groups[1] = this.groups.get(1).getFirst();
        } else {
          int count = 0;
          boolean cont = true;
          while (count < 13 && groups[0].equals(groups[1])) {
            count++;
            groups[1] = this.groupsNotIn.sample();
            cont = false;
          }
          if (cont) {
            int rand = r.nextInt(this.groups.size());
            groups[1] = this.groups.get(rand).getFirst();
            if (groups[1].equals(groups[0])) {
              int next = rand + 1 % this.groups.size();
              groups[1] = this.groups.get(next).getFirst();
            }
          }
        }
      }
      String perk1 = getRandMsgFromList("promotionMessages.groups." + groups[0] + ".list");
      String perk2 = getRandMsgFromList("promotionMessages.groups." + groups[1] + ".list");
      return te1 + perk1 + te2 + perk2 + te3;
    } else {
      return getRandMsgFromList("promotionMessages.groups." + groupsNotIn.sample() + ".messages");
    }
  }

  private String getInformationMessage() {
    return getRandMsgFromList("informationMessages");
  }

  private String getRandMsgFromList(String path) {
    List<String> possibilities = RISAnnouncer.getInstance().getConfig().getStringList(path);
    return possibilities.get(r.nextInt(possibilities.size()));
  }
}
