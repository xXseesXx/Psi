package vazkii.psi.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import vazkii.psi.common.entity.EntitySpellProjectile;

public class ItemProjectileSpellBullet extends ItemSpellBullet {

    @Override
    public void castSpell(ItemStack stack, EntityPlayer caster) {
        World world = caster.worldObj;
        world.spawnEntityInWorld(new EntitySpellProjectile(world, caster, getSpell(stack)));
    }

    @Override
    public double getCostModifier() {
        return 1.02;
    }

    @Override
    public String getBulletType() {
        return "projectile";
    }
}
