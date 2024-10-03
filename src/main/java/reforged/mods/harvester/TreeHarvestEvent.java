package reforged.mods.harvester;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;
import reforged.mods.harvester.pos.BlockPos;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TreeHarvestEvent {

    public static final TreeHarvestEvent instance = new TreeHarvestEvent();

    public void onBlockHarvested(World world, int x, int y, int z, Block block, int metadata, EntityPlayer player) {
        ItemStack heldStack = player.getHeldItem();
        if (canHarvest(player, heldStack, block, metadata)) {
            BlockPos origin = new BlockPos(x, y, z);
            LinkedList<BlockPos> connectedLogs = scanForTree(world, origin, player.isSneaking() ? 0 : 256);
            for (BlockPos log : connectedLogs) {
                int id = world.getBlockId(log.getX(), log.getY(), log.getZ());
                if (Utils.harvestBlock(world, log.getX(), log.getY(), log.getZ(), player)) {
                    /**
                     *  Skip damaging first block, minecraft
                     *  {@link net.minecraft.item.ItemInWorldManager#removeBlock(int, int, int)} already does that
                     */
                    if (connectedLogs.indexOf(log) != 0) {
                        heldStack.getItem().onBlockDestroyed(heldStack, world, id, log.getX(), log.getY(), log.getZ(), player);
                    }
                }
            }
        }
    }

    public boolean canHarvest(EntityPlayer player, ItemStack stack, Block block, int meta) {
        if (!HarvesterConfig.TREE_CAPITATOR || Utils.isRendering()) {
            return false;
        }
        if (player.isSneaking()) {
            return false;
        }
        return ForgeHooks.canToolHarvestBlock(block, meta, stack) || stack.getItem() instanceof ItemAxe;
    }

    private interface BlockAction {
        boolean onBlock(BlockPos pos, Block block, boolean isRightBlock);
    }

    public LinkedList<BlockPos> scanForTree(final World world, final BlockPos startPos, int limit) {
        Block block = Block.blocksList[world.getBlockId(startPos.getX(), startPos.getY(), startPos.getZ())];
        ItemStack blockStack = new ItemStack(block, 1, 32767);
        boolean isLog = false;
        List<ItemStack> logs = Utils.getStackFromOre("log");
        logs.addAll(Utils.getStackFromOre("wood")); // just in case some mod uses old oredict name
        for (ItemStack check : logs) {
            if (Utils.areStacksEqual(check, blockStack)) {
                isLog = true;
                break;
            }
        }
        if (!isLog) {
            return new LinkedList<BlockPos>();
        }
        final boolean[] leavesFound = new boolean[1];
        LinkedList<BlockPos> result = recursiveSearch(world, startPos, new BlockAction() {
            @Override
            public boolean onBlock(BlockPos pos, Block block, boolean isRightBlock) {
                int metadata = Utils.getBlockMetadata(world, pos) | 8;
                boolean isLeave = metadata >= 8 && metadata <= 11;
                if (block.isLeaves(world, startPos.getX(), startPos.getY(), startPos.getZ()) && isLeave || getBOPStatus(world, pos)) leavesFound[0] = true;
                return true;
            }
        }, limit);
        return leavesFound[0] ? result : new LinkedList<BlockPos>();
    }

    private boolean getBOPStatus(World world, BlockPos pos) {
        int meta = Utils.getBlockMetadata(world, pos) | 8;
        Block block = Utils.getBlock(world, pos);
        if (Loader.isModLoaded("BiomesOPlenty")) {
            if (Utils.isInstanceOf(block, "biomesoplenty.blocks.BlockBOPPetals") ||
                    Utils.isInstanceOf(block, "biomesoplenty.blocks.BlockBOPLeaves") ||
                    Utils.isInstanceOf(block, "biomesoplenty.blocks.BlockBOPColorizedLeaves") ||
                    Utils.isInstanceOf(block, "biomesoplenty.blocks.BlockBOPAppleLeaves")) {
                return meta >= 8 && meta <= 15;
            }
        }
        return false;
    }

    // Recursively scan 3x3x3 cubes while keeping track of already scanned blocks to avoid cycles.
    private static LinkedList<BlockPos> recursiveSearch(final World world, final BlockPos start, @Nullable final BlockAction action, int limit) {
        Block wantedBlock = Utils.getBlock(world, start);
        boolean abort = false;
        final LinkedList<BlockPos> result = new LinkedList<BlockPos>();
        final Set<BlockPos> visited = new HashSet<BlockPos>();
        final LinkedList<BlockPos> queue = new LinkedList<BlockPos>();
        queue.push(start);

        while (!queue.isEmpty()) {
            final BlockPos center = queue.pop();
            final int x0 = center.getX();
            final int y0 = center.getY();
            final int z0 = center.getZ();
            for (int z = z0 - 1; z <= z0 + 1 && !abort; ++z) {
                for (int y = y0 - 1; y <= y0 + 1 && !abort; ++y) {
                    for (int x = x0 - 1; x <= x0 + 1 && !abort; ++x) {
                        final BlockPos pos = new BlockPos(x, y, z);
                        Block checkBlock = Utils.getBlock(world, pos);
                        if ((Utils.isAir(world, pos) || !visited.add(pos))) {
                            continue;
                        }
                        final boolean isRightBlock = checkBlock.blockID == wantedBlock.blockID;
                        if (isRightBlock) {
                            result.add(pos);
                            if (queue.size() > limit) {
                                abort = true;
                                break;
                            }
                            queue.push(pos);
                        }
                        if (action != null) {
                            abort = !action.onBlock(pos, checkBlock, isRightBlock);
                        }
                    }
                }
            }
        }
        return !abort ? result : new LinkedList<BlockPos>();
    }
}
