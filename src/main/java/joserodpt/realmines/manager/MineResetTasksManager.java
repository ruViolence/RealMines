package joserodpt.realmines.manager;

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
import joserodpt.realmines.config.MineResetTasks;
import joserodpt.realmines.mine.RMine;
import joserodpt.realmines.mine.task.MineResetTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MineResetTasksManager {

    private final RealMines rm;
    public List<MineResetTask> tasks = new ArrayList<>();

    public MineResetTasksManager(final RealMines rm) {
        this.rm = rm;
    }

    public void addTask(final String name, final Integer i) {
        this.tasks.add(new MineResetTask(name, i, true));
    }

    public void loadTasks() {
        if (MineResetTasks.file().isSection("")) {
            for (final String s : MineResetTasks.file().getSection("").getRoutesAsStrings(false)) {
                final int interval = MineResetTasks.file().getInt(s + ".Delay");

                final MineResetTask mrt = new MineResetTask(s, interval, false);

                for (final String s1 : MineResetTasks.file().getStringList(s + ".LinkedMines")) {
                    final RMine m = this.rm.getMineManager().getMine(s1);
                    if (m != null) {
                        mrt.addMine(m);
                    }
                }

                this.tasks.add(mrt);
            }
        }
    }

    public MineResetTask getTask(final String name) {
        return this.tasks.stream()
                .filter(task -> task.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<String> getRegisteredTasks() {
        MineResetTasks.reload();
        return this.tasks.stream()
                .map(MineResetTask::getName)
                .collect(Collectors.toList());
    }

    public void removeTask(final MineResetTask mrt) {
        final String name = mrt.getName();
        mrt.stopTimer();
        mrt.clearLinks();
        this.tasks.remove(mrt);
        MineResetTasks.file().set(name, null);
        MineResetTasks.save();
    }

    public List<MineResetTask> getTasks() {
        return this.tasks;
    }
}
