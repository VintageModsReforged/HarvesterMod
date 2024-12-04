package reforged.mods.harvester.events;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.TickType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockSapling;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import reforged.mods.harvester.HarvesterConfig;
import reforged.mods.harvester.Utils;
import reforged.mods.harvester.pos.BlockPos;

import java.util.*;

public class GrowthEvent implements ITickHandler {

    private final Map<String, Integer> sneakCount = new HashMap<String, Integer>();
    private final Map<String, Boolean> prevSneaking = new HashMap<String, Boolean>();

    @Override
    public void tickStart(EnumSet<TickType> enumSet, Object... objects) {
        EntityPlayer player = (EntityPlayer) objects[0];
        if (!HarvesterConfig.GROWTH) {
            return;
        }
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

        String username = player.username;
        if (!sneakCount.containsKey(username)) {
            sneakCount.put(username, 0);
            prevSneaking.put(username, player.isSneaking());
        }

        WorldServer world = (WorldServer) player.worldObj;
        if (player.isSprinting() && world.rand.nextDouble() <= HarvesterConfig.SPRINT_GROW_CHANCE) {
            triggerGrowth(player);
        }

        boolean wasSneaking = prevSneaking.get(username);
        int playerSneakCount = sneakCount.get(username);
        if (!player.isSneaking()) {
            prevSneaking.put(username, false);
            return;
        }

        if (wasSneaking && player.isSneaking()) {
            return;
        } else if (!wasSneaking && player.isSneaking()) {
            prevSneaking.put(username, true);
            sneakCount.put(username, ++playerSneakCount);
        }

        if (playerSneakCount >= HarvesterConfig.SNEAK_BEFORE_GROW && world.rand.nextDouble() <= HarvesterConfig.SNEAK_GROW_CHANCE) {
            triggerGrowth(player);
        }
    }

    public void triggerGrowth(EntityPlayer player) {
        BlockPos origin = new BlockPos(player.posX, player.posY, player.posZ);
        World world = player.worldObj;
        List<BlockPos> area = getNearestBlocks(world, origin);
        for (BlockPos pos : area) {
            Block block = Utils.getBlock(world, pos);
            if (block instanceof BlockCrops) {
                ItemDye.applyBonemeal(new ItemStack(Item.dyePowder, 1, 3), world, pos.getX(), pos.getY(), pos.getZ(), player);
            }
            if (block instanceof BlockSapling) {
                BlockSapling sapling = (BlockSapling) block;
                sapling.markOrGrowMarked(world, pos.getX(), pos.getY(), pos.getZ(), world.rand);
            }
            if (Loader.isModLoaded("IC2")) IC2CropHandler.handleIC2Crops(world, pos);
        }
    }

    private List<BlockPos> getNearestBlocks(World world, BlockPos origin) {
        List<BlockPos> list = new ArrayList<BlockPos>();
        int radius = HarvesterConfig.RADIUS;
        for (BlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-radius, -2, -radius), origin.add(radius, 2, radius))) {
            Block block = Utils.getBlock(world, pos);
            if (block instanceof BlockSapling && HarvesterConfig.SAPLINGS) {
                list.add(pos.toImmutable());
            }
            if (block instanceof BlockCrops && HarvesterConfig.CROPS) {
                list.add(pos.toImmutable());
            }
            if (Loader.isModLoaded("IC2")) {
                if (IC2CropHandler.isIC2Crop(block) && HarvesterConfig.IC2_CROPS) {
                    list.add(pos.toImmutable());
                }
            }
        }
        return list;
    }

    @Override
    public void tickEnd(EnumSet<TickType> enumSet, Object... objects) {}

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.PLAYER);
    }

    @Override
    public String getLabel() {
        return "harvester";
    }
}
