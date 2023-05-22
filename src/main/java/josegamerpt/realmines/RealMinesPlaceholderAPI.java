package josegamerpt.realmines;

import josegamerpt.realmines.gui.GUIManager;
import josegamerpt.realmines.mine.RMine;
import josegamerpt.realmines.util.Countdown;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * This class will be registered through the register-method in the
 * plugins onEnable-method.
 */
public class RealMinesPlaceholderAPI extends PlaceholderExpansion {

    private final RealMines plugin;
    private final int mineIndex = 1;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin The instance of our plugin.
     */
    public RealMinesPlaceholderAPI(final RealMines plugin) {
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor() {
        return this.plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier() {
        return "realmines";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     * <p>
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(final OfflinePlayer player, final String identifier) {

        // %example_placeholder1%

        if (identifier.startsWith("totalblocks")) {
            final String[] split = identifier.split("_");
            final String mine = split[this.mineIndex];
            final RMine m = this.plugin.getMineManager().get(mine);
            if (m != null) {
                return String.valueOf(plugin.getMineManager().get(mine).getBlockCount());
            } else {
                return "No mine: " + mine;
            }
        }

        if (identifier.startsWith("minedblocks")) {
            final String[] split = identifier.split("_");
            final String mine = split[this.mineIndex];
            final RMine m = this.plugin.getMineManager().get(mine);
            if (m != null) {
                return String.valueOf(plugin.getMineManager().get(mine).getMinedBlocks());
            } else {
                return "No mine: " + mine;
            }
        }

        if (identifier.startsWith("remainingblocks")) {
            final String[] split = identifier.split("_");
            final String mine = split[this.mineIndex];
            final RMine m = this.plugin.getMineManager().get(mine);
            if (m != null) {
                return String.valueOf(plugin.getMineManager().get(mine).getRemainingBlocks());
            } else {
                return "No mine: " + mine;
            }
        }

        if (identifier.startsWith("perremainingblocks")) {
            final String[] split = identifier.split("_");
            final String mine = split[this.mineIndex];
            final RMine m = this.plugin.getMineManager().get(mine);
            if (m != null) {
                return String.valueOf(plugin.getMineManager().get(mine).getRemainingBlocksPer());
            } else {
                return "No mine: " + mine;
            }
        }

        if (identifier.startsWith("perminedblocks")) {
            final String[] split = identifier.split("_");
            final String mine = split[this.mineIndex];
            final RMine m = this.plugin.getMineManager().get(mine);
            if (m != null) {
                return String.valueOf(plugin.getMineManager().get(mine).getMinedBlocksPer());
            } else {
                return "No mine: " + mine;
            }
        }

        if (identifier.startsWith("secondsleft")) {
            final String[] split = identifier.split("_");
            final String mine = split[this.mineIndex];
            final RMine m = this.plugin.getMineManager().get(mine);
            if (m != null) {
                return String.valueOf(plugin.getMineManager().get(mine).getMineTimer().getCountdown().getSecondsLeft());
            } else {
                return "No mine: " + mine;
            }
        }

        if (identifier.startsWith("timeleft")) {
            final String[] split = identifier.split("_");
            final String mine = split[this.mineIndex];
            final RMine m = this.plugin.getMineManager().get(mine);
            if (m != null) {
                return Countdown.format(this.plugin.getMineManager().get(mine).getMineTimer().getCountdown().getSecondsLeft() * 1000L);
            } else {
                return "No mine: " + mine;
            }
        }

        if (identifier.startsWith("bar")) {
            final String[] split = identifier.split("_");
            final String mine = split[this.mineIndex];
            final RMine m = this.plugin.getMineManager().get(mine);
            if (m != null) {
                return GUIManager.getBar(m);
            } else {
                return "No mine: " + mine;
            }
        }

        // Wev return null if an inalid placeholder (f.e. %example_placeholder3%)
        // was provided
        return null;
    }
}