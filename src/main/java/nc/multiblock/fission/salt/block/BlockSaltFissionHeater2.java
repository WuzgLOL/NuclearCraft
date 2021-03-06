package nc.multiblock.fission.salt.block;

import nc.NuclearCraft;
import nc.enumm.MetaEnums;
import nc.multiblock.fission.FissionReactor;
import nc.multiblock.fission.block.BlockFissionMetaPart;
import nc.multiblock.fission.salt.tile.TileSaltFissionHeater;
import nc.util.*;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import net.minecraftforge.fluids.FluidStack;

public class BlockSaltFissionHeater2 extends BlockFissionMetaPart<MetaEnums.CoolantHeaterType2> /* implements ISidedProperty< SaltFissionHeaterSetting> */ {
	
	public final static PropertyEnum TYPE = PropertyEnum.create("type", MetaEnums.CoolantHeaterType2.class);
	
	public BlockSaltFissionHeater2() {
		super(MetaEnums.CoolantHeaterType2.class, TYPE);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		// return new BlockStateContainer(this, TYPE, DOWN, UP, NORTH, SOUTH,
		// WEST, EAST);
		return new BlockStateContainer(this, TYPE);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		switch (metadata) {
			case 0:
				return new TileSaltFissionHeater.Tin();
			case 1:
				return new TileSaltFissionHeater.Lead();
			case 2:
				return new TileSaltFissionHeater.Boron();
			case 3:
				return new TileSaltFissionHeater.Lithium();
			case 4:
				return new TileSaltFissionHeater.Magnesium();
			case 5:
				return new TileSaltFissionHeater.Manganese();
			case 6:
				return new TileSaltFissionHeater.Aluminum();
			case 7:
				return new TileSaltFissionHeater.Silver();
			case 8:
				return new TileSaltFissionHeater.Fluorite();
			case 9:
				return new TileSaltFissionHeater.Villiaumite();
			case 10:
				return new TileSaltFissionHeater.Carobbiite();
			case 11:
				return new TileSaltFissionHeater.Arsenic();
			case 12:
				return new TileSaltFissionHeater.LiquidNitrogen();
			case 13:
				return new TileSaltFissionHeater.LiquidHelium();
			case 14:
				return new TileSaltFissionHeater.Enderium();
			case 15:
				return new TileSaltFissionHeater.Cryotheum();
		}
		return new TileSaltFissionHeater.Tin();
	}
	
