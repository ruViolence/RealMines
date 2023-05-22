package josegamerpt.realmines.mine.gui;

import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mine.RMine;
import josegamerpt.realmines.util.Items;
import josegamerpt.realmines.util.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MineIcon {

    private RMine m;
    private ItemStack i;
    private Boolean placeholder = false;

    public MineIcon(final RMine min) {
        this.m = min;
		this.makeIcon();
    }

    public MineIcon() {
        this.placeholder = true;
        this.i = Items.createItemLore(Material.DEAD_BUSH, 1, Language.file().getString("GUI.Items.No-Mines-Found.Name"), Language.file().getStringList("GUI.Items.No-Mines-Found.Description"));
    }

    private void makeIcon() {
        this.i = Items.createItemLore(this.m.getIcon(), 1, this.m.getColorIcon() + " &6&l" + this.m.getDisplayName() + " &f- &b&l" + this.m.getType(), this.var(this.m));
    }

    private List<String> var(final RMine m) {
        final List<String> ret = new ArrayList<>();
        Language.file().getStringList("GUI.Items.Mine.Description").forEach(s -> ret.add(Text.color(s.replaceAll("%remainingblocks%", String.valueOf(m.getRemainingBlocks())).replaceAll("%totalblocks%", String.valueOf(m.getBlockCount())).replaceAll("%bar%", this.getBar(m)))));
        return ret;
    }

    private String getBar(final RMine m) {
        return Text.getProgressBar(m.getRemainingBlocks(), m.getBlockCount(), 10, '■', ChatColor.GREEN, ChatColor.RED);
    }


    public boolean isPlaceholder() {
        return this.placeholder;
    }

    public RMine getMine() {
        return this.m;
    }

    public ItemStack getIcon() {
        return this.i;
    }
}