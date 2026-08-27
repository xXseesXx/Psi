/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.client.core.proxy;

import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import vazkii.psi.client.core.handler.ClientTickHandler;
import vazkii.psi.client.core.handler.KeybindHandler;
import vazkii.psi.client.core.handler.LoopcastRenderHandler;
import vazkii.psi.client.core.handler.HUDHandler;
import vazkii.psi.client.fx.FXSparkle;
import vazkii.psi.client.fx.FXWisp;
import vazkii.psi.client.render.BlockMachineRenderer;
import vazkii.psi.client.render.RenderItemCAD;
import vazkii.psi.client.render.entity.RenderSpellCircle;
import vazkii.psi.client.render.entity.RenderSpellProjectile;
import vazkii.psi.client.render.tile.RenderTileProgrammer;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.core.proxy.CommonProxy;
import vazkii.psi.common.entity.EntitySpellCharge;
import vazkii.psi.common.entity.EntitySpellCircle;
import vazkii.psi.common.entity.EntitySpellGrenade;
import vazkii.psi.common.entity.EntitySpellMine;
import vazkii.psi.common.entity.EntitySpellProjectile;

public class ClientProxy extends CommonProxy {

    private int machineRenderType;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        machineRenderType = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new BlockMachineRenderer(machineRenderType));

        ClientRegistry.bindTileEntitySpecialRenderer(TileProgrammer.class, new RenderTileProgrammer());

        MinecraftForgeClient.registerItemRenderer(CommonProxy.itemCAD, new RenderItemCAD());
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellCircle.class, new RenderSpellCircle());
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellProjectile.class, new RenderSpellProjectile());
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellGrenade.class, new RenderSpellProjectile());
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellMine.class, new RenderSpellProjectile());
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellCharge.class, new RenderSpellProjectile());

        KeybindHandler handler = new KeybindHandler();
        KeybindHandler.init();

        FMLCommonHandler.instance()
            .bus()
            .register(handler);
        FMLCommonHandler.instance()
            .bus()
            .register(new ClientTickHandler());

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new HUDHandler());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new LoopcastRenderHandler());
    }

    @Override
    public int getMachineRenderType() {
        return machineRenderType;
    }

    @Override
    public float getFrameTicks() {
        return ClientTickHandler.total;
    }

    @Override
    public void handlePsiSync(final int previous, final int current, final int maximum) {
        net.minecraft.client.Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    HUDHandler.setPsi(previous, current, maximum);
                }
            });
    }

    @Override
    public void handleLoopcastSync(final int entityId, final boolean loopcasting) {
        net.minecraft.client.Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    LoopcastRenderHandler.setLoopcasting(entityId, loopcasting);
                }
            });
    }

    @Override
    public void sparkleFX(double x, double y, double z, float r, float g, float b, float motionX, float motionY,
        float motionZ, float size, int ageMultiplier) {
        if (ageMultiplier != 0 && net.minecraft.client.Minecraft.getMinecraft().theWorld != null) {
            net.minecraft.client.Minecraft.getMinecraft().effectRenderer.addEffect(
                new FXSparkle(
                    net.minecraft.client.Minecraft.getMinecraft().theWorld,
                    x,
                    y,
                    z,
                    size,
                    r,
                    g,
                    b,
                    ageMultiplier,
                    motionX,
                    motionY,
                    motionZ));
        }
    }

    @Override
    public void wispFX(double x, double y, double z, float r, float g, float b, float size, float motionX,
        float motionY, float motionZ, float maxAgeMultiplier) {
        if (maxAgeMultiplier != 0 && net.minecraft.client.Minecraft.getMinecraft().theWorld != null) {
            net.minecraft.client.Minecraft.getMinecraft().effectRenderer.addEffect(
                new FXWisp(
                    net.minecraft.client.Minecraft.getMinecraft().theWorld,
                    x,
                    y,
                    z,
                    motionX,
                    motionY,
                    motionZ,
                    size,
                    r,
                    g,
                    b,
                    maxAgeMultiplier));
        }
    }
}
