/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/item/ItemVectorRuler.java:1
 * Modern: Item.Properties, ModDataComponents SRC_POS/DST_POS BlockPos, Player Inventory, IHUDItem with GuiGraphics.
 * GTNH: net.minecraft.item.Item, NBT "src_x/y/z" + "dst_x/y/z", EntityPlayer inventory, IHUDItem with GL11.
 */
package vazkii.psi.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.common.item.base.IHUDItem;

public class ItemVectorRuler extends Item implements IHUDItem {

    public ItemVectorRuler() {
        setMaxStackSize(1);
    }

    public static Vector3 getRulerVector(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemVectorRuler) {
                return ((ItemVectorRuler) stack.getItem()).getVector(stack);
            }
        }
        return Vector3.zero.copy();
    }

    // GTNH: left-click block sets src/dst via onItemUse (modern useOn)
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, net.minecraft.world.World world, int x, int y, int z,
        int side, float hitX, float hitY, float hitZ) {
        if (player == null) return false;
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("src_x") || player.isSneaking()) {
            tag.setInteger("src_x", x);
            tag.setInteger("src_y", y);
            tag.setInteger("src_z", z);
            tag.removeTag("dst_x");
        } else {
            tag.setInteger("dst_x", x);
            tag.setInteger("dst_y", y);
            tag.setInteger("dst_z", z);
        }
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(getVector(stack).toString());
    }

    public Vector3 getVector(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return Vector3.zero.copy();
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("src_x") || !tag.hasKey("dst_x")) return Vector3.zero.copy();
        Vector3 src = new Vector3(tag.getInteger("src_x"), tag.getInteger("src_y"), tag.getInteger("src_z"));
        Vector3 dst = new Vector3(tag.getInteger("dst_x"), tag.getInteger("dst_y"), tag.getInteger("dst_z"));
        return dst.copy()
            .sub(src);
    }

    @Override
    public void drawHUD(float partialTicks, int screenWidth, int screenHeight, ItemStack stack) {
        // No-op HUD for GTNH — modern draws vector string via GuiGraphics
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal(getUnlocalizedName() + ".name");
    }
}
