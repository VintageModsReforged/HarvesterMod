package reforged.mods.harvester.asm;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import reforged.mods.harvester.HarvesterConfig;

import java.util.Random;

public class LeafDecayHandler {

    static Random rng = new Random();
    static int randomizationTime = HarvesterConfig.MIN_DECAY_TIME;
    static int baseDecayTime = HarvesterConfig.MAX_DECAY_TIME - HarvesterConfig.MIN_DECAY_TIME;

    public static void handleLeafDecay(World world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        Block block = Block.blocksList[id];
        if (block != null) {
            // TODO: make sure to check this next patch
            if (!block.getLocalizedName().toLowerCase().contains("ore berries")) {
                world.scheduleBlockUpdate(x, y, z, id, baseDecayTime + rng.nextInt(randomizationTime));
            }
        }
    }
}
