package vazkii.psi.common.core.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.network.PacketHandler;
import vazkii.psi.common.network.PacketPsiSync;

/** Persistent player Psi pool: modern Psi's default is 5000 capacity and 25 Psi/tick regeneration. */
public class PlayerPsiHandler {

    public static final int MAX_PSI = 5000;
    public static final int REGEN_PER_TICK = 25;
    private static final String TAG = "PsiData";
    private static final String PSI = "availablePsi";
    private static final String REGEN_COOLDOWN = "regenCooldown";
    private static final int CAST_REGEN_DELAY = 10;

    public static int get(EntityPlayer player) {
        NBTTagCompound tag = data(player);
        if (!tag.hasKey(PSI)) {
            tag.setInteger(PSI, MAX_PSI);
        }
        return tag.getInteger(PSI);
    }

    public static boolean spend(EntityPlayer player, int cost, ItemStack cad) {
        int current = get(player);
        int overflow = cad != null && cad.getItem() instanceof ItemCAD ? ItemCAD.getStoredPsi(cad) : 0;
        if (overflow < 0) overflow = cost;
        if (cost > current + overflow) return false;
        int remainder = Math.max(0, cost - current);
        set(player, Math.max(0, current - cost));
        if (remainder > 0) ItemCAD.consumeStoredPsi(cad, remainder);
        data(player).setInteger(REGEN_COOLDOWN, CAST_REGEN_DELAY);
        return true;
    }

    private static void set(EntityPlayer player, int amount) {
        int previous = get(player);
        data(player).setInteger(PSI, Math.max(0, Math.min(MAX_PSI, amount)));
        if (!player.worldObj.isRemote && player instanceof EntityPlayerMP)
            PacketHandler.INSTANCE.sendTo(new PacketPsiSync(previous, get(player), MAX_PSI), (EntityPlayerMP) player);
    }

    private static NBTTagCompound data(EntityPlayer player) {
        NBTTagCompound root = player.getEntityData();
        if (!root.hasKey(TAG)) root.setTag(TAG, new NBTTagCompound());
        return root.getCompoundTag(TAG);
    }

    @SubscribeEvent
    public void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.worldObj.isRemote) return;
        int current = get(event.player);
        NBTTagCompound data = data(event.player);
        int cooldown = data.getInteger(REGEN_COOLDOWN);
        if (cooldown > 0) {
            data.setInteger(REGEN_COOLDOWN, cooldown - 1);
            return;
        }
        ItemStack cad = event.player.getHeldItem();
        if (cad != null && cad.getItem() instanceof ItemCAD
            && ItemCAD.getStoredPsi(cad) < ItemCAD.getStat(cad, "Overflow")) {
            ItemCAD.regenStoredPsi(cad, REGEN_PER_TICK);
        } else if (current < MAX_PSI) set(event.player, current + REGEN_PER_TICK);
    }
}
