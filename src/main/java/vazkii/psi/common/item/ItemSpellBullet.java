package vazkii.psi.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.api.spell.SpellContext;

/** Basic spell bullet: a stackable spell container for the CAD magazine. */
public class ItemSpellBullet extends Item {

    private static final String TAG_SPELL = "spell";

    public ItemSpellBullet() {
        setMaxStackSize(16);
    }

    public static Spell getSpell(ItemStack stack) {
        return stack != null && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey(TAG_SPELL, 10) ? Spell.readFromNBT(
                    stack.getTagCompound()
                        .getCompoundTag(TAG_SPELL))
                    : null;
    }

    public static void setSpell(ItemStack stack, Spell spell) {
        if (stack == null) {
            return;
        }

        if (spell == null) {
            if (stack.hasTagCompound()) {
                stack.getTagCompound()
                    .removeTag(TAG_SPELL);
            }
            return;
        }

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound tag = new NBTTagCompound();
        spell.writeToNBT(tag);
        stack.getTagCompound()
            .setTag(TAG_SPELL, tag);
    }

    public void castSpell(ItemStack stack, EntityPlayer caster) throws Exception {
        castSpell(stack, caster, null);
    }

    public void castSpell(ItemStack stack, EntityPlayer caster, ItemStack colorizer) throws Exception {
        Spell spell = getSpell(stack);

        if (spell == null) {
            return;
        }

        CompiledSpell compiled = new SpellCompiler().compile(spell);
        compiled.safeExecute(new SpellContext().setPlayer(caster).setCompiledSpell(compiled));
    }

    public ArrayList<Entity> castSpell(ItemStack stack, SpellContext context) {
        try {
            Spell spell = getSpell(stack);

            if (spell == null) {
                return new ArrayList<Entity>();
            }

            CompiledSpell compiled = new SpellCompiler().compile(spell);

            context.setCompiledSpell(compiled);
            compiled.safeExecute(context);
        } catch (Exception ignored) {}

        return new ArrayList<Entity>();
    }

    public boolean loopcastSpell(ItemStack stack, SpellContext context) {
        castSpell(stack, context);
        return false;
    }

    public double getCostModifier(ItemStack stack) {
        return 1.0D;
    }

    public String getBulletType() {
        return "basic";
    }

    public boolean isCADOnlyContainer(ItemStack stack) {
        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Spell spell = getSpell(stack);

        return spell != null && spell.name != null && !spell.name.isEmpty() ? EnumChatFormatting.AQUA + spell.name
            : super.getItemStackDisplayName(stack);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {

            tooltip.add(
                EnumChatFormatting.GRAY + "Hold "
                    + EnumChatFormatting.AQUA
                    + "SHIFT"
                    + EnumChatFormatting.GRAY
                    + " for more info");

            return;
        }

        tooltip.add(EnumChatFormatting.AQUA + "Type" + EnumChatFormatting.GRAY + ": " + bulletTypeName());

        tooltip.add(
            EnumChatFormatting.AQUA + "Cost Multiplier"
                + EnumChatFormatting.GRAY
                + ": "
                + (int) (getCostModifier(stack) * 100)
                + "%");
    }

    private String bulletTypeName() {
        String key = "psi.bullet_type_" + getBulletType();
        String translated = net.minecraft.util.StatCollector.translateToLocal(key);

        return key.equals(translated) ? getBulletType() : translated;
    }

    /**
     * Stack-bound spell acceptor replacing the modern capability provider.
     */
    public static final class SpellAcceptor implements ISpellAcceptor {

        private final ItemStack stack;

        public SpellAcceptor(ItemStack stack) {
            this.stack = stack;
        }

        private ItemSpellBullet bulletItem() {
            return (ItemSpellBullet) stack.getItem();
        }

        @Override
        public void setSpell(EntityPlayer player, Spell spell) {
            if (stack.stackSize == 1) {
                ItemSpellBullet.setSpell(stack, spell);
                return;
            }

            stack.stackSize--;

            ItemStack newStack = stack.copy();
            newStack.stackSize = 1;

            ItemSpellBullet.setSpell(newStack, spell);

            if (!player.inventory.addItemStackToInventory(newStack)) {
                player.dropPlayerItemWithRandomChoice(newStack, false);
            }
        }

        @Override
        public Spell getSpell() {
            return ItemSpellBullet.getSpell(stack);
        }

        @Override
        public boolean containsSpell() {
            return getSpell() != null;
        }

        @Override
        public ArrayList<Entity> castSpell(SpellContext context) {
            return bulletItem().castSpell(stack, context);
        }

        @Override
        public boolean loopcastSpell(SpellContext context) {
            return bulletItem().loopcastSpell(stack, context);
        }

        @Override
        public double getCostModifier() {
            return bulletItem().getCostModifier(stack);
        }

        @Override
        public boolean castableFromSocket() {
            return true;
        }

        @Override
        public boolean isCADOnlyContainer() {
            return bulletItem().isCADOnlyContainer(stack);
        }
    }
}
