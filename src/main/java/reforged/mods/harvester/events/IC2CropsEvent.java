package reforged.mods.harvester.events;

import ic2.core.block.BlockCrop;
import ic2.core.block.TileEntityCrop;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import reforged.mods.harvester.pos.BlockPos;

public class IC2CropsEvent {

    public static boolean isIC2Crop(Block inspectedBlock) {
        return inspectedBlock instanceof BlockCrop;
    }

    public static void handleIC2Crops(World world, BlockPos pos) {
        TileEntity blockEntity = world.getBlockTileEntity(pos.getX(), pos.getY(), pos.getZ());
        if (blockEntity instanceof TileEntityCrop) {
            TileEntityCrop crop = (TileEntityCrop) blockEntity;
            crop.tick();
        }
    }
}
