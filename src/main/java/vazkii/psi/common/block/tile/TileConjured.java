/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common\block\tile\TileConjured.java:29
 * Modern: BlockEntity with Level, BlockConjured SOLID/LIGHT, ModBlocks registry. GTNH: TileEntity with World,
 * BlockConjured as BlockContainer, ModBlocks stub.
 * Pathing for hard pieces: provides TileConjured.colorize for ConjureBlock tricks, doParticles stub.
 */
package vazkii.psi.common.block.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileConjured extends TileEntity {

    private static final String TAG_COLORIZER = "colorizer";
    public ItemStack colorizer = null;

    public void doParticles() {
        // GTNH stub: modern does Psi.proxy.wispFX/sparkleFX — deferred, no particles for pathing
    }

    @Override
    public void readFromNBT(NBTTagCompound cmp) {
        super.readFromNBT(cmp);
        readPacketNBT(cmp);
    }

    @Override
    public void writeToNBT(NBTTagCompound cmp) {
        super.writeToNBT(cmp);
        if (colorizer != null) {
            NBTTagCompound colorTag = new NBTTagCompound();
            colorizer.writeToNBT(colorTag);
            cmp.setTag(TAG_COLORIZER, colorTag);
        }
    }

    public void readPacketNBT(NBTTagCompound cmp) {
        if (cmp.hasKey(TAG_COLORIZER)) {
            colorizer = ItemStack.loadItemStackFromNBT(cmp.getCompoundTag(TAG_COLORIZER));
        } else {
            colorizer = null;
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound cmp = new NBTTagCompound();
        writeToNBT(cmp);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -1, cmp);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readPacketNBT(pkt.func_148857_g());
    }
}
