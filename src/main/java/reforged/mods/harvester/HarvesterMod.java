package reforged.mods.harvester;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import reforged.mods.harvester.events.CropsEvent;
import reforged.mods.harvester.events.GrowthEvent;

import java.util.logging.Logger;

@Mod(modid = "harvester", name = "Harvester Mod", useMetadata = true)
public class HarvesterMod {

    public static final Logger LOGGER = Logger.getLogger("Harvester Mod");

    public HarvesterMod() {
        MinecraftForge.EVENT_BUS.register(new CropsEvent());
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.setParent(FMLLog.getLogger());
    }

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent e) {
        HarvesterConfig.init();
        TickRegistry.registerTickHandler(new GrowthEvent(), Side.SERVER);
    }

    @ForgeSubscribe
    public void onRightClick(PlayerInteractEvent e) {
        if (HarvesterConfig.DEBUG && e.entityPlayer.getHeldItem() != null) {
            if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && e.entityPlayer.getHeldItem().getItem() == Item.stick) {
                Block block = Utils.getBlock(e.entity.worldObj, e.x, e.y, e.z);
                int metadata = e.entityPlayer.worldObj.getBlockMetadata(e.x, e.y, e.z);
                if (block != null) {
                    if (Utils.isRendering()) {
                        LOGGER.info("Block: " + block.translateBlockName() + " | Class Name: " + block.getClass().getName());
                        LOGGER.info("Block Metadata: " + metadata);
                    }
                }
            }
        }
    }
}
