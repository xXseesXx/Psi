package vazkii.psi.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import vazkii.psi.common.entity.EntitySpellGrenade;

public class ItemGrenadeSpellBullet extends ItemSpellBullet {

    @Override
    public void castSpell(ItemStack stack, EntityPlayer caster) {
        caster.worldObj.spawnEntityInWorld(new EntitySpellGrenade(caster.worldObj, caster, getSpell(stack)));
    }

    @Override
    public double getCostModifier() {
        return 1.05;
    }

    @Override
    public String getBulletType() {
        return "grenade";
    }
}
