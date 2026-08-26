package vazkii.psi.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import vazkii.psi.common.core.handler.DetonationHandler;

/** Detonates active spell charges within the normal 32-block spell radius. */
public class ItemDetonator extends Item {

    public ItemDetonator() {
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) DetonationHandler.detonate(world, player, player.posX, player.posY, player.posZ, 32);
        return stack;
    }
}
