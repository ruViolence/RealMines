package joserodpt.realmines.utils;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.RealMines;
import joserodpt.realmines.config.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerInput implements Listener {

    private static final Map<UUID, PlayerInput> inputs = new HashMap<>();
    private final UUID uuid;

    private final List<String> texts = Text
            .color(Language.file().getStringList("System.Type-Input"));

    private final InputRunnable runGo;
    private final InputRunnable runCancel;
    private final BukkitTask taskId;

    public PlayerInput(final Player p, final InputRunnable correct, final InputRunnable cancel) {
        this.uuid = p.getUniqueId();
        p.closeInventory();
        this.runGo = correct;
        this.runCancel = cancel;
        this.taskId = new BukkitRunnable() {
            public void run() {
                p.getPlayer().sendTitle(PlayerInput.this.texts.get(0), PlayerInput.this.texts.get(1), 0, 21, 0);
            }
        }.runTaskTimer(RealMines.getPlugin(), 0L, 20);

        this.register();
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onPlayerChat(final AsyncPlayerChatEvent event) {
                final Player p = event.getPlayer();
                final String input = event.getMessage();
                final UUID uuid = p.getUniqueId();
                if (inputs.containsKey(uuid)) {
                    event.setCancelled(true);

                    final PlayerInput current = inputs.get(uuid);
                    try {
                        if (input.equalsIgnoreCase("cancel")) {
                            Text.send(p, Language.file().getString("System.Input-Cancelled"));
                            current.taskId.cancel();
                            p.sendTitle("", "", 0, 1, 0);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.getPlugin(), () -> current.runCancel.run(input), 3);
                            current.unregister();
                            return;
                        }

                        current.taskId.cancel();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.getPlugin(), () -> current.runGo.run(input), 3);
                        p.sendTitle("", "", 0, 1, 0);
                        current.unregister();
                    } catch (final Exception e) {
                        Text.send(p, Language.file().getString("System.Error-Occurred"));
                        RealMines.getPlugin().log(Level.WARNING, e.getMessage());
                    }
                }
            }
        };
    }

    private void register() {
        inputs.put(this.uuid, this);
    }

    private void unregister() {
        inputs.remove(this.uuid);
    }

    @FunctionalInterface
    public interface InputRunnable {
        void run(String input);
    }
}
