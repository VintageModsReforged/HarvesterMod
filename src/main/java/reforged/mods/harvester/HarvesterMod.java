package reforged.mods.harvester;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.logging.Logger;

@Mod(modid = "harvester", name = "Harvester Mod", version = "1.4.7-1.0.2.1")
public class HarvesterMod {

    public static final Logger LOGGER = Logger.getLogger("Harvester Mod");

    public HarvesterMod() {
        MinecraftForge.EVENT_BUS.register(new HarvestEvent());
        LOGGER.setParent(FMLLog.getLogger());
    }

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent e) {
        HarvessterConfig.init();
    }
}
