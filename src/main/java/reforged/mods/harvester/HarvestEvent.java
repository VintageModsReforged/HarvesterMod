package reforged.mods.harvester;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.*;

public class HarvestEvent {

    @ForgeSubscribe
    public void onRightClick(PlayerInteractEvent e) {
        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            EntityPlayer player = e.entityPlayer;
            if (harvest(player, e.x, e.y, e.z)) {
                e.setResult(Event.Result.ALLOW);
                e.setCanceled(true);
            }
        }
    }

    public boolean harvest(EntityPlayer player, int x, int y, int z) {
        World world = player.worldObj;
        Block clickedBlock = Block.blocksList[world.getBlockId(x, y, z)];
        boolean harvest = false;
        if (world.isRemote) {
            return false;
        }

        List<ItemStack> drops = new ArrayList<ItemStack>();
        Map<Integer, Integer> sortedDropsStacks = new HashMap<Integer, Integer>();
        if (clickedBlock instanceof BlockCrops) {
            BlockCrops crop = (BlockCrops) clickedBlock;
            int stage = world.getBlockMetadata(x, y, z);
            if (stage == 7) {
                drops = crop.getBlockDropped(world, x, y, z, stage, 0);
                world.setBlockMetadataWithNotify(x, y, z, 1);
                harvest = true;
            }
        }
        if (clickedBlock instanceof BlockCocoa) {
            BlockCocoa cocoa = (BlockCocoa) clickedBlock;
            int stage = world.getBlockMetadata(x, y, z);
            if (stage == 8) {
                drops = cocoa.getBlockDropped(world, x, y, z, stage, 0);
                world.setBlockMetadataWithNotify(x, y, z, 0);
                harvest = true;
            }
        }

        if (!drops.isEmpty()) {
            for (ItemStack drop : drops) {
                if (sortedDropsStacks.containsKey(drop.itemID)) {
                    sortedDropsStacks.put(drop.itemID, sortedDropsStacks.get(drop.itemID) + 1);
                } else {
                    sortedDropsStacks.put(drop.itemID, drop.stackSize);
                }
            }
            List<ItemStack> newDrops = new ArrayList<ItemStack>();
            for (Integer id : sortedDropsStacks.keySet()) {
                newDrops.add(new ItemStack(Item.itemsList[id], sortedDropsStacks.get(id)));
            }
            Random random = new Random();
            MovingObjectPosition mop = raytraceFromEntity(world, player, false, 4.5D);
            ItemStack plantable = clickedBlock.getPickBlock(mop, world, x, y, z);
            for (ItemStack drop : newDrops) {
                if (drop.isItemEqual(plantable)) {
                    drop.stackSize--;
                }
                if (drop.stackSize == 0) {
                    continue;
                }

                EntityItem dropItem = entityDropItem(drop, world, x, y, z, 0.5F);
                dropItem.motionY += random.nextFloat() * 0.05F;
                dropItem.motionX += (random.nextFloat() - random.nextFloat()) * 0.1F;
                dropItem.motionZ += (random.nextFloat() - random.nextFloat()) * 0.1F;
            }
        }
        return harvest;
    }

    public EntityItem entityDropItem(ItemStack drop, World world, int x, int y, int z, float offset) {
        EntityItem entityitem = new EntityItem(world, x, y + (double) offset, z, drop);
        entityitem.delayBeforeCanPickup = 10;
        world.spawnEntityInWorld(entityitem);
        return entityitem;
    }

    public static MovingObjectPosition raytraceFromEntity(World world, Entity player, boolean checkFluid, double range) {
        float f = 1.0F;
        float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
        float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
        double d0 = player.prevPosX + (player.posX - player.prevPosX) * f;
        double d1 = player.prevPosY + (player.posY - player.prevPosY) * f;
        if ((!world.isRemote) && ((player instanceof EntityPlayer))) {
            d1 += 1.62D;
        }
        double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * f;
        Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = range;
        if ((player instanceof EntityPlayerMP)) {
            d3 = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
        }
        Vec3 vec31 = vec3.addVector(f7 * d3, f6 * d3, f8 * d3);
        return world.rayTraceBlocks_do_do(vec3, vec31, checkFluid, !checkFluid);
    }
}
