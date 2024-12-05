package reforged.mods.harvester;

import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import java.io.File;
import java.util.Arrays;

public class HarvesterConfig {

    public static Configuration CONFIG;

    public static boolean DEBUG;

    // features - general
    public static boolean TREE_CAPITATOR;
    public static boolean LEAF_DECAY;
    public static boolean GROWTH;
    // tree capitator
    public static int CAPITATOR_MAX_COUNT;
    public static boolean IGNORE_DURABILITY;
    public static String[] LEAVES;
    public static String[] LOGS;
    // leaf decay
    public static int MIN_DECAY_TIME;
    public static int MAX_DECAY_TIME;
    // growth
    public static int RADIUS;
    public static boolean SAPLINGS;
    public static boolean CROPS;
    public static boolean IC2_CROPS;
    public static double SPRINT_GROW_CHANCE;
    public static double SNEAK_GROW_CHANCE;
    public static int SNEAK_BEFORE_GROW;

    public static void init() {
        CONFIG = new Configuration(new File((File) FMLInjectionData.data()[6], "config/harvester.cfg"));
        CONFIG.load();

        DEBUG = getBoolean("debug", "debug", false, "Enable debug mode. Helps identify block name, class and metadata. Useful for crops and leaves.");
        // features
        TREE_CAPITATOR = getBoolean("common", "treeCapitator", true, "Enable TreeCapitator feature?");
        LEAF_DECAY = getBoolean("common", "leafDecay", true, "Enable Leaf Decay feature?");
        GROWTH = getBoolean("common", "growth", true, "Enable Growth feature? (Applies growth effect from sneak or sprinting)");
        // capitator
        CAPITATOR_MAX_COUNT = getInt("TreeCapitator", "capitatorMaxCount", 0, Integer.MAX_VALUE, 256, "TreeCapitator Max Harvest Count");
        IGNORE_DURABILITY = getBoolean("TreeCapitator", "ignoreDurability", true, "Ignore tool's durability when chopping down a tree, meaning it will continue harvesting it even if the durability is low." +
                "\nIf true, the tree harvester will ignore tools' damage and will cut down the whole tree. E.g. Huge Jungle Tree can be harvested using Wooden Axe." +
                "\nIf false, this will prevent harvesting once the durability is gone, meaning if the tree is big enough, the upper part  might be left unharvested.");
        LEAVES = getString("TreeCapitator", "leaves", new String[]{}, "Support for custom leaves block. Enable debug and right click with a stick to get more info in the log.");
        LOGS = getString("TreeCapitator", "logs", new String[]{"thaumcraft.common.world.BlockMagicalLog"}, "Support for custom logs. Enable debug and right click with a stick to get more info in the log.");
        // leaf decay
        MIN_DECAY_TIME = getInt("LeafDecay", "minimumDecayTime", 0, Integer.MAX_VALUE, 4, "Minimum time in ticks for leaf decay. Must be lower than MaximumDecayTime!");
        MAX_DECAY_TIME = getInt("LeafDecay", "maximumDecayTime", 0, Integer.MAX_VALUE, 11, "Maximum time in ticks for leaf decay. Must be higher than MinimumDecayTime!");
        if (MIN_DECAY_TIME >= MAX_DECAY_TIME) {
            HarvesterMod.LOGGER.warning("minimumDecayTime needs to be lower than MaximumDecayTime, resetting to default values!");
            MIN_DECAY_TIME = getInt("LeafDecay", "minimumDecayTime", 0, Integer.MAX_VALUE, 4, "Minimum time in ticks for leaf decay. Must be lower than MaximumDecayTime!");
            MAX_DECAY_TIME = getInt("LeafDecay", "maximumDecayTime", 0, Integer.MAX_VALUE, 11, "Maximum time in ticks for leaf decay. Must be higher than MinimumDecayTime!");
        }
        // growth
        RADIUS = getInt("Growth", "radius", 0, Integer.MAX_VALUE, 5, "The radius of effect in blocks of applying the growth effect. Not recommended to change due to performance");
        SAPLINGS = getBoolean("Growth", "saplings", true, "When true saplings are allowed to grow with twerking");
        CROPS = getBoolean("Growth", "crops", true, "When true crops are allowed to grow with twerking");
        IC2_CROPS = getBoolean("Growth", "ic2Crops", false, "When true IC2 crops are allowed to grow with twerking");
        SPRINT_GROW_CHANCE = getDouble("Growth", "sprintGrowChance", 0, 1, 0.15, "The chance of growth effect being applied from sprinting");
        SNEAK_GROW_CHANCE = getDouble("Growth", "sneakGrowChance", 0, 1, 0.5, "The chance of growth effect being applied from any source");
        SNEAK_BEFORE_GROW = getInt("Growth", "sneakBeforeGrowth", 0, Integer.MAX_VALUE, 5, "The minimum number of crouches before the bonemeal is applied (bonemeal is applied randomly so this will not be exact)");

        if (CONFIG != null) {
            CONFIG.save();
        }
    }

    private static String[] getString(String cat, String tag, String[] defaultValue, String comment) {
        comment = comment.replace("{t}", tag) + "\n";
        Property prop = CONFIG.get(cat, tag, defaultValue);
        prop.comment = comment + "Default: " + Arrays.toString(defaultValue);
        return prop.getStringList();
    }

    private static int getInt(String cat, String tag, int min, int max, int defaultValue, String comment) {
        comment = comment.replace("{t}", tag) + "\n";
        Property prop = CONFIG.get(cat, tag, defaultValue);
        prop.comment = comment + "Min: " + min + ", Max: " + max + ", Default: " + defaultValue;
        int value = prop.getInt(defaultValue);
        value = Math.max(value, min);
        value = Math.min(value, max);
        prop.set(Integer.toString(value));
        return value;
    }

    private static double getDouble(String cat, String tag, double min, double max, double defaultValue, String comment) {
        comment = comment.replace("{t}", tag) + "\n";
        Property prop = CONFIG.get(cat, tag, defaultValue);
        prop.comment = comment + "Min: " + min + ", Max: " + max + ", Default: " + defaultValue;
        double value = prop.getDouble(defaultValue);
        value = Math.max(value, min);
        value = Math.min(value, max);
        prop.set(Double.toString(value));
        return value;
    }

    private static boolean getBoolean(String cat, String tag, boolean defaultValue, String comment) {
        comment = comment.replace("{t}", tag) + "\n";
        Property prop = CONFIG.get(cat, tag, defaultValue);
        prop.comment = comment + "Default: " + defaultValue;
        return prop.getBoolean(defaultValue);
    }
}
