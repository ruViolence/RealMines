package joserodpt.realmines.command;

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
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.mine.task.MineResetTask;
import joserodpt.realmines.utils.Text;
import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.annotations.WrongUsage;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command("realminesresettask")
@Alias({"minesresettask", "rmrt"})
public class MineResetTaskCMD extends CommandBase {

    String playerOnly = "[RealMines] Only players can run this command.";

    private final RealMines rm;

    public MineResetTaskCMD(final RealMines rm) {
        this.rm = rm;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender,
                Arrays.asList("", "         &9Real&bMines", "         &7Release &a" + rm.getDescription().getVersion(), ""));
    }

    @SubCommand("create")
    @Completion({"#minetasksuggestions", "#range:300"})
    @Permission("realmines.admin")
    @WrongUsage("&c/rmrt create <name> <delay>")
    public void createcmd(final CommandSender commandSender, final String name, final Integer delay) {
        if (commandSender instanceof Player) {
            final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(ChatColor.stripColor(Text.color(name)));
            if (mrt == null) {
                this.rm.getMineResetTasksManager().addTask(ChatColor.stripColor(Text.color(name)), delay);
                Text.send(commandSender, Language.file().getString("System.Mine-Task-Created").replace("%task%", name).replace("%delay%", String.valueOf(delay)));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Task-Exists"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
        }
    }

    @SubCommand("remove")
    @Completion("#minetasks")
    @Permission("realmines.admin")
    @WrongUsage("&c/rmrt create <name> <delay>")
    public void removecmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(name);
            if (mrt != null) {
                this.rm.getMineResetTasksManager().removeTask(mrt);
                Text.send(commandSender, Language.file().getString("System.Remove").replace("%object%", name));
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Task-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
        }
    }

    @SubCommand("link")
    @Completion({"#minetasks", "#mines"})
    @Permission("realmines.admin")
    @WrongUsage("&c/rmrt link <taskname> <mine>")
    public void linkcmd(final CommandSender commandSender, final String name, final String mine) {
        if (commandSender instanceof Player) {
            final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(name);
            if (mrt != null) {
                final RMine m = this.rm.getMineManager().getMine(mine);
                if (m != null) {
                    mrt.addMine(m);
                    Text.send(commandSender, Language.file().getString("System.Mine-Linked"));
                } else {
                    Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
                }
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Task-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
        }
    }

    @SubCommand("unlink")
    @Completion({"#minetasks", "#mines"})
    @Permission("realmines.admin")
    @WrongUsage("&c/rmrt unlink <taskname> <mine>")
    public void unlinkcmd(final CommandSender commandSender, final String name, final String mine) {
        if (commandSender instanceof Player) {
            final MineResetTask mrt = this.rm.getMineResetTasksManager().getTask(name);
            if (mrt != null) {
                final RMine m = this.rm.getMineManager().getMine(mine);
                if (m != null) {
                    mrt.removeMine(m);
                    Text.send(commandSender, Language.file().getString("System.Mine-Unlinked"));
                } else {
                    Text.send(commandSender, Language.file().getString("System.Mine-Doesnt-Exist"));
                }
            } else {
                Text.send(commandSender, Language.file().getString("System.Mine-Task-Doesnt-Exist"));
            }
        } else {
            Text.send(commandSender, this.playerOnly);
        }
    }

}