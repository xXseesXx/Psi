package vazkii.psi.common.network;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.item.ItemCAD;
public class PacketCADSelect implements IMessage { private int slot; public PacketCADSelect(){} public PacketCADSelect(int s){slot=s;} public void toBytes(ByteBuf b){b.writeInt(slot);}public void fromBytes(ByteBuf b){slot=b.readInt();} public static class Handler implements IMessageHandler<PacketCADSelect,IMessage>{public IMessage onMessage(PacketCADSelect m,MessageContext c){ItemStack held=c.getServerHandler().playerEntity.getHeldItem();if(held!=null&&held.getItem() instanceof ItemCreativeCAD)ItemCreativeCAD.setSelectedSlot(held,m.slot);else if(held!=null&&held.getItem() instanceof ItemCAD)ItemCAD.setSelectedSlot(held,m.slot);return null;}}}
