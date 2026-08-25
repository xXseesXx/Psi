package vazkii.psi.common.core.proxy;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.block.Block;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import vazkii.psi.common.Psi;
import vazkii.psi.common.core.handler.ConfigHandler;
import vazkii.psi.common.core.handler.GuiHandler;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.item.ItemSpellBullet;
import vazkii.psi.common.block.BlockCADAssembler;
import vazkii.psi.common.block.BlockProgrammer;
import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.lib.LibMisc;

public class CommonProxy {

    /** Legacy block renderer ID. The dedicated server uses the vanilla renderer. */
    public int getMachineRenderType() { return 0; }

    public static Item itemCAD;
    public static Item itemSpellBullet;
    public static Item itemCreativeCAD;
    public static Block blockProgrammer;
    public static Block blockCADAssembler;

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
        itemSpellBullet = new ItemSpellBullet().setUnlocalizedName("psi.spell_bullet").setTextureName("psi:spell_bullet").setCreativeTab(CreativeTabs.tabTools);
        itemCreativeCAD = new ItemCreativeCAD().setUnlocalizedName("psi.cad_creative").setTextureName("psi:cad_creative_gizmo").setCreativeTab(CreativeTabs.tabTools);
        GameRegistry.registerItem(itemSpellBullet, "spell_bullet");
        GameRegistry.registerItem(itemCreativeCAD, "cad_creative");
        Psi.logger.info("Registered ItemCAD");

        blockProgrammer = new BlockProgrammer().setCreativeTab(CreativeTabs.tabDecorations);
        blockCADAssembler = new BlockCADAssembler().setCreativeTab(CreativeTabs.tabDecorations);
        GameRegistry.registerBlock(blockProgrammer, "programmer");
        GameRegistry.registerBlock(blockCADAssembler, "cad_assembler");
        GameRegistry.registerTileEntity(TileProgrammer.class, "psi_programmer");
        GameRegistry.registerTileEntity(TileCADAssembler.class, "psi_cad_assembler");
        Psi.logger.info("Registered programmer and CAD assembler blocks");
    }

    public void init(FMLInitializationEvent event) {
        // Register GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(Psi.instance, new GuiHandler());
        Psi.logger.info("Registered GuiHandler");

        // Register packet handler for client-server communication
        vazkii.psi.common.network.PacketHandler.init();
        Psi.logger.info("Registered PacketHandler");

        // Register entities
        EntityRegistry.registerModEntity(
            EntitySpellProjectile.class,
            "spell_projectile",
            1, // entity ID
            Psi.instance, // Mod instance
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
