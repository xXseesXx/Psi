package vazkii.psi.api.cad;

import net.minecraft.nbt.NBTTagCompound;

import vazkii.psi.api.internal.Vector3;

/**
 * Persistent runtime data stored by a CAD.
 */
public interface ICADData {

    int getTime();

    void setTime(int time);

    int getBattery();

    void setBattery(int battery);

    Vector3 getSavedVector(int memorySlot);

    void setSavedVector(int memorySlot, Vector3 value);

    NBTTagCompound serializeForSynchronization();
}
