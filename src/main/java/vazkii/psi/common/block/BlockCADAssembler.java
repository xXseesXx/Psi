package vazkii.psi.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import vazkii.psi.common.Psi;
import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.core.handler.GuiHandler;

public class BlockCADAssembler extends BlockContainer {
    @SideOnly(Side.CLIENT) private IIcon top, side, bottom, bottomTop;
    private boolean renderingBase;
    public BlockCADAssembler() { super(Material.iron); setHardness(5F); setResistance(10F); setBlockName("psi.cad_assembler"); setBlockTextureName("psi:cad_assembler_side"); }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileCADAssembler(); }
    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, net.minecraft.item.ItemStack stack) {
        int facing = MathHelper.floor_double(placer.rotationYaw * 4F / 360F + .5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
    }
    @Override public int getRenderType() { return Psi.proxy.getMachineRenderType(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int sideHit,float hitX,float hitY,float hitZ) { if(!world.isRemote) player.openGui(Psi.instance, GuiHandler.GUI_CAD_ASSEMBLER, world,x,y,z); return true; }
    public void setRenderingBase(boolean renderingBase) { this.renderingBase = renderingBase; }
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister r) { top=r.registerIcon("psi:cad_assembler_top"); side=r.registerIcon("psi:cad_assembler_side"); bottom=r.registerIcon("psi:cad_assembler_bottom"); bottomTop=r.registerIcon("psi:cad_assembler_bottom_top"); }
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int sideId,int meta) { return sideId==1?(renderingBase?bottomTop:top):(sideId==0?bottom:side); }
}
