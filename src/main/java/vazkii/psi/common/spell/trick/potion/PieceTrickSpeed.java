/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/potion/PieceTrickSpeed.java:1
 * GTNH: Potion id via Potion.field, EntityLivingBase.
 */
package vazkii.psi.common.spell.trick.potion;

import net.minecraft.potion.Potion;

import vazkii.psi.api.spell.Spell;

public class PieceTrickSpeed extends PieceTrickPotionBase {

    public PieceTrickSpeed(Spell spell) {
        super(spell);
    }

    @Override
    public Potion getPotion() {
        return Potion.moveSpeed;
    }

}
