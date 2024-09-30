package reforged.mods.harvester;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HarvestEvent {

    @ForgeSubscribe
    public void onRightClick(PlayerInteractEvent e) {
        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            BlockPos pos = new BlockPos(e.x, e.y, e.z);
            EntityPlayer player = e.entityPlayer;
            if (harvest(player, pos)) {
                e.setResult(Event.Result.ALLOW);
                e.setCanceled(true);
            }
        }
    }

    public boolean harvest(EntityPlayer player, BlockPos pos) {
        World world = player.worldObj;
        Block clickedBlock = Block.blocksList[world.getBlockId(pos.getX(), pos.getY(), pos.getZ())];
        boolean harvest = false;
        if (world.isRemote) {
            return false;
        }

        List<ItemStack> drops = new ArrayList<ItemStack>();
        if (clickedBlock instanceof BlockCrops) {
            BlockCrops crop = (BlockCrops) clickedBlock;
            int stage = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());
            if (stage == 7) {
                drops = crop.getBlockDropped(world, pos.x, pos.y, pos.z, stage, 0);
                world.setBlockMetadataWithNotify(pos.x, pos.y, pos.z, 1, 2);
                harvest = true;
            }
        }
        if (clickedBlock instanceof BlockCocoa) {
            BlockCocoa cocoa = (BlockCocoa) clickedBlock;
            int stage = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());
            if (stage == 8) {
                drops = cocoa.getBlockDropped(world, pos.x, pos.y, pos.z, stage, 0);
                world.setBlockMetadataWithNotify(pos.x, pos.y, pos.z, 0, 2);
                harvest = true;
            }
        }

        if (!drops.isEmpty()) {
            Random random = new Random();
            MovingObjectPosition mop = raytraceFromEntity(world, player, false, 4.5D);
            ItemStack plantable = clickedBlock.getPickBlock(mop, world, pos.x, pos.y, pos.z);
            for (ItemStack drop : drops) {
                if (drop == plantable) {
                    drop.stackSize--;
                }
                if (drop.stackSize == 0) {
                    continue;
                }

                EntityItem dropItem = entityDropItem(drop, world, pos, 0.5F);
                dropItem.motionY += random.nextFloat() * 0.05F;
                dropItem.motionX += (random.nextFloat() - random.nextFloat()) * 0.1F;
                dropItem.motionZ += (random.nextFloat() - random.nextFloat()) * 0.1F;
            }
        }
        return harvest;
    }

    public EntityItem entityDropItem(ItemStack drop, World world, BlockPos pos, float offset) {
        EntityItem entityitem = new EntityItem(world, pos.x, pos.y + (double)offset, pos.z, drop);
        entityitem.delayBeforeCanPickup = 10;
        world.spawnEntityInWorld(entityitem);
        return entityitem;
    }

    public static class BlockPos {

        int x, y, z;

        public BlockPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }
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
