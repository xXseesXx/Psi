package vazkii.psi.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.Psi;

/** Invisible focal point which executes its stored spell 20 times, four times per second. */
public class EntitySpellCircle extends Entity {

    public static final int CAST_TIMES = 20;
    public static final int CAST_DELAY = 5;
    private static final String TAG_COLORIZER = "colorizer";
    private static final int DW_COLORIZER = 20;
    private EntityPlayer caster;
    private Spell spell;
    private CompiledSpell compiled;
    private int age, casts;

    public EntitySpellCircle(World world) {
        super(world);
        setSize(1F, .1F);
    }

    public EntitySpellCircle(World world, EntityPlayer caster, Spell spell) {
        this(world);
        this.caster = caster;
        this.spell = spell;
        try {
            compiled = new SpellCompiler().compile(spell);
        } catch (Exception ignored) {}
    }

    @Override
    public void onUpdate() {
        ++age;
        if (worldObj.isRemote) spawnCircleParticles();
        if (!worldObj.isRemote && age > CAST_DELAY
            && age % CAST_DELAY == 0
            && casts < CAST_TIMES
            && caster != null
            && compiled != null) {
            try {
                compiled.execute(
                    new SpellContext().setPlayer(caster)
                        .setFocalPoint(this)
                        .setSpell(spell)
                        .setLoopcastIndex(casts++));
            } catch (Exception ignored) {
                setDead();
            }
        }
        if (age > (CAST_TIMES + 2) * CAST_DELAY) setDead();
    }

    private void spawnCircleParticles() {
        int color = getSpellColor();
        float r = ((color >> 16) & 255) / 255F;
        float g = ((color >> 8) & 255) / 255F;
        float b = (color & 255) / 255F;
        for (int i = 0; i < 5; i++) {
            double angle = rand.nextDouble() * Math.PI * 2D;
            double radius = (rand.nextDouble() - 0.5D);
            float rise = 0.15F + rand.nextFloat() * 0.03F;
            Psi.proxy.sparkleFX(
                posX + Math.cos(angle) * radius,
                posY,
                posZ + Math.sin(angle) * radius,
                r,
                g,
                b,
                0F,
                rise,
                0F,
                0.25F,
                15);
        }
    }

    public int getAge() {
        return age;
    }

    public EntitySpellCircle setColorizer(ItemStack colorizer) {
        ItemStack stored = colorizer == null ? new ItemStack(Blocks.air) : colorizer.copy();
        stored.stackSize = 1;
        dataWatcher.updateObject(DW_COLORIZER, stored);
        return this;
    }

    public ItemStack getColorizer() {
        ItemStack stack = dataWatcher.getWatchableObjectItemStack(DW_COLORIZER);
        return stack != null && stack.getItem() != null && stack.getItem() != Item.getItemFromBlock(Blocks.air) ? stack
            : null;
    }

    public int getSpellColor() {
        return Psi.proxy.getColorForColorizer(getColorizer());
    }

    @Override
    protected void entityInit() {
        dataWatcher.addObjectByDataType(DW_COLORIZER, 5);
        dataWatcher.updateObject(DW_COLORIZER, new ItemStack(Blocks.air));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        age = tag.getInteger("age");
        casts = tag.getInteger("casts");
        if (tag.hasKey(TAG_COLORIZER)) setColorizer(ItemStack.loadItemStackFromNBT(tag.getCompoundTag(TAG_COLORIZER)));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("age", age);
        tag.setInteger("casts", casts);
        ItemStack colorizer = getColorizer();
        if (colorizer != null) tag.setTag(TAG_COLORIZER, colorizer.writeToNBT(new NBTTagCompound()));
    }
}
