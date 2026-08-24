package vazkii.psi.common.core.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import vazkii.psi.common.Psi;
import vazkii.psi.common.core.handler.ConfigHandler;
import vazkii.psi.common.lib.LibMisc;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        Psi.logger.info("Psi is starting up at version " + LibMisc.VERSION);
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {
        // Register test command
        event.registerServerCommand(new vazkii.psi.common.command.CommandPsiTest());
        Psi.logger.info("Registered /psitest command");
    }
}
