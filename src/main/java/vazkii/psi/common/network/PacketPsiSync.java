package vazkii.psi.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import vazkii.psi.common.Psi;

public class PacketPsiSync implements IMessage {

    private int previous, current, maximum;

    public PacketPsiSync() {}

    public PacketPsiSync(int previous, int current, int maximum) {
        this.previous = previous;
        this.current = current;
        this.maximum = maximum;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(previous);
        buf.writeInt(current);
        buf.writeInt(maximum);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        previous = buf.readInt();
        current = buf.readInt();
        maximum = buf.readInt();
    }

    public static class Handler implements IMessageHandler<PacketPsiSync, IMessage> {

        @Override
        public IMessage onMessage(final PacketPsiSync message, MessageContext context) {
            Psi.proxy.handlePsiSync(message.previous, message.current, message.maximum);
            return null;
        }
    }
}
