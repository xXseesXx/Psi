/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell.param;

import vazkii.psi.api.spell.SpellParam;

/**
 * Base class for parameters that accept a specific type and may require constants.
 */
public abstract class ParamSpecific<T> extends SpellParam<T> {

    final boolean constant;

    public ParamSpecific(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable);
        this.constant = constant;
    }

    @Override
    public boolean requiresConstant() {
        return constant;
    }

}
