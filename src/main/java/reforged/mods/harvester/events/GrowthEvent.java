package reforged.mods.harvester.events;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.TickType;
import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.entity.player.BonemealEvent;
import reforged.mods.harvester.HarvesterConfig;
import reforged.mods.harvester.Utils;
import reforged.mods.harvester.pos.BlockPos;

import java.util.*;

public class GrowthEvent implements ITickHandler {

    private final Map<String, Integer> sneakCount = new HashMap<String, Integer>();
    private final Map<String, Boolean> prevSneaking = new HashMap<String, Boolean>();

    @Override
    public void tickStart(EnumSet<TickType> enumSet, Object... objects) {
        if (!HarvesterConfig.GROWTH) return;
        EntityPlayer player = (EntityPlayer) objects[0];
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
            applyBonemeal(new ItemStack(Item.dyePowder, 1, 3), world, pos.getX(), pos.getY(), pos.getZ(), player);
            if (Loader.isModLoaded("IC2")) IC2CropsEvent.handleIC2Crops(world, pos);
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
                if (IC2CropsEvent.isIC2Crop(block) && HarvesterConfig.IC2_CROPS) {
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

    public static boolean applyBonemeal(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        int l = world.getBlockId(x, y, z);
        BonemealEvent event = new BonemealEvent(player, world, l, x, y, z);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return false;
        } else if (event.getResult() == Event.Result.ALLOW) {
            if (!world.isRemote) {
                --stack.stackSize;
            }

            return true;
        } else if (l == Block.sapling.blockID) {
            if (!world.isRemote) {
                if ((double) world.rand.nextFloat() < 0.45) {
                    ((BlockSapling) Block.sapling).growTree(world, x, y, z, world.rand);
                }

                --stack.stackSize;
            }

            return true;
        } else if (l != Block.mushroomBrown.blockID && l != Block.mushroomRed.blockID) {
            if (l != Block.melonStem.blockID && l != Block.pumpkinStem.blockID) {
                if (l > 0 && Block.blocksList[l] instanceof BlockCrops) {
                    if (world.getBlockMetadata(x, y, z) == 7) {
                        return false;
                    } else {
                        if (!world.isRemote) {
                            ((BlockCrops) Block.blocksList[l]).fertilize(world, x, y, z);
                            --stack.stackSize;
                        }

                        return true;
                    }
                } else if (l == Block.cocoaPlant.blockID) {
                    int i1 = world.getBlockMetadata(x, y, z);
                    int j1 = BlockDirectional.getDirection(i1);
                    int k1 = BlockCocoa.func_72219_c(i1);
                    if (k1 >= 2) {
                        return false;
                    } else {
                        if (!world.isRemote) {
                            ++k1;
                            world.setBlockMetadataWithNotify(x, y, z, k1 << 2 | j1);
                            --stack.stackSize;
                        }

                        return true;
                    }
                } else if (l != Block.grass.blockID) {
                    return false;
                } else {
                    if (!world.isRemote) {
                        --stack.stackSize;

                        label104:
                        for (int i1 = 0; i1 < 128; ++i1) {
                            int j1 = x;
                            int k1 = y + 1;
                            int l1 = z;

                            for (int i2 = 0; i2 < i1 / 16; ++i2) {
                                j1 += world.rand.nextInt(3) - 1;
                                k1 += (world.rand.nextInt(3) - 1) * world.rand.nextInt(3) / 2;
                                l1 += world.rand.nextInt(3) - 1;
                                if (world.getBlockId(j1, k1 - 1, l1) != Block.grass.blockID || world.isBlockNormalCube(j1, k1, l1)) {
                                    continue label104;
                                }
                            }

                            if (world.getBlockId(j1, k1, l1) == 0) {
                                if (world.rand.nextInt(10) != 0) {
                                    if (Block.tallGrass.canBlockStay(world, j1, k1, l1)) {
                                        world.setBlock(j1, k1, l1, Block.tallGrass.blockID);
                                    }
                                } else {
                                    ForgeHooks.plantGrass(world, j1, k1, l1);
                                }
                            }
                        }
                    }

                    return true;
                }
            } else if (world.getBlockMetadata(x, y, z) == 7) {
                return false;
            } else {
                if (!world.isRemote) {
                    ((BlockStem) Block.blocksList[l]).fertilizeStem(world, x, y, z);
                    --stack.stackSize;
                }

                return true;
            }
        } else {
            if (!world.isRemote) {
                if ((double) world.rand.nextFloat() < 0.4) {
                    ((BlockMushroom) Block.blocksList[l]).fertilizeMushroom(world, x, y, z, world.rand);
                }

                --stack.stackSize;
            }

            return true;
        }
    }
}
