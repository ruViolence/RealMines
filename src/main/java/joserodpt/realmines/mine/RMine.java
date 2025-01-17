package joserodpt.realmines.mine;

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
import joserodpt.realmines.config.Config;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.event.MineBlockBreakEvent;
import joserodpt.realmines.gui.BlockPickerGUI;
import joserodpt.realmines.manager.MineManager;
import joserodpt.realmines.mine.components.MineColor;
import joserodpt.realmines.mine.components.MineCuboid;
import joserodpt.realmines.mine.components.MineSign;
import joserodpt.realmines.mine.components.items.MineItem;
import joserodpt.realmines.mine.task.MineTimer;
import joserodpt.realmines.utils.Items;
import joserodpt.realmines.utils.Text;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class RMine {

    public enum Type { BLOCKS, SCHEMATIC, FARM }

    protected String name;
    protected MineColor color;
    protected String displayName;
    protected List<MineSign> signs;
    protected Map<Material, MineItem> mineItems;
    protected Location teleport;
    protected Material icon;
    protected MineCuboid mineCuboid;
    protected boolean resetByPercentage;
    protected boolean resetByTime;
    protected int resetByPercentageValue;
    protected int resetByTimeValue;
    protected MineTimer timer;
    protected boolean highlight = false;
    protected boolean silent;
    protected HashMap<MineCuboid.CuboidDirection, Material> faces;
    protected int minedBlocks;
    protected boolean freezed, breakingPermissionOn;
    private final MineManager mm;
    private World w;

    public RMine(final World w, final String n, final String displayname, final List<MineSign> si, final Map<Material, MineItem> b, final Material i,
                 final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final MineColor color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent, final boolean breakingPermissionOn, final MineManager mm) {
        this.mm = mm;
        this.w = w;
        this.name = ChatColor.stripColor(Text.color(n));
        this.color = color;
        this.displayName = displayname;
        this.silent = silent;
        this.signs = si;
        this.mineItems = b;
        this.icon = i;
        this.teleport = t;
        this.resetByPercentage = resetByPercentag;
        this.resetByTime = resetByTim;
        this.resetByPercentageValue = rbpv;
        this.resetByTimeValue = rbtv;
        this.faces = faces;
        this.breakingPermissionOn = breakingPermissionOn;

        this.timer = new MineTimer(this);
        if (this.resetByTime) {
            this.timer.start();
        }
    }

    public MineColor getMineColor() {
        return this.color;
    }

    public void setMineColor(MineColor color) {
        this.color = color;
    }

    public boolean hasFaceBlock(final MineCuboid.CuboidDirection up) {
        return this.faces.get(up) != null;
    }

    public MineTimer getMineTimer() {
        return this.timer;
    }

    public Location getPOS1() { return this.getMineCuboid().getPOS1(); }

    public Location getPOS2() {
        return this.getMineCuboid().getPOS2();
    }

    public void setPOS(final Location p1, final Location p2) {
        this.mineCuboid = new MineCuboid(p1, p2);
    }

    public boolean hasTP() {
        return this.teleport != null;
    }

    public List<String> getSignList() {
        return this.getSigns().stream().map(mineSign -> mineSign.getBlock().getWorld().getName() + ";" + mineSign.getBlock().getX() + ";" + mineSign.getBlock().getY() + ";" + mineSign.getBlock().getZ() + ";" + mineSign.getModifier()).collect(Collectors.toList());
    }

    public String getBar() {
        return Text.getProgressBar(this.getRemainingBlocks(), this.getBlockCount(), 10, '■', ChatColor.GREEN, ChatColor.RED);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(final String input) {
        this.displayName = input;
        this.saveData(Data.NAME);
    }

    public MineCuboid getMineCuboid() {
        return this.mineCuboid;
    }

    //block counts
    public int getBlockCount() {
        return this.getMineCuboid().getTotalBlocks();
    }

    public int getMinedBlocks() {
        return this.minedBlocks;
    }

    public int getRemainingBlocks() {
        return this.getBlockCount() - this.getMinedBlocks();
    }
    //block counts

    //block percentages
    public int getRemainingBlocksPer() {
        return this.getBlockCount() == 0 ? 0 : (this.getRemainingBlocks() * 100 / this.getBlockCount());
    }

    public int getMinedBlocksPer() {
        return this.getBlockCount() == 0 ? 0 : (this.getMinedBlocks() * 100 / this.getBlockCount());
    }
    //block percentages

    public abstract void fill();

    public Map<Material, MineItem> getMineItems() {
        return this.mineItems;
    }

    public List<MineItem> getBlockIcons() {
        return this.getMineItems().isEmpty() ? new ArrayList<>(Collections.singletonList(new MineItem())) :
                new ArrayList<>(this.getMineItems().values());
    }

    public void saveData(final Data t) {
        this.mm.saveMine(this, t);
        if (!this.resetByTime) {
            this.timer.kill();
        } else {
            this.timer.restart();
        }
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void saveAll() {
        this.mm.saveAllMineData(this);
        if (!this.resetByTime) {
            this.timer.kill();
        } else {
            this.timer.restart();
        }
    }

    public void reset() {
        if (!Bukkit.getOnlinePlayers().isEmpty() || Config.file().getBoolean("RealMines.resetMinesWhenNoPlayers")) {
            this.kickPlayers(Language.file().getString("Mines.Reset.Starting").replace("%mine%", this.getDisplayName()));
            this.fill();

            //reset mined blocks
            this.minedBlocks = 0;
            processBlockBreakEvent(false);

            if (!this.isSilent()) {
                if (Config.file().getBoolean("RealMines.broadcastResetMessageOnlyInWorld")) {
                    this.getMineCuboid().getWorld().getPlayers().forEach(player -> Text.send(player, Language.file().getString("Mines.Reset.Announcement").replace("%mine%", this.getDisplayName())));
                } else {
                    Bukkit.broadcastMessage(Text.color(Config.file().getString("RealMines.Prefix") + Language.file().getString("Mines.Reset.Announcement").replace("%mine%", this.getDisplayName())));
                }
            }
        }
    }

    public void addSign(final Block block, final String modif) {
        this.signs.add(new MineSign(block, modif));
        this.saveData(Data.SIGNS);
    }

    public void updateSigns() {
        Bukkit.getScheduler().runTask(RealMines.getPlugin(), () -> {
            for (final MineSign ms : this.signs) {
                if (ms.getBlock().getType().name().contains("SIGN")) {
                    final Sign sign = (Sign) ms.getBlock().getState();
                    final String modif = ms.getModifier();

                    switch (modif.toLowerCase()) {
                        case "pm":
                            sign.setLine(1, this.getMinedBlocksPer() + "%");
                            sign.setLine(2, Text.color(Language.file().getString("Signs.Mined-On")));
                            break;
                        case "bm":
                            sign.setLine(1, String.valueOf(this.getMinedBlocks()));
                            sign.setLine(2, Text.color(Language.file().getString("Signs.Mined-Blocks-On")));
                            break;
                        case "br":
                            sign.setLine(1, String.valueOf(this.getRemainingBlocks()));
                            sign.setLine(2, Text.color(Language.file().getString("Signs.Blocks-On")));
                            break;
                        case "pl":
                            sign.setLine(1, this.getRemainingBlocksPer() + "%");
                            sign.setLine(2, Text.color(Language.file().getString("Signs.Left-On")));
                            break;
                    }

                    sign.setLine(0, Text.color(Config.file().getString("RealMines.Prefix")));
                    sign.setLine(3, Text.color(this.getDisplayName()));
                    sign.update();
                }
            }
        });
    }

    public void clear() {
        this.minedBlocks = 0;
        processBlockBreakEvent(false);

        this.clearContents();
    }

    public boolean isSilent() {
        return this.silent;
    }

    public void kickPlayers(final String s) {
        if (this.getType() != Type.FARM) {
            if (Config.file().getBoolean("RealMines.teleportPlayers")) {
                this.getPlayersInMine().forEach(player -> this.mm.teleport(player, this, this.isSilent()));
            }
        }
        if (!this.isSilent()) {
            this.broadcastMessage(s);
        }
    }

    public void broadcastMessage(String s) {
        this.getPlayersInMine().forEach(p -> Text.send(p, s));
        if (Config.file().getBoolean("RealMines.actionbarMessages"))
            this.getPlayersInMine().forEach(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Text.color(s))));
    }

    public List<Player> getPlayersInMine() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> this.mineCuboid.contains(p.getLocation()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void removeDependencies() {
        this.signs.forEach(ms -> ms.getBlock().getLocation().getWorld().getBlockAt(ms.getBlock().getLocation()).setType(Material.AIR));
    }

    public List<Location> getHighlightedCube() {
        final List<Location> result = new ArrayList<>();
        final World world = this.mineCuboid.getPOS1().getWorld();
        final double minX = Math.min(this.mineCuboid.getPOS1().getX(), this.mineCuboid.getPOS2().getX());
        final double minY = Math.min(this.mineCuboid.getPOS1().getY(), this.mineCuboid.getPOS2().getY());
        final double minZ = Math.min(this.mineCuboid.getPOS1().getZ(), this.mineCuboid.getPOS2().getZ());
        final double maxX = Math.max(this.mineCuboid.getPOS1().getX() + 1, this.mineCuboid.getPOS2().getX() + 1);
        final double maxY = Math.max(this.mineCuboid.getPOS1().getY() + 1, this.mineCuboid.getPOS2().getY() + 1);
        final double maxZ = Math.max(this.mineCuboid.getPOS1().getZ() + 1, this.mineCuboid.getPOS2().getZ() + 1);
        final double dist = 0.5D;
        for (double x = minX; x <= maxX; x += dist) {
            for (double y = minY; y <= maxY; y += dist) {
                for (double z = minZ; z <= maxZ; z += dist) {
                    int components = 0;
                    if (x == minX || x == maxX) ++components;
                    if (y == minY || y == maxY) ++components;
                    if (z == minZ || z == maxZ) ++components;
                    if (components >= 2) {
                        result.add(new Location(world, x, y, z));
                    }
                }
            }
        }
        return result;
    }

    public World getWorld() {
        return this.w;
    }

    public void highlight() {
        if (this.highlight) {
            this.getHighlightedCube().forEach(location -> location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 0, 0.001, 1, 0, 1, new Particle.DustOptions(this.getMineColor().getColor(), 1)));
        }
    }

    public String getName() {
        return this.name;
    }

    public boolean isResetBy(final Reset e) {
        switch (e) {
            case PERCENTAGE:
                return this.resetByPercentage;
            case TIME:
                return this.resetByTime;
            case SILENT:
                return this.silent;
        }
        return false;
    }

    public int getResetValue(final Reset e) {
        switch (e) {
            case PERCENTAGE:
                return this.resetByPercentageValue;
            case TIME:
                return this.resetByTimeValue;
        }
        return -1;
    }

    public void setResetStatus(final Reset e, final boolean b) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentage = b;
            case TIME:
                this.resetByTime = b;
            case SILENT:
                this.silent = b;
        }
    }

    public void setResetValue(final Reset e, final int d) {
        switch (e) {
            case PERCENTAGE:
                this.resetByPercentageValue = d;
                break;
            case TIME:
                this.resetByTimeValue = d;
                break;
        }
    }

    public Material getIcon() {
        return this.icon;
    }

    public ItemStack getMineIcon() {
        return Items.createItemLore(this.getIcon(), 1, this.getMineColor().getColorPrefix() + " &f&l" + this.getDisplayName() + " &7[&b&l" + this.getType().name() + "&r&7]", Language.file().getStringList("GUI.Items.Mine.Description")
                .stream()
                .map(s -> Text.color(s
                        .replaceAll("%remainingblocks%", String.valueOf(this.getRemainingBlocks()))
                        .replaceAll("%totalblocks%", String.valueOf(this.getBlockCount()))
                        .replaceAll("%bar%", this.getBar())))
                .collect(Collectors.toList()));
    }

    public void setIcon(final Material a) {
        this.icon = a;
    }

    public boolean isHighlighted() {
        return this.highlight;
    }

    public void setHighlight(final boolean b) {
        this.highlight = b;
    }

    public Location getTeleport() {
        return this.teleport;
    }

    public void setTeleport(final Location location) {
        this.teleport = location;
    }

    public List<MineSign> getSigns() {
        return this.signs;
    }

    public MineTimer getTimer() {
        return this.timer;
    }

    public Material getFaceBlock(final MineCuboid.CuboidDirection up) {
        return this.faces.get(up);
    }

    public void setFaceBlock(final MineCuboid.CuboidDirection cd, final Material a) {
        this.faces.put(cd, a);
        this.saveData(Data.FACES);
    }

    public void removeFaceblock(final MineCuboid.CuboidDirection d) {
        this.faces.remove(d);
        this.saveData(Data.FACES);
    }

    public HashMap<MineCuboid.CuboidDirection, Material> getFaces() {
        return this.faces;
    }

    public abstract RMine.Type getType();

    public void processBlockBreakAction(final Material m, final Player p, final Location l, final Double random) {
        if (this.getMineItems().containsKey(m)) {
            this.getMineItems().get(m).getBreakActions().forEach(mineAction -> mineAction.execute(p, l, random));
        }
    }

    public void processBlockBreakEvent(final MineBlockBreakEvent event, final boolean reset) {
        //add or remove to mined blocks
        this.minedBlocks = this.minedBlocks + (event.isBroken() ? 1 : -1);

        if (this.minedBlocks < 0) {
            this.minedBlocks = 0;
        }

        if (event.getPlayer() != null) {
            processBlockBreakAction(event.getBlock().getType(), event.getPlayer(), event.getBlock().getLocation(), RealMines.getPlugin().getRand().nextDouble() * 100);
        }

        processBlockBreakEvent(reset);
    }

    private void processBlockBreakEvent(boolean reset) {
        if (reset) {
            //if mine reset percentage is lower, reset it
            if (this.isResetBy(RMine.Reset.PERCENTAGE) & ((double) this.getRemainingBlocksPer() < this.getResetValue(RMine.Reset.PERCENTAGE))) {
                this.kickPlayers(Language.file().getString("Mines.Reset.Percentage"));
                Bukkit.getScheduler().scheduleSyncDelayedTask(RealMines.getPlugin(), this::reset, 10);
            }
        }

        //update min e signs
        this.updateSigns();
    }

    public abstract BlockPickerGUI.PickType getBlockPickType();

    public abstract void clearContents();

    public boolean isFreezed() {
        return this.freezed;
    }

    public void setFreezed(boolean freezed) {
        this.freezed = freezed;
    }

    public void setBreakingPermissionOn(boolean breakingPermissionOn) {
        this.breakingPermissionOn = breakingPermissionOn;
    }

    public boolean isBreakingPermissionOn() {
        return this.breakingPermissionOn;
    }

    public String getBreakPermission() {
        return "realmines." + this.getName() + ".break";
    }

    public enum Reset {PERCENTAGE, TIME, SILENT}

    public enum Data {BLOCKS, ICON, TELEPORT, SIGNS, LOCATION, SETTINGS, NAME, FACES, COLOR, MINE_TYPE}
}
