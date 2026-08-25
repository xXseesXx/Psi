package vazkii.psi.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.block.tile.TileProgrammer;

/** Server-authoritative persistence for edits made in a programmer block. */
public class PacketProgrammerSpellUpdate implements IMessage {
    private int x,y,z; private NBTTagCompound spell;
    public PacketProgrammerSpellUpdate() {}
    public PacketProgrammerSpellUpdate(int x,int y,int z,Spell spell) { this.x=x;this.y=y;this.z=z;this.spell=new NBTTagCompound();spell.writeToNBT(this.spell); }
    @Override public void toBytes(ByteBuf buf) { buf.writeInt(x);buf.writeInt(y);buf.writeInt(z);ByteBufUtils.writeTag(buf,spell); }
    @Override public void fromBytes(ByteBuf buf) { x=buf.readInt();y=buf.readInt();z=buf.readInt();spell=ByteBufUtils.readTag(buf); }
    public static class Handler implements IMessageHandler<PacketProgrammerSpellUpdate,IMessage> {
        @Override public IMessage onMessage(PacketProgrammerSpellUpdate message,MessageContext ctx) {
            TileEntity te=ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x,message.y,message.z);
            if(te instanceof TileProgrammer && ((TileProgrammer)te).canPlayerInteract(ctx.getServerHandler().playerEntity)) ((TileProgrammer)te).setSpell(Spell.readFromNBT(message.spell));
            return null;
        }
    }
}
