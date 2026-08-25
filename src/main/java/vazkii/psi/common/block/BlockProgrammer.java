package vazkii.psi.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import vazkii.psi.common.Psi;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.core.handler.GuiHandler;
import vazkii.psi.common.item.ItemSpellBullet;

public class BlockProgrammer extends BlockContainer {
    @SideOnly(Side.CLIENT) private IIcon top, inactiveTop, side, inactiveSide, bottom, baseTop, baseSide;
    private boolean renderingBase;
    public BlockProgrammer() { super(Material.iron); setHardness(5F); setResistance(10F); setBlockName("psi.programmer"); setBlockTextureName("psi:programmer_side"); }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileProgrammer(); }
    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, net.minecraft.item.ItemStack stack) {
        int facing = MathHelper.floor_double(placer.rotationYaw * 4F / 360F + .5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
    }
    @Override public int getRenderType() { return Psi.proxy.getMachineRenderType(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int sideHit, float hitX, float hitY, float hitZ) {
        TileProgrammer programmer=(TileProgrammer)world.getTileEntity(x,y,z); if(programmer==null)return true;
        if(player.getHeldItem()!=null && player.getHeldItem().getItem() instanceof ItemSpellBullet && programmer.canCompile()) { if(!world.isRemote) ItemSpellBullet.setSpell(player.getHeldItem(),programmer.spell); return true; }
        // Programmer is a client-only screen in this backport; opening it server-side produces no container/open packet.
        if(world.isRemote) player.openGui(Psi.instance,GuiHandler.GUI_SPELL_PROGRAMMER,world,x,y,z);
        return true;
    }
    @Override public boolean hasComparatorInputOverride() { return true; }
    @Override public int getComparatorInputOverride(World world, int x, int y, int z, int sideHit) { TileEntity te=world.getTileEntity(x,y,z); if(te instanceof TileProgrammer) { TileProgrammer p=(TileProgrammer)te; return p.canCompile()?2:(p.isEnabled()?1:0); } return 0; }
    public void setRenderingBase(boolean renderingBase) { this.renderingBase = renderingBase; }
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister r) { top=r.registerIcon("psi:programmer_top"); inactiveTop=r.registerIcon("psi:programmer_top_inactive"); side=r.registerIcon("psi:programmer_side"); inactiveSide=r.registerIcon("psi:programmer_side_inactive"); bottom=r.registerIcon("psi:programmer_bottom"); baseTop=r.registerIcon("psi:cad_assembler_bottom_top"); baseSide=r.registerIcon("psi:cad_assembler_side"); }
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int sideId, int meta) { boolean active=(meta&8)!=0; return sideId==1?(renderingBase?baseTop:(active?top:inactiveTop)):(sideId==0?bottom:(renderingBase?baseSide:(active?side:inactiveSide))); }
}
