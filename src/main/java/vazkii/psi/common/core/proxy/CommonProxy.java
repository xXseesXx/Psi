package vazkii.psi.common.core.proxy;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import vazkii.psi.common.Psi;
import vazkii.psi.common.core.handler.ConfigHandler;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.lib.LibMisc;

public class CommonProxy {

    public static Item itemCAD;

    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        Psi.logger.info("Psi is starting up at version " + LibMisc.VERSION);

        // Initialize spell piece registry
        vazkii.psi.common.spell.SpellPieceRegistry.init();
        Psi.logger.info("Initialized SpellPieceRegistry with 9 pieces");

        // Register items
        itemCAD = new ItemCAD().setUnlocalizedName("psi:cad")
            .setTextureName("psi:cad")
            .setCreativeTab(CreativeTabs.tabTools);
        GameRegistry.registerItem(itemCAD, "cad");
        Psi.logger.info("Registered ItemCAD");
    }

    public void init(FMLInitializationEvent event) {
        // Register entities
        EntityRegistry.registerModEntity(
            EntitySpellProjectile.class,
            "spell_projectile",
            1, // entity ID
            Psi.class, // Need to use class reference for 1.7.10
            64, // tracking range
            1, // update frequency
            true // send velocity updates
        );
        Psi.logger.info("Registered EntitySpellProjectile");
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {
        // Register test command
        event.registerServerCommand(new vazkii.psi.common.command.CommandPsiTest());
        Psi.logger.info("Registered /psitest command");
    }
}
