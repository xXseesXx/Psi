package vazkii.psi.common.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * Handles network packet registration for Psi mod.
 * Manages client-server communication for spell updates.
 */
public class PacketHandler {

    public static SimpleNetworkWrapper INSTANCE;

    /**
     * Initialize the packet handler and register all packets.
     * Must be called during mod initialization (preInit or init phase).
     */
    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("psi");

        // Register spell update packet (client -> server)
        // Packet ID 0: Spell update from GUI to server
        INSTANCE.registerMessage(PacketSpellUpdate.Handler.class, PacketSpellUpdate.class, 0, Side.SERVER);
        INSTANCE.registerMessage(PacketProgrammerSpellUpdate.Handler.class, PacketProgrammerSpellUpdate.class, 1, Side.SERVER);
        INSTANCE.registerMessage(PacketCADSelect.Handler.class, PacketCADSelect.class, 2, Side.SERVER);
    }
}