	/* private static final PropertySidedEnum<SaltFissionHeaterSetting> DOWN = PropertySidedEnum.create("down", SaltFissionHeaterSetting.class, EnumFacing.DOWN); private static final PropertySidedEnum<SaltFissionHeaterSetting> UP = PropertySidedEnum.create("up", SaltFissionHeaterSetting.class, EnumFacing.UP); private static final PropertySidedEnum<SaltFissionHeaterSetting> NORTH = PropertySidedEnum.create("north", SaltFissionHeaterSetting.class, EnumFacing.NORTH); private static final PropertySidedEnum<SaltFissionHeaterSetting> SOUTH = PropertySidedEnum.create("south", SaltFissionHeaterSetting.class, EnumFacing.SOUTH); private static final PropertySidedEnum<SaltFissionHeaterSetting> WEST = PropertySidedEnum.create("west", SaltFissionHeaterSetting.class, EnumFacing.WEST); private static final PropertySidedEnum<SaltFissionHeaterSetting> EAST = PropertySidedEnum.create("east", SaltFissionHeaterSetting.class, EnumFacing.EAST);
	 * 
	 * @Override public SaltFissionHeaterSetting getProperty(IBlockAccess world, BlockPos pos, EnumFacing facing) { TileEntity tile = world.getTileEntity(pos); if (tile instanceof TileSaltFissionHeater) { return ((TileSaltFissionHeater) tile).getHeaterSetting(facing); } return SaltFissionHeaterSetting.DISABLED; } */
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		// return state.withProperty(DOWN, getProperty(world, pos,
		// EnumFacing.DOWN)).withProperty(UP, getProperty(world, pos,
		// EnumFacing.UP)).withProperty(NORTH, getProperty(world, pos,
		// EnumFacing.NORTH)).withProperty(SOUTH, getProperty(world, pos,
		// EnumFacing.SOUTH)).withProperty(WEST, getProperty(world, pos,
		// EnumFacing.WEST)).withProperty(EAST, getProperty(world, pos,
		// EnumFacing.EAST));
		return state;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player == null) {
			return false;
		}
		if (hand != EnumHand.MAIN_HAND || player.isSneaking()) {
			return false;
		}
		
		if (!world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileSaltFissionHeater) {
				TileSaltFissionHeater heater = (TileSaltFissionHeater) tile;
				FissionReactor reactor = heater.getMultiblock();
				if (reactor != null) {
					FluidStack fluidStack = FluidStackHelper.getFluid(player.getHeldItem(hand));
					if (heater.canModifyFilter(0) && heater.getTanks().get(0).isEmpty() && fluidStack != null && !FluidStackHelper.stacksEqual(heater.getFilterTanks().get(0).getFluid(), fluidStack) && heater.getTanks().get(0).canFillFluidType(fluidStack)) {
						player.sendMessage(new TextComponentString(Lang.localise("message.nuclearcraft.filter") + " " + TextFormatting.BOLD + Lang.localise(fluidStack.getUnlocalizedName())));
						FluidStack filter = fluidStack.copy();
						filter.amount = 1000;
						heater.getFilterTanks().get(0).setFluid(filter);
						heater.onFilterChanged(0);
					}
					else {
						player.openGui(NuclearCraft.instance, 203, world, pos.getX(), pos.getY(), pos.getZ());
					}
					return true;
				}
			}
		}
		return rightClickOnPart(world, pos, player, hand, facing, true);
	}
	
	/* @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) { if (hand != EnumHand.MAIN_HAND || player == null) return false;
	 * 
	 * if (ItemMultitool.isMultitool(player.getHeldItem(hand))) { TileEntity tile = world.getTileEntity(pos); if (tile instanceof TileSaltFissionHeater) { TileSaltFissionHeater heater = (TileSaltFissionHeater) tile; EnumFacing side = player.isSneaking() ? facing.getOpposite() : facing; heater.toggleHeaterSetting(side); if (!world.isRemote) player.sendMessage(getToggleMessage(player, heater, side)); return true; } }
	 * 
	 * return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ); }
	 * 
	 * private static TextComponentString getToggleMessage(EntityPlayer player, TileSaltFissionHeater heater, EnumFacing side) { SaltFissionHeaterSetting setting = heater.getHeaterSetting(side); String message = player.isSneaking() ? "nc.block.fluid_toggle_opposite" : "nc.block.fluid_toggle"; TextFormatting color = setting == SaltFissionHeaterSetting.HOT_COOLANT_OUT ? TextFormatting.RED : (setting == SaltFissionHeaterSetting.COOLANT_SPREAD ? TextFormatting.AQUA : (setting == SaltFissionHeaterSetting.DEFAULT ? TextFormatting.WHITE : TextFormatting.GRAY)); return new TextComponentString(Lang.localise(message) + " " + color + Lang.localise("nc.block.salt_heater_fluid_side." + setting.getName())); }
	 * 
	 * @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) { placementSide = null; if (placer != null && placer.isSneaking()) placementSide = facing.getOpposite(); return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand); }
	 * 
	 * @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) { if (placementSide == null) return; BlockPos from = pos.offset(placementSide); TileEntity tile = world.getTileEntity(pos), otherTile = world.getTileEntity(from); if (tile instanceof TileSaltFissionHeater && otherTile instanceof TileSaltFissionHeater) { TileSaltFissionHeater heater = (TileSaltFissionHeater) tile; TileSaltFissionHeater other = (TileSaltFissionHeater) otherTile; heater.setFluidConnections(FluidConnection.cloneArray(other. getFluidConnections())); heater.setHeaterSettings(other.getHeaterSettings().clone()); heater.markDirtyAndNotify(); } } */
}
