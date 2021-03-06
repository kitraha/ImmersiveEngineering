package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;

public class ItemBlockMetalDevices extends ItemBlockIEBase
{
	public ItemBlockMetalDevices(Block b)
	{
		super(b);
	}


	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advInfo)
	{
		if(((BlockIEBase)field_150939_a).subNames[stack.getItemDamage()].startsWith("capacitor"))
		{
			list.add(StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.energyStored", ItemNBTHelper.getInt(stack, "energyStorage")));
			if(ItemNBTHelper.hasKey(stack, "sideConfig") && GuiScreen.isShiftKeyDown())
			{
				int[] s = ItemNBTHelper.getIntArray(stack, "sideConfig");
				String sq = "\u25FC";
				String top = (s[1]==0?EnumChatFormatting.BLUE: s[1]==1?EnumChatFormatting.GOLD: EnumChatFormatting.DARK_GRAY) + sq;
				String bot = (s[0]==0?EnumChatFormatting.BLUE: s[0]==1?EnumChatFormatting.GOLD: EnumChatFormatting.DARK_GRAY) + sq;
				String front = (s[3]==0?EnumChatFormatting.BLUE: s[3]==1?EnumChatFormatting.GOLD: EnumChatFormatting.DARK_GRAY) + sq;
				String back = (s[2]==0?EnumChatFormatting.BLUE: s[2]==1?EnumChatFormatting.GOLD: EnumChatFormatting.DARK_GRAY) + sq;
				String left = (s[4]==0?EnumChatFormatting.BLUE: s[4]==1?EnumChatFormatting.GOLD: EnumChatFormatting.DARK_GRAY) + sq;
				String right = (s[5]==0?EnumChatFormatting.BLUE: s[5]==1?EnumChatFormatting.GOLD: EnumChatFormatting.DARK_GRAY) + sq;
				
				list.add( " "+top+" " );
				list.add( left+front+right );
				list.add( " "+bot+back );
			}
		}
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		if(meta==BlockMetalDevices.META_transformer)
		{
			int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;
			if(side==f && world.getTileEntity(x+(side==4?1: side==5?-1: 0), y, z+(side==2?1: side==3?-1: 0)) instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)world.getTileEntity(x+(side==4?1: side==5?-1: 0), y, z+(side==2?1: side==3?-1: 0))).type>0 )
				;
			else if(!world.isAirBlock(x,y+1,z))
				return false;
		}
		if(meta==BlockMetalDevices.META_relayHV&& world.isAirBlock(x,y+1,z))
			return false;
		boolean ret = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta);
		if(!ret)
			return ret;
		int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;
		if(world.getTileEntity(x, y, z) instanceof TileEntityRelayHV)
		{
			if(ret && !world.isAirBlock(x,y+1,z))
				((TileEntityConnectorLV)world.getTileEntity(x, y, z)).facing = ForgeDirection.UP.ordinal();
		}
		else if(world.getTileEntity(x, y, z) instanceof TileEntityCapacitorLV)
		{
			if(ItemNBTHelper.hasKey(stack, "energyStorage"))
				((TileEntityCapacitorLV)world.getTileEntity(x, y, z)).energyStorage.setEnergyStored(ItemNBTHelper.getInt(stack, "energyStorage"));
			if(ItemNBTHelper.hasKey(stack, "sideConfig"))
				((TileEntityCapacitorLV)world.getTileEntity(x, y, z)).sideConfig = ItemNBTHelper.getIntArray(stack, "sideConfig");
			else
				((TileEntityCapacitorLV)world.getTileEntity(x, y, z)).sideConfig[f]=1;
		}
		else if(world.getTileEntity(x, y, z) instanceof TileEntityConnectorLV)
			((TileEntityConnectorLV)world.getTileEntity(x, y, z)).facing = ForgeDirection.getOrientation(side).getOpposite().ordinal();
		else if(world.getTileEntity(x, y, z) instanceof TileEntityTransformer)
		{
			((TileEntityTransformer)world.getTileEntity(x,y,z)).facing=f;
			if(side==f && world.getTileEntity(x+(side==4?1: side==5?-1: 0), y, z+(side==2?1: side==3?-1: 0)) instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)world.getTileEntity(x+(side==4?1: side==5?-1: 0), y, z+(side==2?1: side==3?-1: 0))).type>0 )
				((TileEntityTransformer)world.getTileEntity(x,y,z)).postAttached=side;
			else
			{
				((TileEntityTransformer)world.getTileEntity(x,y,z)).dummy = true;
				world.setBlock(x,y+1,z, field_150939_a,meta, 0x3);
				if(world.getTileEntity(x,y+1,z) instanceof TileEntityTransformer)
				{
					((TileEntityTransformer)world.getTileEntity(x,y+1,z)).facing = f;
				}
			}
		}
		else if(world.getTileEntity(x, y, z) instanceof TileEntityDynamo)
			((TileEntityDynamo)world.getTileEntity(x,y,z)).facing=f;
		else if(world.getTileEntity(x, y, z) instanceof TileEntityConveyorBelt)
		{
			TileEntityConveyorBelt tile = (TileEntityConveyorBelt)world.getTileEntity(x,y,z);
			
			ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[f].getOpposite();
			if(world.getTileEntity(x+fd.offsetX, y+1, z+fd.offsetZ) instanceof TileEntityConveyorBelt)
			{
				TileEntityConveyorBelt con = (TileEntityConveyorBelt)world.getTileEntity(x+fd.offsetX, y+1, z+fd.offsetZ);
				if(ForgeDirection.VALID_DIRECTIONS[con.facing].equals(fd))
				{
					tile.transportDown = true;
					f = ForgeDirection.OPPOSITES[f];
				}
				else
					tile.transportUp = true;
			}
			else if(world.getTileEntity(x+fd.offsetX, y-1, z+fd.offsetZ) instanceof TileEntityConveyorBelt)
			{
				TileEntityConveyorBelt con = (TileEntityConveyorBelt)world.getTileEntity(x+fd.offsetX, y-1, z+fd.offsetZ);
				if(ForgeDirection.VALID_DIRECTIONS[con.facing].getOpposite().equals(fd))
					con.transportDown = true;
			}
			else if(world.getTileEntity(x-fd.offsetX, y-1, z-fd.offsetZ) instanceof TileEntityConveyorBelt)
			{
				TileEntityConveyorBelt con = (TileEntityConveyorBelt)world.getTileEntity(x-fd.offsetX, y-1, z-fd.offsetZ);
				if(con.facing == f)
					con.transportUp = true;
			}
			else if(world.getTileEntity(x-fd.offsetX, y+1, z-fd.offsetZ) instanceof TileEntityConveyorBelt)
			{
				TileEntityConveyorBelt con = (TileEntityConveyorBelt)world.getTileEntity(x-fd.offsetX, y+1, z-fd.offsetZ);
				if(con.facing == f)
					tile.transportDown = true;
			}
			tile.facing=f;
		}

		return ret;
	}
}