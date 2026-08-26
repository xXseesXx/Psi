package vazkii.psi.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import vazkii.psi.common.entity.EntitySpellMine;

public class ItemMineSpellBullet extends ItemSpellBullet {

    @Override
    public void castSpell(ItemStack stack, EntityPlayer caster) {
        castSpell(stack, caster, null);
    }

    @Override
    public void castSpell(ItemStack stack, EntityPlayer caster, ItemStack colorizer) {
        caster.worldObj
            .spawnEntityInWorld(new EntitySpellMine(caster.worldObj, caster, getSpell(stack)).setColorizer(colorizer));
    }

    @Override
    public double getCostModifier() {
        return 1.151;
    }

    @Override
    public String getBulletType() {
        return "mine";
    }
}
