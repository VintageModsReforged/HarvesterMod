package reforged.mods.harvester;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet14BlockDig;
import net.minecraft.network.packet.Packet53BlockChange;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;
import reforged.mods.harvester.pos.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static boolean isAir(World world, BlockPos pos) {
        return world.isAirBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public static int getBlockMetadata(World world, BlockPos pos) {
        return world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());
    }

    public static int getBlockId(World world, BlockPos pos) {
        return world.getBlockId(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Block getBlock(World world, BlockPos pos) {
        return getBlock(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static Block getBlock(World world, int x, int y, int z) {
        return Block.blocksList[world.getBlockId(x, y, z)];
    }

    public static boolean isRendering() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    public static boolean isSimulating() {
        return !isRendering();
    }

    public static ItemStack getPickBlock(World world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        Block block = Block.blocksList[id];
        if (id == 0) {
            return null;
        } else {
            Item item = Item.itemsList[id];
            return item == null ? null : new ItemStack(id, 1, block.getDamageValue(world, x, y, z));
        }
    }


    public static EntityItem dropItem(ItemStack drop, World world, int x, int y, int z, float offset) {
        EntityItem entityitem = new EntityItem(world, x, y + (double) offset, z, drop);
        entityitem.delayBeforeCanPickup = 10;
        world.spawnEntityInWorld(entityitem);
        return entityitem;
    }

    public static boolean areStacksEqual(ItemStack aStack, ItemStack bStack) {
        return aStack != null && bStack != null &&
                aStack.itemID == bStack.itemID &&
                (aStack.getTagCompound() == null == (bStack.getTagCompound() == null)) &&
                (aStack.getTagCompound() == null || aStack.getTagCompound().equals(bStack.getTagCompound()))
                && (aStack.getItemDamage() == bStack.getItemDamage() || aStack.getItemDamage() == 32767 || bStack.getItemDamage() == 32767);
    }

    public static List<ItemStack> getStackFromOre(String startWith) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        for (String name : OreDictionary.getOreNames()) {
            if (name.startsWith(startWith)) {
                List<ItemStack> oreDictList = OreDictionary.getOres(name);
                stacks.addAll(oreDictList);
            }
        }
        return stacks;
    }

    /**
     * For potential compat purposes
     * Checks if the given class is currently loaded and the given object is instance of that class.
     *
     * @param obj   The object to check if it is an instance of a class.
     * @param clazz The fully qualified class name.
     * @return Whether the given class is loaded and the given object is instance of that class.
     */
    public static boolean isInstanceOf(Object obj, String clazz) {
        if (obj == null)
            return false;
        try {
            Class<?> c = Class.forName(clazz);
            if (c.isInstance(obj))
                return true;
        } catch (Throwable ignored) {
        }
        return false;
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

    public static boolean harvestBlock(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isAirBlock(x, y, z)) {
            return false;
        } else {
            EntityPlayerMP playerMP = null;
            if (player instanceof EntityPlayerMP) {
                playerMP = (EntityPlayerMP)player;
            }

            Block block = Block.blocksList[world.getBlockId(x, y, z)];
            int blockMetadata = world.getBlockMetadata(x, y, z);
            if (!ForgeHooks.canHarvestBlock(block, player, 0)) {
                return false;
            } else if (!ForgeHooks.canHarvestBlock(block, player, blockMetadata)) {
                return false;
            } else {
                if (player.capabilities.isCreativeMode) {
                    if (!world.isRemote) {
                        block.onBlockHarvested(world, x, y, z, blockMetadata, player);
                    } else {
                        world.playAuxSFX(2001, x, y, z, world.getBlockId(x, y, z) | blockMetadata << 12);
                    }

                    if (block.removeBlockByPlayer(world, player, x, y, z)) {
                        block.onBlockDestroyedByPlayer(world, x, y, z, blockMetadata);
                    }

                    if (!world.isRemote) {
                        assert playerMP != null;
                        playerMP.playerNetServerHandler.sendPacketToPlayer(new Packet53BlockChange());
                    } else {
                        Minecraft.getMinecraft().getNetHandler().addToSendQueue(new Packet14BlockDig());
                    }

                } else {
                    world.playAuxSFXAtEntity(player, 2001, x, y, z, world.getBlockId(x, y, z) | blockMetadata << 12);
                    if (!world.isRemote) {
                        block.onBlockHarvested(world, x, y, z, blockMetadata, player);
                        if (block.removeBlockByPlayer(world, player, x, y, z)) {
                            block.onBlockDestroyedByPlayer(world, x, y, z, blockMetadata);
                            block.harvestBlock(world, player, x, y, z, blockMetadata);
                        }
                        assert playerMP != null;
                        playerMP.playerNetServerHandler.sendPacketToPlayer(new Packet53BlockChange());
                    } else {
                        if (block.removeBlockByPlayer(world, player, x, y, z)) {
                            block.onBlockDestroyedByPlayer(world, x, y, z, blockMetadata);
                        }
                        Minecraft.getMinecraft().getNetHandler().addToSendQueue(new Packet14BlockDig());
                    }
                }
                return true;
            }
        }
    }
}
