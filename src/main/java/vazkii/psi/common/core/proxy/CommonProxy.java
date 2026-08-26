package vazkii.psi.common.core.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import vazkii.psi.common.Psi;
import vazkii.psi.common.block.BlockCADAssembler;
import vazkii.psi.common.block.BlockProgrammer;
import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.core.PsiCreativeTab;
import vazkii.psi.common.core.handler.ConfigHandler;
import vazkii.psi.common.core.handler.GuiHandler;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.item.ItemSpellBullet;
import vazkii.psi.common.item.component.ItemCADAssembly;
import vazkii.psi.common.item.component.ItemCADBattery;
import vazkii.psi.common.item.component.ItemCADComponent;
import vazkii.psi.common.item.component.ItemCADCore;
import vazkii.psi.common.item.component.ItemCADSocket;
import vazkii.psi.common.lib.LibMisc;

public class CommonProxy {

    /** Legacy block renderer ID. The dedicated server uses the vanilla renderer. */
    public int getMachineRenderType() {
        return 0;
    }

    public static Item itemCAD;
    public static Item itemSpellBullet;
    public static Item itemCreativeCAD;
    public static Item itemCADAssemblyIron, itemCADAssemblyGold, itemCADAssemblyPsimetal;
    public static Item itemCADAssemblyEbonyPsimetal, itemCADAssemblyIvoryPsimetal, itemCADAssemblyCreative;
    public static Item itemCADCoreBasic, itemCADCoreOverclocked, itemCADCoreConductive, itemCADCoreHyperclocked,
        itemCADCoreRadiative;
    public static Item itemCADSocketBasic, itemCADSocketSignaling, itemCADSocketLarge, itemCADSocketTransmissive,
        itemCADSocketHuge;
    public static Item itemCADBatteryBasic, itemCADBatteryExtended, itemCADBatteryUltradense;
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
            .setCreativeTab(PsiCreativeTab.TAB);
        GameRegistry.registerItem(itemCAD, "cad");
        itemSpellBullet = new ItemSpellBullet().setUnlocalizedName("psi.spell_bullet")
            .setTextureName("psi:spell_bullet")
            .setCreativeTab(PsiCreativeTab.TAB);
        itemCreativeCAD = new ItemCreativeCAD().setUnlocalizedName("psi.cad_creative")
            .setTextureName("psi:cad_creative_gizmo")
            .setCreativeTab(PsiCreativeTab.TAB);
        GameRegistry.registerItem(itemSpellBullet, "spell_bullet");
        GameRegistry.registerItem(itemCreativeCAD, "cad_creative");
        itemCADAssemblyIron = registerComponent(
            new ItemCADAssembly(),
            "cad_assembly_iron",
            "Efficiency",
            70,
            "Potency",
            100);
        itemCADAssemblyGold = registerComponent(
            new ItemCADAssembly(),
            "cad_assembly_gold",
            "Efficiency",
            75,
            "Potency",
            175);
        itemCADAssemblyPsimetal = registerComponent(
            new ItemCADAssembly(),
            "cad_assembly_psimetal",
            "Efficiency",
            85,
            "Potency",
            250);
        itemCADAssemblyEbonyPsimetal = registerComponent(
            new ItemCADAssembly(),
            "cad_assembly_ebony_psimetal",
            "Efficiency",
            90,
            "Potency",
            350);
        itemCADAssemblyIvoryPsimetal = registerComponent(
            new ItemCADAssembly(),
            "cad_assembly_ivory_psimetal",
            "Efficiency",
            95,
            "Potency",
            320);
        itemCADAssemblyCreative = registerComponent(
            new ItemCADAssembly(),
            "cad_assembly_creative",
            "Efficiency",
            -1,
            "Potency",
            -1);

        itemCADCoreBasic = registerComponent(new ItemCADCore(), "cad_core_basic", "Complexity", 14, "Projection", 1);
        itemCADCoreOverclocked = registerComponent(
            new ItemCADCore(),
            "cad_core_overclocked",
            "Complexity",
            24,
            "Projection",
            3);
        itemCADCoreConductive = registerComponent(
            new ItemCADCore(),
            "cad_core_conductive",
            "Complexity",
            20,
            "Projection",
            4);
        itemCADCoreHyperclocked = registerComponent(
            new ItemCADCore(),
            "cad_core_hyperclocked",
            "Complexity",
            36,
            "Projection",
            6);
        itemCADCoreRadiative = registerComponent(
            new ItemCADCore(),
            "cad_core_radiative",
            "Complexity",
            30,
            "Projection",
            7);

        itemCADSocketBasic = registerComponent(
            new ItemCADSocket(),
            "cad_socket_basic",
            "Bandwidth",
            5,
            "Sockets",
            4,
            ItemCAD.STAT_MEMORY,
            7);
        itemCADSocketSignaling = registerComponent(
            new ItemCADSocket(),
            "cad_socket_signaling",
            "Bandwidth",
            7,
            "Sockets",
            6,
            ItemCAD.STAT_MEMORY,
            14);
        itemCADSocketLarge = registerComponent(
            new ItemCADSocket(),
            "cad_socket_large",
            "Bandwidth",
            6,
            "Sockets",
            8,
            ItemCAD.STAT_MEMORY,
            14);
        itemCADSocketTransmissive = registerComponent(
            new ItemCADSocket(),
            "cad_socket_transmissive",
            "Bandwidth",
            9,
            "Sockets",
            10,
            ItemCAD.STAT_MEMORY,
            18);
        itemCADSocketHuge = registerComponent(
            new ItemCADSocket(),
            "cad_socket_huge",
            "Bandwidth",
            8,
            "Sockets",
            12,
            ItemCAD.STAT_MEMORY,
            21);

        itemCADBatteryBasic = registerComponent(new ItemCADBattery(), "cad_battery_basic", "Overflow", 100);
        itemCADBatteryExtended = registerComponent(new ItemCADBattery(), "cad_battery_extended", "Overflow", 200);
        itemCADBatteryUltradense = registerComponent(new ItemCADBattery(), "cad_battery_ultradense", "Overflow", 400);
        Psi.logger.info("Registered ItemCAD");

        blockProgrammer = new BlockProgrammer().setCreativeTab(PsiCreativeTab.TAB);
        blockCADAssembler = new BlockCADAssembler().setCreativeTab(PsiCreativeTab.TAB);
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

    private static Item registerComponent(ItemCADComponent item, String name, Object... stats) {
        item.setUnlocalizedName("psi." + name)
            .setTextureName("psi:" + name)
            .setCreativeTab(PsiCreativeTab.TAB);
        for (int i = 0; i < stats.length; i += 2) {
            item.addStat((String) stats[i], ((Integer) stats[i + 1]).intValue());
        }
        GameRegistry.registerItem(item, name);
        return item;
    }
}
