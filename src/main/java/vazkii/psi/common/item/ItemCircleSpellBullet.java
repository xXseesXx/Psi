package vazkii.psi.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import vazkii.psi.common.entity.EntitySpellCircle;

public class ItemCircleSpellBullet extends ItemSpellBullet {

    @Override
    public void castSpell(ItemStack stack, EntityPlayer caster) {
        MovingObjectPosition hit = caster.rayTrace(32, 1F);
        double x = hit == null ? caster.posX : hit.hitVec.xCoord;
        double y = hit == null ? caster.posY : hit.hitVec.yCoord;
        double z = hit == null ? caster.posZ : hit.hitVec.zCoord;
        EntitySpellCircle circle = new EntitySpellCircle(caster.worldObj, caster, getSpell(stack));
        circle.setPosition(x, y, z);
        caster.worldObj.spawnEntityInWorld(circle);
    }

    @Override
    public double getCostModifier() {
        return EntitySpellCircle.CAST_TIMES * .75;
    }

    @Override
    public String getBulletType() {
        return "circle";
    }

    @Override
    public boolean isCADOnlyContainer() {
        return true;
    }
}
