package vazkii.psi.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.Psi;
import vazkii.psi.common.core.handler.GuiHandler;
import vazkii.psi.common.entity.EntitySpellProjectile;

/**
 * CAD (Computer-Aided Design) Assembly - stores and casts spells as projectiles.
 *
 * Right-click to shoot spell projectile.
 * Stores spell in NBT.
 */
public class ItemCAD extends Item {

    private static final String TAG_SPELL = "spell";

    public ItemCAD() {
        setMaxStackSize(1);
        setMaxDamage(0); // No durability for now
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        // Shift+Right-Click: Open spell programmer GUI
        if (player.isSneaking()) {
            if (world.isRemote) {
                // Open GUI only on client side
                player.openGui(Psi.instance, GuiHandler.GUI_SPELL_PROGRAMMER, world, 0, 0, 0);
            }
            return stack;
        }

        // Don't execute spell casting on client
        if (world.isRemote) {
            return stack;
        }

        // Normal Right-Click: Cast spell
        Spell spell = getSpell(stack);

        if (spell == null) {
            player.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.RED + "[Psi] "
                        + EnumChatFormatting.RESET
                        + "No spell programmed into this CAD"));
            return stack;
        }

        try {
            // Create and shoot projectile
            EntitySpellProjectile projectile = new EntitySpellProjectile(world, player, spell);

            // Set velocity based on player's look direction
            double velocity = 1.5;
            double mx = -Math.sin(Math.toRadians(player.rotationYaw)) * Math.cos(Math.toRadians(player.rotationPitch))
                * velocity;
            double my = -Math.sin(Math.toRadians(player.rotationPitch)) * velocity;
            double mz = Math.cos(Math.toRadians(player.rotationYaw)) * Math.cos(Math.toRadians(player.rotationPitch))
                * velocity;

            projectile.setThrowableHeading(mx, my, mz, 1.5F, 0.0F);

            // Spawn projectile
            world.spawnEntityInWorld(projectile);

            // Success feedback
            player.addChatMessage(
                new ChatComponentText(EnumChatFormatting.GREEN + "[Psi] " + EnumChatFormatting.RESET + "Spell cast!"));

        } catch (Exception e) {
            player.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.RED + "[Psi Error] "
                        + e.getClass()
                            .getSimpleName()
                        + ": "
                        + e.getMessage()));
            e.printStackTrace();
        }

        return stack;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        Spell spell = getSpell(stack);

        if (spell != null) {
            // Show spell name
            tooltip.add(EnumChatFormatting.AQUA + "Spell: " + EnumChatFormatting.RESET + spell.name);

            // TODO Phase 9+: Add more detailed spell info (cost, effects, etc.)
        } else {
            tooltip.add(EnumChatFormatting.GRAY + "No spell programmed");
            tooltip.add(EnumChatFormatting.DARK_GRAY + "Use a spell programmer to set");
        }
    }

    /**
     * Get the spell stored in this CAD item.
     */
    public static Spell getSpell(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) {
            return null;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        if (!nbt.hasKey(TAG_SPELL)) {
            return null;
        }

        try {
            return Spell.readFromNBT(nbt.getCompoundTag(TAG_SPELL));
        } catch (Exception e) {
            System.err.println("[Psi] Failed to read spell from CAD: " + e.getMessage());
            return null;
        }
    }

    /**
     * Set the spell stored in this CAD item.
     */
    public static void setSpell(ItemStack stack, Spell spell) {
        if (stack == null) {
            return;
        }

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound nbt = stack.getTagCompound();
        NBTTagCompound spellNbt = new NBTTagCompound();
        spell.writeToNBT(spellNbt);
        nbt.setTag(TAG_SPELL, spellNbt);
    }
}
