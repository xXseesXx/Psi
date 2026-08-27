package vazkii.psi.api.spell;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * An item that can hold and cast a spell.
 *
 * The 1.7.10 backport uses the item interface directly instead of the
 * capability system used by newer Minecraft versions.
 */
public interface ISpellAcceptor {

    static boolean isAcceptor(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ISpellAcceptor;
    }

    static boolean isContainer(ItemStack stack) {
        return isAcceptor(stack)
            && ((ISpellAcceptor) stack.getItem()).castableFromSocket();
    }

    static boolean hasSpell(ItemStack stack) {
        return isAcceptor(stack)
            && ((ISpellAcceptor) stack.getItem()).containsSpell();
    }

    static ISpellAcceptor acceptor(ItemStack stack) {
        if (!isAcceptor(stack)) throw new NullPointerException();
        return (ISpellAcceptor) stack.getItem();
    }

    void setSpell(EntityPlayer player, Spell spell);

    default boolean castableFromSocket() {
        return false;
    }

    default Spell getSpell() {
        return null;
    }

    default boolean containsSpell() {
        return false;
    }

    default ArrayList castSpell(SpellContext context) {
        return null;
    }

    default boolean loopcastSpell(SpellContext context) {
        castSpell(context);
        return false;
    }

    default double getCostModifier() {
        return 1.0D;
    }

    default boolean isCADOnlyContainer() {
        return false;
    }

    default boolean requiresSneakForSpellSet() {
        return false;
    }
}
