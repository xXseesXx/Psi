package vazkii.psi.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import vazkii.psi.common.entity.EntitySpellCharge;

public class ItemChargeSpellBullet extends ItemSpellBullet {

    @Override
    public void castSpell(ItemStack stack, EntityPlayer caster) {
        caster.worldObj.spawnEntityInWorld(new EntitySpellCharge(caster.worldObj, caster, getSpell(stack)));
    }

    @Override
    public double getCostModifier() {
        return 1.151;
    }

    @Override
    public String getBulletType() {
        return "charge";
    }
}
