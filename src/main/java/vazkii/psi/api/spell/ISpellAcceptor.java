package vazkii.psi.api.spell;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import vazkii.psi.common.item.ItemSpellBullet;

/**
 * An item that can hold and cast a spell.
 *
 * The 1.7.10 backport uses the item interface directly instead of the
 * capability system used by newer Minecraft versions.
 *
 * Some stack-backed containers, such as ItemSpellBullet, expose their
 * acceptor through a stack-bound adapter rather than implementing this
 * interface directly.
 */
public interface ISpellAcceptor {

    /**
     * Returns whether this stack has a spell acceptor.
     */
    static boolean isAcceptor(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }

        if (stack.getItem() instanceof ISpellAcceptor) {
            return true;
        }

        return stack.getItem() instanceof ItemSpellBullet;
    }

    /**
     * Returns whether this stack can be used as a spell container from
     * a socket/magazine.
     */
    static boolean isContainer(ItemStack stack) {
        ISpellAcceptor acceptor = getAcceptor(stack);
        return acceptor != null && acceptor.castableFromSocket();
    }

    /**
     * Returns whether this stack currently contains a spell.
     */
    static boolean hasSpell(ItemStack stack) {
        ISpellAcceptor acceptor = getAcceptor(stack);
        return acceptor != null && acceptor.containsSpell();
    }

    /**
     * Gets the stack-bound spell acceptor for this stack.
     */
    static ISpellAcceptor getAcceptor(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return null;
        }

        if (stack.getItem() instanceof ISpellAcceptor) {
            return (ISpellAcceptor) stack.getItem();
        }

        if (stack.getItem() instanceof ItemSpellBullet) {
            return new ItemSpellBullet.SpellAcceptor(stack);
        }

        return null;
    }

    /**
     * Gets the spell acceptor for a stack, throwing when none exists.
     */
    static ISpellAcceptor acceptor(ItemStack stack) {
        ISpellAcceptor acceptor = getAcceptor(stack);

        if (acceptor == null) {
            throw new NullPointerException("Stack does not have a spell acceptor");
        }

        return acceptor;
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

    default ArrayList<Entity> castSpell(SpellContext context) {
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
