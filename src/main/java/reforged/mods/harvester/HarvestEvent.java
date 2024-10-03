package reforged.mods.harvester;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockPotato;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
        Random random = new Random();
        World world = player.worldObj;
        Block clickedBlock = Block.blocksList[world.getBlockId(x, y, z)];
        boolean harvest = false;
        if (Utils.isRendering()) {
            return false;
        }

        List<ItemStack> drops = new ArrayList<ItemStack>();
        Map<Integer, Integer> sortedDropsStacks = new HashMap<Integer, Integer>();
        // performing double check because Natura's cotton uses stages from 4 to 8
        if (clickedBlock instanceof BlockCrops && !Utils.isInstanceOf(clickedBlock, "mods.natura.blocks.crops.CropBlock")) { // only vanilla
            BlockCrops crop = (BlockCrops) clickedBlock;
            int stage = world.getBlockMetadata(x, y, z);
            if (stage == 7) {
                drops = crop.getBlockDropped(world, x, y, z, stage, 0);
                if (crop instanceof BlockPotato) {
                    if (random.nextInt(50) == 0) {
                        drops.add(new ItemStack(Item.poisonousPotato));
                    }
                }
                world.setBlockMetadataWithNotify(x, y, z, 1, 2);
                harvest = true;
            }
        }
        if (Loader.isModLoaded("Natura") && clickedBlock instanceof mods.natura.blocks.crops.CropBlock) { // Natura
            mods.natura.blocks.crops.CropBlock crop = (mods.natura.blocks.crops.CropBlock) clickedBlock;
            int stage = world.getBlockMetadata(x, y, z);
            if (stage == 3) {
                drops = crop.getBlockDropped(world, x, y, z, stage, 0);
                world.setBlockMetadataWithNotify(x, y, z, 0, 2);
                harvest = true;
            }
        }
        if (clickedBlock instanceof BlockCocoa) {
            BlockCocoa cocoa = (BlockCocoa) clickedBlock;
            int stage = world.getBlockMetadata(x, y, z);
            int direction = BlockCocoa.getDirection(stage);
            int maxStage = 0;
            int minStage = 0;
            switch (direction) {
                case 1:
                    maxStage = 9;
                    minStage = 1;
                    break;
                case 2:
                    maxStage = 10;
                    minStage = 2;
                    break;
                case 3:
                    maxStage = 11;
                    minStage = 3;
                    break;
                case 0:
                    maxStage = 8;
                    minStage = 0;
                    break;
            }

            if (stage == maxStage) {
                drops = cocoa.getBlockDropped(world, x, y, z, stage, 0);
                int newDirection = BlockCocoa.getDirection(minStage);
                int extra = BlockCocoa.func_72219_c(minStage);
                world.setBlockMetadataWithNotify(x, y, z, extra << 2 | newDirection, 2);
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
            int meta = 0;
            for (Integer id : sortedDropsStacks.keySet()) {
                if (id == Item.dyePowder.itemID) { // cocoa
                    meta = 3;
                }
                newDrops.add(new ItemStack(Item.itemsList[id], sortedDropsStacks.get(id), meta));
            }

            ItemStack plantable = Utils.getPickBlock(world, x, y, z);
            for (ItemStack drop : newDrops) {
                if (drop.isItemEqual(plantable) || ((drop.itemID == Item.potato.itemID || drop.itemID == Item.carrot.itemID) && drop.stackSize > 1)) {
                    drop.stackSize--;
                }
                if (drop.stackSize == 0) {
                    continue;
                }
                EntityItem dropItem = Utils.dropItem(drop, world, x, y, z, 0.5F);
                dropItem.motionY += random.nextFloat() * 0.05F;
                dropItem.motionX += (random.nextFloat() - random.nextFloat()) * 0.1F;
                dropItem.motionZ += (random.nextFloat() - random.nextFloat()) * 0.1F;
            }
        }
        return harvest;
    }
}
