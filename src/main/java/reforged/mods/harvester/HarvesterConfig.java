package reforged.mods.harvester;

import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import java.io.File;

public class HarvesterConfig {

    public static Configuration CONFIG;

    public static int MIN_DECAY_TIME;
    public static int MAX_DECAY_TIME;
    public static int CAPITATOR_MAX_COUNT;
    public static boolean TREE_CAPITATOR;
    public static boolean IGNORE_DURABILITY;

    public static void init() {
        CONFIG = new Configuration(new File((File) FMLInjectionData.data()[6], "config/harvester.cfg"));
        CONFIG.load();

        MIN_DECAY_TIME = getInt("main - decay", "MinimumDecayTime", 0, Integer.MAX_VALUE, 4, "Minimum time in ticks for leaf decay. Must be lower than MaximumDecayTime!");
        MAX_DECAY_TIME = getInt("main - decay", "MaximumDecayTime", 0, Integer.MAX_VALUE, 11, "Maximum time in ticks for leaf decay. Must be higher than MinimumDecayTime!");
        TREE_CAPITATOR = getBoolean("main - capitator", "TreeCapitator", true, "Enable TreeCapitator feature?");
        CAPITATOR_MAX_COUNT = getInt("main - capitator", "CapitatorMaxCount", 0, Integer.MAX_VALUE, 256, "TreeCapitator Max Harvest Count");
        IGNORE_DURABILITY = getBoolean("main - capitator", "IgnoreDurability", true, "Ignore tool's durability when chopping down a tree, meaning it will continue harvesting it even if the durability is low." +
                "\nIF true, THIS WILL GLITCH THE TOOL AND MAKE IT SEEM LIKE IT HAS RESTORED DURABILITY, BUT THAT IS ONLY VISUAL AND NEXT HARVEST WILL BREAK IT!" +
                "\nIf false, this will prevent harvesting once the durability is gone, meaning if the tree is big enough, the upper part  might be left unharvested. This is more unpleasant than having a glitched tool :D");
        if (MIN_DECAY_TIME >= MAX_DECAY_TIME) {
            HarvesterMod.LOGGER.warning("MinimumDecayTime needs to be lower than MaximumDecayTime, resetting to default values!");
            MIN_DECAY_TIME = getInt("main", "MinimumDecayTime", 0, Integer.MAX_VALUE, 4, "Minimum time in ticks for leaf decay. Must be lower than MaximumDecayTime!");
            MAX_DECAY_TIME = getInt("main", "MaximumDecayTime", 0, Integer.MAX_VALUE, 11, "Maximum time in ticks for leaf decay. Must be higher than MinimumDecayTime!");
        }

        if (CONFIG != null) {
            CONFIG.save();
        }
    }

    private static int getInt(String cat, String tag, int min, int max, int defaultValue, String comment) {
        comment = comment.replace("{t}", tag) + "\n";
        Property prop = CONFIG.get(cat, tag, defaultValue);
        prop.comment = comment + "Min: " + min + ", Max: " + max + ", Default: " + defaultValue;
        int value = prop.getInt(defaultValue);
        value = Math.max(value, min);
        value = Math.min(value, max);
        prop.value = Integer.toString(value);
        return value;
    }

    private static boolean getBoolean(String cat, String tag, boolean defaultValue, String comment) {
        comment = comment.replace("{t}", tag) + "\n";
        Property prop = CONFIG.get(cat, tag, defaultValue);
        prop.comment = comment + "Default: " + defaultValue;
        return prop.getBoolean(defaultValue);
    }
}
