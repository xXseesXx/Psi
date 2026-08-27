package vazkii.psi.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import vazkii.psi.common.entity.EntitySpellProjectile;

public class ItemProjectileSpellBullet extends ItemSpellBullet {

    @Override
    public void castSpell(ItemStack stack, EntityPlayer caster) {
        castSpell(stack, caster, null);
    }

    @Override
    public void castSpell(ItemStack stack, EntityPlayer caster, ItemStack colorizer) {
        World world = caster.worldObj;
        world.spawnEntityInWorld(new EntitySpellProjectile(world, caster, getSpell(stack)).setColorizer(colorizer));
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 1.02;
    }

    @Override
    public String getBulletType() {
        return "projectile";
    }
}
