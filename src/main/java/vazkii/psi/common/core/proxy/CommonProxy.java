package vazkii.psi.common.core.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import vazkii.psi.api.cad.ICADColorizer;
import vazkii.psi.common.Psi;
import vazkii.psi.common.block.BlockCADAssembler;
import vazkii.psi.common.block.BlockProgrammer;
import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.core.PsiCreativeTab;
import vazkii.psi.common.core.handler.ConfigHandler;
import vazkii.psi.common.core.handler.GuiHandler;
import vazkii.psi.common.core.handler.LoopcastHandler;
import vazkii.psi.common.core.handler.PlayerPsiHandler;
import vazkii.psi.common.entity.EntitySpellCharge;
import vazkii.psi.common.entity.EntitySpellCircle;
import vazkii.psi.common.entity.EntitySpellGrenade;
import vazkii.psi.common.entity.EntitySpellMine;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemChargeSpellBullet;
import vazkii.psi.common.item.ItemCircleSpellBullet;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.item.ItemDetonator;
import vazkii.psi.common.item.ItemGrenadeSpellBullet;
import vazkii.psi.common.item.ItemLoopcastSpellBullet;
import vazkii.psi.common.item.ItemMineSpellBullet;
import vazkii.psi.common.item.ItemProjectileSpellBullet;
import vazkii.psi.common.item.ItemSpellBullet;
import vazkii.psi.common.item.component.ItemCADAssembly;
import vazkii.psi.common.item.component.ItemCADBattery;
import vazkii.psi.common.item.component.ItemCADColorizer;
import vazkii.psi.common.item.component.ItemCADColorizerEmpty;
import vazkii.psi.common.item.component.ItemCADColorizerPsi;
import vazkii.psi.common.item.component.ItemCADColorizerRainbow;
import vazkii.psi.common.item.component.ItemCADComponent;
import vazkii.psi.common.item.component.ItemCADCore;
import vazkii.psi.common.item.component.ItemCADSocket;
import vazkii.psi.common.lib.LibMisc;

public class CommonProxy {

    /** Client-only proxies receive authoritative Psi-bar updates through this no-op server hook. */
    public void handlePsiSync(int previous, int current, int maximum) {}

    public void handleLoopcastSync(int entityId, boolean loopcasting) {}

    /** Animated colorizers read this; dedicated servers always report zero. */
    public float getFrameTicks() {
        return 0F;
    }

    public int getColorForColorizer(ItemStack colorizer) {
        if (colorizer != null && colorizer.getItem() instanceof ICADColorizer)
            return ((ICADColorizer) colorizer.getItem()).getColor(colorizer);
        return ICADColorizer.DEFAULT_SPELL_COLOR;
    }

    public void sparkleFX(double x, double y, double z, float r, float g, float b, float motionX, float motionY,
        float motionZ, float size, int ageMultiplier) {}

    public void wispFX(double x, double y, double z, float r, float g, float b, float size, float motionX,
        float motionY, float motionZ, float maxAgeMultiplier) {}

    /** Legacy block renderer ID. The dedicated server uses the vanilla renderer. */
    public int getMachineRenderType() {
        return 0;
    }

    public static Item itemCAD;
    public static Item itemSpellBullet, itemSpellBulletProjectile, itemSpellBulletLoop, itemSpellBulletCircle,
        itemSpellBulletGrenade, itemSpellBulletCharge, itemSpellBulletMine;
    public static Item itemCreativeCAD;
    public static Item itemDetonator;
    public static Item itemCADAssemblyIron, itemCADAssemblyGold, itemCADAssemblyPsimetal;
    public static Item itemCADAssemblyEbonyPsimetal, itemCADAssemblyIvoryPsimetal, itemCADAssemblyCreative;
    public static Item itemCADCoreBasic, itemCADCoreOverclocked, itemCADCoreConductive, itemCADCoreHyperclocked,
        itemCADCoreRadiative;
    public static Item itemCADSocketBasic, itemCADSocketSignaling, itemCADSocketLarge, itemCADSocketTransmissive,
        itemCADSocketHuge;
    public static Item itemCADBatteryBasic, itemCADBatteryExtended, itemCADBatteryUltradense;
    public static Item[] itemCADColorizerDyes = new Item[ItemCADColorizer.DYE_NAMES.length];
    public static Item itemCADColorizerRainbow, itemCADColorizerPsi, itemCADColorizerEmpty;
    public static Item itemCADColorizerWhite, itemCADColorizerOrange, itemCADColorizerMagenta,
        itemCADColorizerLightBlue, itemCADColorizerYellow, itemCADColorizerLime, itemCADColorizerPink,
        itemCADColorizerGray, itemCADColorizerLightGray, itemCADColorizerCyan, itemCADColorizerPurple,
        itemCADColorizerBlue, itemCADColorizerBrown, itemCADColorizerGreen, itemCADColorizerRed, itemCADColorizerBlack;
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
        itemSpellBulletProjectile = registerBullet(new ItemProjectileSpellBullet(), "spell_bullet_projectile");
        itemSpellBulletLoop = registerBullet(new ItemLoopcastSpellBullet(), "spell_bullet_loop");
        itemSpellBulletCircle = registerBullet(new ItemCircleSpellBullet(), "spell_bullet_circle");
        itemSpellBulletGrenade = registerBullet(new ItemGrenadeSpellBullet(), "spell_bullet_grenade");
        itemSpellBulletCharge = registerBullet(new ItemChargeSpellBullet(), "spell_bullet_charge");
        itemSpellBulletMine = registerBullet(new ItemMineSpellBullet(), "spell_bullet_mine");
        itemDetonator = new ItemDetonator().setUnlocalizedName("psi.detonator")
            .setTextureName("psi:detonator")
            .setCreativeTab(PsiCreativeTab.TAB);
        GameRegistry.registerItem(itemDetonator, "detonator");
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

