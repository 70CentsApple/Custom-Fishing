package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.Function;
import net.momirealms.customfishing.competition.CompetitionConfig;
import net.momirealms.customfishing.competition.CompetitionGoal;
import net.momirealms.customfishing.competition.bossbar.BossBarConfig;
import net.momirealms.customfishing.competition.bossbar.Overlay;
import net.momirealms.customfishing.object.action.ActionInterface;
import net.momirealms.customfishing.object.action.CommandActionImpl;
import net.momirealms.customfishing.object.action.MessageActionImpl;
import net.momirealms.customfishing.util.ConfigUtil;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.units.qual.C;

import java.util.*;

public class CompetitionManager extends Function {

    public static HashMap<String, CompetitionConfig> competitionsT;
    public static HashMap<String, CompetitionConfig> competitionsC;

    @Override
    public void load() {
        competitionsC = new HashMap<>();
        competitionsT = new HashMap<>();
        if (ConfigManager.enableCompetition) loadCompetitions();
    }

    @Override
    public void unload() {
        if (competitionsC != null) competitionsC.clear();
        if (competitionsT != null) competitionsT.clear();
    }

    public void loadCompetitions(){
        YamlConfiguration config = ConfigUtil.getConfig("competition.yml");
        Set<String> keys = config.getKeys(false);
        keys.forEach(key -> {
            boolean enableBsb = config.getBoolean(key + ".bossbar.enable", false);
            BossBarConfig bossBarConfig = new BossBarConfig(
                    config.getStringList(key + ".bossbar.text").toArray(new String[0]),
                    Overlay.valueOf(config.getString(key + ".bossbar.overlay","SOLID").toUpperCase()),
                    BarColor.valueOf(config.getString(key + ".bossbar.color","WHITE").toUpperCase()),
                    config.getInt(key + ".bossbar.refresh-rate",10),
                    config.getInt(key + ".bossbar.switch-interval", 15)
            );

            HashMap<String, ActionInterface[]> rewardsMap = new HashMap<>();
            Objects.requireNonNull(config.getConfigurationSection(key + ".prize")).getKeys(false).forEach(rank -> {
                List<ActionInterface> rewards = new ArrayList<>();
                if (config.contains(key + ".prize." + rank + ".messages"))
                    rewards.add(new MessageActionImpl(config.getStringList(key + ".prize." + rank + ".messages").toArray(new String[0]), null));
                if (config.contains(key + ".prize." + rank + ".commands"))
                    rewards.add(new CommandActionImpl(config.getStringList(key + ".prize." + rank + ".commands").toArray(new String[0]), null));
                rewardsMap.put(rank, rewards.toArray(new ActionInterface[0]));
            });

            CompetitionConfig competitionConfig = new CompetitionConfig(
                    config.getInt(key + ".duration",600),
                    config.getInt(key + ".min-players",1),
                    config.getStringList(key + ".broadcast.start"),
                    config.getStringList(key + ".broadcast.end"),
                    config.getStringList(key + ".command.start"),
                    config.getStringList(key + ".command.end"),
                    config.getStringList(key + ".command.join"),
                    CompetitionGoal.valueOf(config.getString(key + ".goal", "RANDOM")),
                    bossBarConfig,
                    enableBsb,
                    rewardsMap
            );

            config.getStringList(key + ".start-time").forEach(time -> competitionsT.put(time, competitionConfig));
            competitionsC.put(key, competitionConfig);
        });
    }
}