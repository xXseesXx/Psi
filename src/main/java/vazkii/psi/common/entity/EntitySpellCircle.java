package vazkii.psi.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.api.spell.SpellContext;

/** Invisible focal point which executes its stored spell 20 times, four times per second. */
public class EntitySpellCircle extends Entity {

    public static final int CAST_TIMES = 20;
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
        if (!worldObj.isRemote && age > 5 && age % 5 == 0 && casts < CAST_TIMES && caster != null && compiled != null) {
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
        if (age > (CAST_TIMES + 2) * 5) setDead();
    }

    @Override
    protected void entityInit() {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        age = tag.getInteger("age");
        casts = tag.getInteger("casts");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("age", age);
        tag.setInteger("casts", casts);
    }
}
