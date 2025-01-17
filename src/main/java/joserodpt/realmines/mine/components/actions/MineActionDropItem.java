package joserodpt.realmines.mine.components.actions;

import joserodpt.realmines.config.Language;
import joserodpt.realmines.config.Mines;
import joserodpt.realmines.utils.ItemStackSpringer;
import joserodpt.realmines.utils.Items;
import joserodpt.realmines.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class MineActionDropItem extends MineAction {

    private ItemStack i;
    public MineActionDropItem(final String id, final String mineID, final Double chance, final ItemStack i) {
        super(id, mineID, chance);
        this.i = i;
    }

    public void execute(final Player p, final Location l, final double randomChance) {
        if (randomChance < super.getChance()) {
            Objects.requireNonNull(l.getWorld()).dropItemNaturally(l, this.i);
            if (Mines.file().getBoolean(super.getMineID() + ".Settings.Discard-Break-Action-Messages"))
                Text.send(p, Language.file().getString("Mines.Break-Actions.Drop-Item"));
        }
    }

    @Override
    public MineAction.Type getType() {
        return Type.DROP_ITEM;
    }

    @Override
    public String getValue() {
        return ItemStackSpringer.getItemSerializedJSON(this.i);
    }

    @Override
    public ItemStack getItem() {
        return Items.createItemLore(Material.DROPPER, 1, "&b&lDrop Item &r&f- " + super.getChance() + "%" , Arrays.asList("&fItem: &bx" + this.i.getAmount() + " " + Text.beautifyMaterialName(this.i.getType()), "","&b&nLeft-Click&r&f to change the chance.", "&e&nRight-Click&r&f to change the item.", "&c&nQ (Drop)&r&f to remove this action.", "&8ID: " + getID()));
    }

    public void setItem(ItemStack itemInMainHand) {
        if (itemInMainHand.getType() == Material.AIR) {
            return;
        }
        this.i = itemInMainHand;
    }

    @Override
    public String toString() {
        return "MineActionItem{" +
                "i=" + i +
                ", chance=" + super.getChance() +
                '}';
    }
}
