/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.client.core.proxy;

import vazkii.psi.common.core.proxy.CommonProxy;
import cpw.mods.fml.common.FMLCommonHandler;
import vazkii.psi.client.core.handler.KeybindHandler;
import vazkii.psi.client.render.BlockMachineRenderer;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.client.registry.ClientRegistry;
import vazkii.psi.client.render.tile.RenderTileProgrammer;
import vazkii.psi.common.block.tile.TileProgrammer;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    private int machineRenderType;

    @Override public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        machineRenderType = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new BlockMachineRenderer(machineRenderType));
        ClientRegistry.bindTileEntitySpecialRenderer(TileProgrammer.class, new RenderTileProgrammer());
        KeybindHandler handler=new KeybindHandler(); KeybindHandler.init(); FMLCommonHandler.instance().bus().register(handler);
    }

    @Override public int getMachineRenderType() { return machineRenderType; }

}