        // DyeColor.getTextColor() values from current Psi, used to tint the colorizer inside.
        itemCADColorizerWhite = registerColorizer(new ItemCADColorizer(0xF9FFFE), "cad_colorizer_white");
        itemCADColorizerOrange = registerColorizer(new ItemCADColorizer(0xF9801D), "cad_colorizer_orange");
        itemCADColorizerMagenta = registerColorizer(new ItemCADColorizer(0xC74EBD), "cad_colorizer_magenta");
        itemCADColorizerLightBlue = registerColorizer(new ItemCADColorizer(0x3AB3DA), "cad_colorizer_light_blue");
        itemCADColorizerYellow = registerColorizer(new ItemCADColorizer(0xFED83D), "cad_colorizer_yellow");
        itemCADColorizerLime = registerColorizer(new ItemCADColorizer(0x80C71F), "cad_colorizer_lime");
        itemCADColorizerPink = registerColorizer(new ItemCADColorizer(0xF38BAA), "cad_colorizer_pink");
        itemCADColorizerGray = registerColorizer(new ItemCADColorizer(0x474F52), "cad_colorizer_gray");
        itemCADColorizerLightGray = registerColorizer(new ItemCADColorizer(0x9D9D97), "cad_colorizer_light_gray");
        itemCADColorizerCyan = registerColorizer(new ItemCADColorizer(0x169C9C), "cad_colorizer_cyan");
        itemCADColorizerPurple = registerColorizer(new ItemCADColorizer(0x8932B8), "cad_colorizer_purple");
        itemCADColorizerBlue = registerColorizer(new ItemCADColorizer(0x3C44AA), "cad_colorizer_blue");
        itemCADColorizerBrown = registerColorizer(new ItemCADColorizer(0x835432), "cad_colorizer_brown");
        itemCADColorizerGreen = registerColorizer(new ItemCADColorizer(0x5E7C16), "cad_colorizer_green");
        itemCADColorizerRed = registerColorizer(new ItemCADColorizer(0xB02E26), "cad_colorizer_red");
        itemCADColorizerBlack = registerColorizer(new ItemCADColorizer(0x1D1D21), "cad_colorizer_black");
        itemCADColorizerDyes = new Item[] { itemCADColorizerWhite, itemCADColorizerOrange, itemCADColorizerMagenta,
            itemCADColorizerLightBlue, itemCADColorizerYellow, itemCADColorizerLime, itemCADColorizerPink,
            itemCADColorizerGray, itemCADColorizerLightGray, itemCADColorizerCyan, itemCADColorizerPurple,
            itemCADColorizerBlue, itemCADColorizerBrown, itemCADColorizerGreen, itemCADColorizerRed,
            itemCADColorizerBlack };
        itemCADColorizerRainbow = registerColorizer(new ItemCADColorizerRainbow(), "cad_colorizer_rainbow");
        itemCADColorizerPsi = registerColorizer(new ItemCADColorizerPsi(), "cad_colorizer_psi");
        itemCADColorizerEmpty = registerColorizer(new ItemCADColorizerEmpty(), "cad_colorizer_empty");
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
        FMLCommonHandler.instance()
            .bus()
            .register(new LoopcastHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(new PlayerPsiHandler());
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
        EntityRegistry.registerModEntity(EntitySpellGrenade.class, "spell_grenade", 2, Psi.instance, 64, 1, true);
        EntityRegistry.registerModEntity(EntitySpellMine.class, "spell_mine", 3, Psi.instance, 64, 1, true);
        EntityRegistry.registerModEntity(EntitySpellCharge.class, "spell_charge", 4, Psi.instance, 64, 1, true);
        EntityRegistry.registerModEntity(EntitySpellCircle.class, "spell_circle", 5, Psi.instance, 64, 1, true);
        vazkii.psi.common.crafting.ColorizerChangeRecipe colorizerRecipe = new vazkii.psi.common.crafting.ColorizerChangeRecipe();
        GameRegistry.addRecipe(colorizerRecipe);
        FMLCommonHandler.instance()
            .bus()
            .register(colorizerRecipe);
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

    private static Item registerColorizer(ItemCADColorizer item, String name) {
        item.setUnlocalizedName("psi." + name)
            .setCreativeTab(PsiCreativeTab.TAB);
        GameRegistry.registerItem(item, name);
        return item;
    }

    private static Item registerBullet(ItemSpellBullet item, String name) {
        item.setUnlocalizedName("psi." + name)
            .setTextureName("psi:" + name)
            .setCreativeTab(PsiCreativeTab.TAB);
        GameRegistry.registerItem(item, name);
        return item;
    }
}
