/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/potion/PieceTrickStrength.java:1
 * GTNH: Potion id via Potion.field, EntityLivingBase.
 */
package vazkii.psi.common.spell.trick.potion;

import net.minecraft.potion.Potion;

import vazkii.psi.api.spell.*;

public class PieceTrickStrength extends PieceTrickPotionBase {

    public PieceTrickStrength(Spell spell) {
        super(spell);
        setStatLabel(
            EnumSpellStat.POTENCY,
            new StatLabel(SpellParam.GENERIC_NAME_POWER).cube()
                .mul(SpellParam.GENERIC_NAME_TIME)
                .mul(5));
    }

    @Override
    public Potion getPotion() {
        return Potion.damageBoost;
    }

    @Override
    public int getPotency(int power, int time) throws SpellCompilationException {
        return (int) multiplySafe(power, power, power, time, 5);
    }

}
