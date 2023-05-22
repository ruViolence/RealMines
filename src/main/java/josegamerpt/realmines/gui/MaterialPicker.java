package josegamerpt.realmines.gui;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mine.BlockMine;
import josegamerpt.realmines.mine.RMine;
import josegamerpt.realmines.mine.component.MineBlock;
import josegamerpt.realmines.mine.component.MineCuboid;
import josegamerpt.realmines.util.Items;
import josegamerpt.realmines.util.Pagination;
import josegamerpt.realmines.util.PlayerInput;
import josegamerpt.realmines.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MaterialPicker {

    private static final Map<UUID, MaterialPicker> inventories = new HashMap<>();
    static ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Items.createItemLore(Material.GREEN_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Next.Name"),
            Language.file().getStringList("GUI.Items.Next.Description"));
    static ItemStack back = Items.createItemLore(Material.YELLOW_STAINED_GLASS, 1, Language.file().getString("GUI.Items.Back.Name"),
            Language.file().getStringList("GUI.Items.Back.Description"));
    static ItemStack close = Items.createItemLore(Material.ACACIA_DOOR, 1, Language.file().getString("GUI.Items.Close.Name"),
            Language.file().getStringList("GUI.Items.Close.Description"));
    static ItemStack search = Items.createItemLore(Material.OAK_SIGN, 1, Language.file().getString("GUI.Items.Search.Name"),
            Language.file().getStringList("GUI.Items.Close.Description"));
    private final RealMines rm;
    private final UUID uuid;
    private final ArrayList<Material> items;
    private final HashMap<Integer, Material> display = new HashMap<>();
    private final RMine min;
    private final PickType pt;
    int pageNumber = 0;
    Pagination<Material> p;
    private final Inventory inv;
    private final String add;

    public MaterialPicker(final RealMines rm, final RMine m, final Player pl, final PickType block, final String additional) {
        this.add = additional;
        this.rm = rm;
        this.uuid = pl.getUniqueId();
        this.min = m;
        this.pt = block;

        if (Objects.requireNonNull(block) == PickType.ICON) {
            this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(Language.file().getString("GUI.Select-Icon-Name").replaceAll("%mine%", m.getDisplayName())));
        } else {
            this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(Language.file().getString("GUI.Pick-New-Block-Name")));
        }
        this.items = Items.getValidBlocks();

        this.p = new Pagination<>(28, this.items);
        this.fillChest(this.p.getPage(this.pageNumber));

        this.register();
    }

    public MaterialPicker(final RealMines rm, final RMine m, final Player pl, final PickType block, final String search, final String additional) {
        this.add = additional;
        this.rm = rm;

        this.uuid = pl.getUniqueId();
        this.min = m;
        this.pt = block;

        if (Objects.requireNonNull(block) == PickType.ICON) {
            this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(Language.file().getString("GUI.Select-Icon-Name").replaceAll("%mine%", m.getDisplayName())));
        } else {
            this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(Language.file().getString("GUI.Pick-New-Block-Name")));
        }

        this.items = this.searchMaterial(search);

        this.p = new Pagination<>(28, this.items);
        this.fillChest(this.p.getPage(this.pageNumber));

        this.register();
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(final InventoryClickEvent e) {
                final HumanEntity clicker = e.getWhoClicked();
                if (clicker instanceof Player) {
                    if (e.getCurrentItem() == null) {
                        return;
                    }
                    final UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        final MaterialPicker current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        final Player gp = (Player) clicker;

                        switch (e.getRawSlot()) {
                            case 4:
                                new PlayerInput(gp, input -> {
                                    if (current.searchMaterial(input).size() == 0) {
                                        gp.sendMessage(Text.color(Language.file().getString("System.Nothing-Found")));
                                        current.exit(current.rm, gp);
                                        return;
                                    }
                                    final MaterialPicker df = new MaterialPicker(current.rm, current.min, gp, current.pt, input, current.add);
                                    df.openInventory(gp);
                                }, input -> {
                                    gp.closeInventory();
                                    final MineBlocksViewer v = new MineBlocksViewer(current.rm, gp, current.min);
                                    v.openInventory(gp);
                                });
                                break;
                            case 49:
                                current.exit(current.rm, gp);
                                break;
                            case 26:
                            case 35:
                                this.nextPage(current);
                                gp.playSound(gp.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                break;
                            case 18:
                            case 27:
                                this.backPage(current);
                                gp.playSound(gp.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            final Material a = current.display.get(e.getRawSlot());

                            switch (current.pt) {
                                case ICON:
                                    current.min.setIcon(a);
                                    current.min.saveData(BlockMine.Data.ICON);
                                    gp.closeInventory();
                                    current.rm.getGUIManager().openMine(current.min, gp);
                                    break;
                                case BLOCK:
                                    ((BlockMine) current.min).addBlock(new MineBlock(a));
                                    gp.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                        final MineBlocksViewer v = new MineBlocksViewer(current.rm, gp, current.min);
                                        v.openInventory(gp);
                                    }, 3);
                                    break;
                                case FACE_MATERIAL:
                                    final MineCuboid.CuboidDirection cd = MineCuboid.CuboidDirection.valueOf(current.add);
                                    current.min.setFaceBlock(cd, a);
                                    gp.closeInventory();
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(current.rm, () -> {
                                        final MineFaces v = new MineFaces(current.rm, gp, current.min);
                                        v.openInventory(gp);
                                    }, 3);
                                    break;
                            }
                        }

                        e.setCancelled(true);
                    }
                }
            }

            private void backPage(final MaterialPicker asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(final MaterialPicker asd) {
                if (asd.p.exists(asd.pageNumber + 1)) {
                    asd.pageNumber++;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            @EventHandler
            public void onClose(final InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    if (e.getInventory() == null) {
                        return;
                    }
                    final Player p = (Player) e.getPlayer();
                    final UUID uuid = p.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        inventories.get(uuid).unregister();
                    }
                }
            }
        };
    }

    private ArrayList<Material> searchMaterial(final String s) {
        final ArrayList<Material> ms = new ArrayList<>();
        for (final Material m : Items.getValidBlocks()) {
            if (m.name().toLowerCase().contains(s.toLowerCase())) {
                ms.add(m);
            }
        }
        return ms;
    }

    public void fillChest(final List<Material> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; i++) {
            this.inv.setItem(i, placeholder);
        }

        this.inv.setItem(4, search);

        this.inv.setItem(45, placeholder);
        this.inv.setItem(46, placeholder);
        this.inv.setItem(47, placeholder);
        this.inv.setItem(48, placeholder);
        this.inv.setItem(49, placeholder);
        this.inv.setItem(50, placeholder);
        this.inv.setItem(51, placeholder);
        this.inv.setItem(52, placeholder);
        this.inv.setItem(53, placeholder);
        this.inv.setItem(36, placeholder);
        this.inv.setItem(44, placeholder);
        this.inv.setItem(9, placeholder);
        this.inv.setItem(17, placeholder);

        this.inv.setItem(18, back);
        this.inv.setItem(27, back);
        this.inv.setItem(26, next);
        this.inv.setItem(35, next);

        int slot = 0;
        for (final ItemStack i : this.inv.getContents()) {
            if (i == null && items.size() != 0) {
                final Material s = items.get(0);
                this.inv.setItem(slot,
                        Items.createItemLore(s, 1, Language.file().getString("GUI.Items.Pick.Name").replaceAll("%material%", s.name()), Language.file().getStringList("GUI.Items.Pick.Description")));
                this.display.put(slot, s);
                items.remove(0);
            }
            slot++;
        }

        this.inv.setItem(49, close);
    }

    public void openInventory(final Player target) {
        final Inventory inv = this.getInventory();
        final InventoryView openInv = target.getOpenInventory();
        if (openInv != null) {
            final Inventory openTop = target.getOpenInventory().getTopInventory();
            if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
                openTop.setContents(inv.getContents());
            } else {
                target.openInventory(inv);
            }
        }
    }

    protected void exit(final RealMines rm, final Player gp) {
        switch (this.pt) {
            case ICON:
                gp.closeInventory();
                rm.getGUIManager().openMine(this.min, gp);
                break;
            case BLOCK:
                final MineBlocksViewer v = new MineBlocksViewer(rm, gp, this.min);
                v.openInventory(gp);
                break;
        }
    }

    public Inventory getInventory() {
        return this.inv;
    }

    private void register() {
        inventories.put(this.uuid, this);
    }

    private void unregister() {
        inventories.remove(this.uuid);
    }

    public enum PickType {ICON, BLOCK, FACE_MATERIAL}
}
