package josegamerpt.realmines.mine;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.mine.component.MineBlock;
import josegamerpt.realmines.mine.component.MineCuboid;
import josegamerpt.realmines.mine.component.MineSign;
import josegamerpt.realmines.mine.gui.MineBlockIcon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockMine extends RMine {
    private final ArrayList<MineBlock> blocks;
    private final ArrayList<Material> sorted = new ArrayList<>();

    public BlockMine(final String n, final String displayname, final ArrayList<MineBlock> b, final ArrayList<MineSign> si, final Location p1, final Location p2, final Material i,
                     final Location t, final Boolean resetByPercentag, final Boolean resetByTim, final int rbpv, final int rbtv, final String color, final HashMap<MineCuboid.CuboidDirection, Material> faces, final boolean silent) {
        super(n, displayname, si, i, t, resetByPercentag, resetByTim, rbpv, rbtv, color, faces, silent);

        this.blocks = b;

        super.setPOS(p1, p2);
        this.updateSigns();
    }

    @Override
    public void fill() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            this.sortBlocks();
            if (this.blocks.size() != 0) {

                Bukkit.getScheduler().runTask(RealMines.getInstance(), () -> {
                    //blocks
                    this.mineCuboid.forEach(block -> block.setType(this.getBlock()));
                    //faces
                    for (final Map.Entry<MineCuboid.CuboidDirection, Material> pair : this.faces.entrySet()) {
                        this.mineCuboid.getFace(pair.getKey()).forEach(block -> block.setType(pair.getValue()));
                    }
                });
            }
        }
    }

    @Override
    public String getType() {
        return "BLOCKS";
    }

    private void sortBlocks() {
        this.sorted.clear();

        for (final MineBlock d : this.blocks) {
            final double percentage = d.getPercentage() * this.getBlockCount();

            for (int i = 0; i <= (int) percentage; ++i) {
                if (this.sorted.size() != this.getBlockCount()) {
                    this.sorted.add(d.getMaterial());
                }
            }
        }
    }

    private Material getBlock() {
        final Material m;
        final Random rand = new Random();
        if (this.sorted.size() > 0) {
            m = this.sorted.get(rand.nextInt(this.sorted.size()));
            this.sorted.remove(m);
        } else {
            m = Material.AIR;
        }
        return m;
    }

    public ArrayList<String> getBlockList() {
        final ArrayList<String> l = new ArrayList<>();
        this.blocks.forEach(mineBlock -> l.add(mineBlock.getMaterial().name() + ";" + mineBlock.getPercentage()));
        return l;
    }

    public ArrayList<MineBlockIcon> getBlocks() {
        final ArrayList<MineBlockIcon> l = new ArrayList<>();
        this.blocks.forEach(mineBlock -> l.add(new MineBlockIcon(mineBlock)));
        if (l.size() == 0) {
            l.add(new MineBlockIcon());
        }
        return l;
    }

    public void removeBlock(final MineBlock mb) {
        this.blocks.remove(mb);
        this.saveData(Data.BLOCKS);
    }

    public void addBlock(final MineBlock mineBlock) {
        if (!this.contains(mineBlock)) {
            this.blocks.add(mineBlock);
            this.saveData(Data.BLOCKS);

            this.blocks.sort((a, b) -> {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return Double.compare(b.getPercentage(), a.getPercentage());
            });

        }
    }

    private boolean contains(final MineBlock mineBlock) {
        for (final MineBlock block : this.blocks) {
            if (block.getMaterial() == mineBlock.getMaterial()) {
                return true;
            }
        }
        return false;
    }
}
