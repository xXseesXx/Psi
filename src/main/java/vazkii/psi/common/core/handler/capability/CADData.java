package vazkii.psi.common.core.handler.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;

import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.cad.ICADData;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.item.component.ItemCADSocket;

/**
 * 1.7.10 replacement for the modern capability-backed CADData.
 *
 * All data is stored on the CAD ItemStack under the PsiCADData tag.
 */
public class CADData implements ICADData, ISpellAcceptor, ISocketable {

    private static final String TAG_DATA = "PsiCADData";
    private static final String TAG_TIME = "Time";
    private static final String TAG_BATTERY = "Battery";
    private static final String TAG_MEMORY = "Memory";
    private static final String TAG_SELECTED = "SelectedSlot";
    private static final String TAG_BULLET_PREFIX = "Bullet";

    private final ItemStack cad;

    public CADData(ItemStack cad) {
        this.cad = cad;
    }

    private NBTTagCompound data() {
        NBTTagCompound root = cad.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            cad.setTagCompound(root);
        }

        if (!root.hasKey(TAG_DATA, 10)) {
            root.setTag(TAG_DATA, new NBTTagCompound());
        }

        return root.getCompoundTag(TAG_DATA);
    }

    @Override
    public int getTime() {
        return data().getInteger(TAG_TIME);
    }

    @Override
    public void setTime(int time) {
        data().setInteger(TAG_TIME, time);
    }

    @Override
    public int getBattery() {
        return data().getInteger(TAG_BATTERY);
    }

    @Override
    public void setBattery(int battery) {
        data().setInteger(TAG_BATTERY, battery);
    }

    @Override
    public Vector3 getSavedVector(int memorySlot) {
        if (memorySlot < 0) return Vector3.zero.copy();

        NBTTagList memory = data().getTagList(TAG_MEMORY, 6);
        if (memorySlot >= memory.tagCount()) return Vector3.zero.copy();

        return new Vector3(
            memory.func_150309_d(memorySlot * 3),
            memory.func_150309_d(memorySlot * 3 + 1),
            memory.func_150309_d(memorySlot * 3 + 2));
    }

    @Override
    public void setSavedVector(int memorySlot, Vector3 value) {
        if (memorySlot < 0) return;

        NBTTagList memory = data().getTagList(TAG_MEMORY, 6);
        while (memory.tagCount() < (memorySlot + 1) * 3) {
            memory.appendTag(new net.minecraft.nbt.NBTTagDouble(0D));
        }

        if (value == null) {
            memory.func_150304_a(memorySlot * 3, new net.minecraft.nbt.NBTTagDouble(0D));
            memory.func_150304_a(memorySlot * 3 + 1, new net.minecraft.nbt.NBTTagDouble(0D));
            memory.func_150304_a(memorySlot * 3 + 2, new net.minecraft.nbt.NBTTagDouble(0D));
        } else {
            memory.func_150304_a(memorySlot * 3, new net.minecraft.nbt.NBTTagDouble(value.x));
            memory.func_150304_a(memorySlot * 3 + 1, new net.minecraft.nbt.NBTTagDouble(value.y));
            memory.func_150304_a(memorySlot * 3 + 2, new net.minecraft.nbt.NBTTagDouble(value.z));
        }

        data().setTag(TAG_MEMORY, memory);
    }

    @Override
    public NBTTagCompound serializeForSynchronization() {
        NBTTagCompound result = new NBTTagCompound();
        result.setInteger(TAG_TIME, getTime());
        result.setInteger(TAG_BATTERY, getBattery());
        return result;
    }

    @Override
    public void setSpell(EntityPlayer player, Spell spell) {
        int slot = getSelectedSlot();
        ItemStack bullet = getBulletInSocket(slot);

        if (bullet != null && bullet.stackSize > 0 && ISpellAcceptor.isAcceptor(bullet)) {
            ISpellAcceptor.acceptor(bullet)
                .setSpell(player, spell);
            setBulletInSocket(slot, bullet);

        }
    }

    @Override
    public boolean requiresSneakForSpellSet() {
        return true;
    }

    @Override
    public boolean isSocketSlotAvailable(int slot) {
        if (!(cad.getItem() instanceof ICAD)) return false;

        int sockets = ((ICAD) cad.getItem()).getStatValue(cad, EnumCADStat.SOCKETS);
        if (sockets == -1 || sockets > ItemCADSocket.MAX_SOCKETS) {
            sockets = ItemCADSocket.MAX_SOCKETS;
        }

        return slot >= 0 && slot < sockets;
    }

    @Override
    public ItemStack getBulletInSocket(int slot) {
        if (!isSocketSlotAvailable(slot)) return null;

        NBTTagCompound root = data();
        String key = TAG_BULLET_PREFIX + slot;
        if (!root.hasKey(key, 10)) return null;

        return ItemStack.loadItemStackFromNBT(root.getCompoundTag(key));
    }

    @Override
    public void setBulletInSocket(int slot, ItemStack bullet) {
        if (!isSocketSlotAvailable(slot)) return;

        NBTTagCompound root = data();
        String key = TAG_BULLET_PREFIX + slot;

        if (bullet == null || bullet.stackSize <= 0) {
            root.removeTag(key);
        } else {
            NBTTagCompound serialized = new NBTTagCompound();
            bullet.writeToNBT(serialized);
            root.setTag(key, serialized);
        }
    }

    @Override
    public int getSelectedSlot() {
        return MathHelper.clamp_int(data().getInteger(TAG_SELECTED), 0, Math.max(0, getLastSlot()));
    }

    @Override
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < ISocketable.MAX_ASSEMBLER_SLOTS) {
            data().setInteger(TAG_SELECTED, slot);
        }
    }

    @Override
    public int getLastSlot() {
        if (!(cad.getItem() instanceof ICAD)) return 0;

        int sockets = ((ICAD) cad.getItem()).getStatValue(cad, EnumCADStat.SOCKETS);
        if (sockets == -1 || sockets > ItemCADSocket.MAX_SOCKETS) {
            sockets = ItemCADSocket.MAX_SOCKETS;
        }

        return Math.max(0, sockets - 1);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack bullet) {
        return ISocketable.super.isItemValid(slot, bullet);
    }

    @Override
    public boolean canLoopcast() {
        return true;
    }
}
