package reforged.mods.harvester.asm;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.world.World;
import reforged.mods.harvester.HarvesterConfig;
import reforged.mods.harvester.pos.BlockPos;

import java.util.Random;

public class LeafDecayHandler {

    static Random rng = new Random();
    static int randomizationTime = HarvesterConfig.MIN_DECAY_TIME;
    static int baseDecayTime = HarvesterConfig.MAX_DECAY_TIME - HarvesterConfig.MIN_DECAY_TIME;

    public static void handleLeafDecay(World world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        Block block = Block.blocksList[id];
        if (block != null) {
            BlockPos origin = new BlockPos(x, y, z);
            for (BlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-1, -1, -1), origin.add(1, 1, 1))) {
                if ((block.isLeaves(world, pos.getX(), pos.getY(), pos.getZ()) || block instanceof BlockLeavesBase) && !block.translateBlockName().toLowerCase().contains("ore berries")) { // handle regular blocks marked as leaves
                    world.scheduleBlockUpdate(pos.getX(), pos.getY(), pos.getZ(), id, baseDecayTime + rng.nextInt(randomizationTime));
                }
            }
        }
    }
}
