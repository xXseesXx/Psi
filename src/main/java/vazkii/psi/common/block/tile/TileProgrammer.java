package vazkii.psi.common.block.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompiler;

/** Persistent spell workspace used by the Spell Programmer block. */
public class TileProgrammer extends TileEntity {
    private static final String TAG_SPELL = "spell";
    public Spell spell;

    public boolean isEnabled() { return spell != null && !spell.grid.isEmpty(); }
    public boolean canCompile() {
        if (!isEnabled()) return false;
        try { new SpellCompiler().compile(spell); return true; } catch (Exception ignored) { return false; }
    }
    public boolean canPlayerInteract(EntityPlayer player) {
        return !player.isDead && player.getDistanceSq(xCoord + .5D, yCoord + .5D, zCoord + .5D) <= 64D;
    }
    public void setSpell(Spell updated) {
        spell = updated;
        markDirty();
        if (worldObj != null) {
            int facing = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 3;
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, facing | (isEnabled() ? 8 : 0), 2);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (spell != null) { NBTTagCompound spellTag = new NBTTagCompound(); spell.writeToNBT(spellTag); tag.setTag(TAG_SPELL, spellTag); }
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        spell = tag.hasKey(TAG_SPELL) ? Spell.readFromNBT(tag.getCompoundTag(TAG_SPELL)) : null;
    }
    @Override public S35PacketUpdateTileEntity getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }
    @Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g()); }
}
