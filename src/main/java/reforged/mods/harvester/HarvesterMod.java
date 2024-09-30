package reforged.mods.harvester;

import cpw.mods.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = "harvester", name = "Harvester Mod", version = "1.5.2-1.0.1")
public class HarvesterMod {

    public HarvesterMod() {
        MinecraftForge.EVENT_BUS.register(new HarvestEvent());
    }
}
