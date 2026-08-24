/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

import java.util.Locale;

/**
 * An Enum defining all spell stats.
 * 
 * NOTE: CAD stat integration removed for barebones 1.7.10 backport.
 * Full CAD system will be added in later phases.
 */
public enum EnumSpellStat {

    COMPLEXITY,
    POTENCY,
    COST,
    PROJECTION,
    BANDWIDTH;

    public String getName() {
        return "psi.spellstat." + name().toLowerCase(Locale.ROOT);
    }

    public String getDesc() {
        return getName() + ".desc";
    }

}
