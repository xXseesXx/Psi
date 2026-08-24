package vazkii.psi.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.item.ItemCAD;

/**
 * Packet to synchronize spell data from client to server.
 * Sent whenever the player modifies a spell in the GUI.
 */
public class PacketSpellUpdate implements IMessage {

    private NBTTagCompound spellNBT;

    /**
     * Default constructor required by FML.
     */
    public PacketSpellUpdate() {}

    /**
     * Create a new spell update packet.
     * 
     * @param spell The spell to synchronize
     */
    public PacketSpellUpdate(Spell spell) {
        this.spellNBT = new NBTTagCompound();
        spell.writeToNBT(this.spellNBT);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, spellNBT);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        spellNBT = ByteBufUtils.readTag(buf);
    }

    /**
     * Server-side handler for spell update packets.
     */
    public static class Handler implements IMessageHandler<PacketSpellUpdate, IMessage> {

        @Override
        public IMessage onMessage(PacketSpellUpdate message, MessageContext ctx) {
            // Get the player who sent the packet
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;

            // Get the item the player is holding
            ItemStack heldItem = player.getHeldItem();

            // Verify it's a CAD
            if (heldItem != null && heldItem.getItem() instanceof ItemCAD) {
                // Read spell from NBT
                Spell spell = Spell.readFromNBT(message.spellNBT);

                // Save spell to CAD on server side
                ItemCAD.setSpell(heldItem, spell);

                System.out.println(
                    "[Psi] Saved spell '" + spell.name + "' to CAD for player " + player.getCommandSenderName());
            }

            return null; // No response needed
        }
    }
}
