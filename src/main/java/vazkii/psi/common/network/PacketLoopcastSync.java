package vazkii.psi.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import vazkii.psi.common.Psi;

/** Synchronizes a player's loopcast ring to clients tracking that player. */
public class PacketLoopcastSync implements IMessage {

    private int entityId;
    private boolean loopcasting;

    public PacketLoopcastSync() {}

    public PacketLoopcastSync(int entityId, boolean loopcasting) {
        this.entityId = entityId;
        this.loopcasting = loopcasting;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeBoolean(loopcasting);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        entityId = buffer.readInt();
        loopcasting = buffer.readBoolean();
    }

    public static class Handler implements IMessageHandler<PacketLoopcastSync, IMessage> {
        @Override
        public IMessage onMessage(PacketLoopcastSync message, MessageContext context) {
            Psi.proxy.handleLoopcastSync(message.entityId, message.loopcasting);
            return null;
        }
    }
}
