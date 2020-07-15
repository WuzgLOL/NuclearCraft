package nc.multiblock.fission.tile.port;

import static nc.config.NCConfig.smart_processor_input;
import static nc.util.PosHelper.DEFAULT_NON;

import java.util.*;

import javax.annotation.*;

import com.google.common.collect.Lists;

import nc.Global;
import nc.multiblock.fission.tile.port.internal.PortItemHandler;
import nc.recipe.ProcessorRecipeHandler;
import nc.tile.internal.inventory.*;
import nc.tile.inventory.*;
import nc.util.NBTHelper;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.*;

public abstract class TileFissionItemPort<PORT extends TileFissionItemPort<PORT, TARGET> & ITileFilteredInventory, TARGET extends IFissionPortTarget<PORT, TARGET> & ITileFilteredInventory> extends TileFissionPort<PORT, TARGET> implements ITileFilteredInventory {
	
	private final @Nonnull String inventoryName;
	
	protected final @Nonnull NonNullList<ItemStack> inventoryStacks = NonNullList.withSize(2, ItemStack.EMPTY);
	protected final @Nonnull NonNullList<ItemStack> filterStacks = NonNullList.withSize(2, ItemStack.EMPTY);
	
	protected @Nonnull InventoryConnection[] inventoryConnections = ITileInventory.inventoryConnectionAll(Lists.newArrayList(ItemSorption.IN, ItemSorption.OUT));
	
	public int inventoryStackLimit = 64;
	
	protected final ProcessorRecipeHandler recipeHandler;
	
	public TileFissionItemPort(Class<PORT> portClass, String type, ProcessorRecipeHandler recipeHandler) {
		super(portClass);
		inventoryName = Global.MOD_ID + ".container.fission_" + type + "_port";
		this.recipeHandler = recipeHandler;
	}
	
	@Override
	public void setInventoryStackLimit(int stackLimit) {
		inventoryStackLimit = stackLimit;
	}
	
	@Override
	public int getTankCapacityPerConnection() {
		return 0;
	}
	
	@Override
	public int getTankBaseCapacity() {
		return 1;
	}
	
	@Override
	public void setTankCapacity(int capacity) {}
	
	@Override
	public boolean canModifyFilter(int slot) {
		return getMultiblock() != null ? !getMultiblock().isAssembled() : true;
	}
	
	@Override
	public void onFilterChanged(int slot) {
		/* if (!canModifyFilter(slot)) { getMultiblock().getLogic().refreshPorts(); } */
		markDirty();
	}
	
	@Override
	public int getFilterID() {
		return getFilterStacks().get(0).isEmpty() ? 0 : RecipeItemHelper.pack(getFilterStacks().get(0));
	}
	
	// Inventory
	
	@Override
	public String getName() {
		return inventoryName;
	}
	
	@Override
	public @Nonnull NonNullList<ItemStack> getInventoryStacks() {
		return !DEFAULT_NON.equals(masterPortPos) ? masterPort.getInventoryStacks() : inventoryStacks;
	}
	
	@Override
	public @Nonnull NonNullList<ItemStack> getInventoryStacksInternal() {
		return inventoryStacks;
	}
	
	@Override
	public @Nonnull NonNullList<ItemStack> getFilterStacks() {
		return !DEFAULT_NON.equals(masterPortPos) ? masterPort.getFilterStacks() : filterStacks;
	}
	
	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack stack = ITileFilteredInventory.super.decrStackSize(slot, amount);
		if (!world.isRemote) {
			if (slot < recipeHandler.getItemInputSize()) {
				refreshTargetsFlag = true;
			}
			else if (slot < recipeHandler.getItemInputSize() + recipeHandler.getItemOutputSize()) {
				refreshTargetsFlag = true;
			}
		}
		return stack;
	}
	
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		ITileFilteredInventory.super.setInventorySlotContents(slot, stack);
		if (!world.isRemote) {
			if (slot < recipeHandler.getItemInputSize()) {
				refreshTargetsFlag = true;
			}
			else if (slot < recipeHandler.getItemInputSize() + recipeHandler.getItemOutputSize()) {
				refreshTargetsFlag = true;
			}
		}
	}
	
	/* @Override public void markDirty() { super.markDirty(); } */
	
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (stack.isEmpty() || slot >= recipeHandler.getItemInputSize()) {
			return false;
		}
		ItemStack filter = getFilterStacks().get(slot);
		if (!filter.isEmpty() && !stack.isItemEqual(filter)) {
			return false;
		}
		return isItemValidForSlotInternal(slot, stack);
	}
	
	@Override
	public boolean isItemValidForSlotInternal(int slot, ItemStack stack) {
		if (stack.isEmpty() || slot >= recipeHandler.getItemInputSize()) {
			return false;
		}
		return smart_processor_input ? recipeHandler.isValidItemInput(stack, getInventoryStacks().get(slot), inputItemStacksExcludingSlot(slot)) : recipeHandler.isValidItemInput(stack);
	}
	
	public List<ItemStack> inputItemStacksExcludingSlot(int slot) {
		List<ItemStack> inputItemsExcludingSlot = new ArrayList<>(getInventoryStacks().subList(0, recipeHandler.getItemInputSize()));
		inputItemsExcludingSlot.remove(slot);
		return inputItemsExcludingSlot;
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side) {
		return ITileFilteredInventory.super.canInsertItem(slot, stack, side) && isItemValidForSlot(slot, stack);
	}
	
	@Override
	public void clearAllSlots() {
		ITileFilteredInventory.super.clearAllSlots();
		refreshTargetsFlag = true;
	}
	
	@Override
	public @Nonnull InventoryConnection[] getInventoryConnections() {
		return inventoryConnections;
	}
	
	@Override
	public void setInventoryConnections(@Nonnull InventoryConnection[] connections) {
		inventoryConnections = connections;
	}
	
	@Override
	public int getInventoryStackLimit() {
		return !DEFAULT_NON.equals(masterPortPos) ? masterPort.getInventoryStackLimit() : inventoryStackLimit;
	}
	
	@Override
	public ItemOutputSetting getItemOutputSetting(int slot) {
		return ItemOutputSetting.DEFAULT;
	}
	
	@Override
	public void setItemOutputSetting(int slot, ItemOutputSetting setting) {}
	
	// NBT
	
	@Override
	public NBTTagCompound writeAll(NBTTagCompound nbt) {
		super.writeAll(nbt);
		writeInventory(nbt);
		writeInventoryConnections(nbt);
		
		nbt.setInteger("inventoryStackLimit", inventoryStackLimit);
		return nbt;
	}
	
	@Override
	public void readAll(NBTTagCompound nbt) {
		super.readAll(nbt);
		readInventory(nbt);
		readInventoryConnections(nbt);
		
		inventoryStackLimit = nbt.getInteger("inventoryStackLimit");
	}
	
	@Override
	public NBTTagCompound writeInventory(NBTTagCompound nbt) {
		int[] counts = new int[inventoryStacks.size()];
		for (int i = 0; i < inventoryStacks.size(); i++) {
			nbt.setInteger("inventoryStackSize" + i, counts[i] = inventoryStacks.get(i).getCount());
			if (!inventoryStacks.get(i).isEmpty()) {
				inventoryStacks.get(i).setCount(1);
			}
		}
		
		NBTHelper.saveAllItems(nbt, inventoryStacks, filterStacks);
		
		for (int i = 0; i < inventoryStacks.size(); i++) {
			if (!inventoryStacks.get(i).isEmpty()) {
				inventoryStacks.get(i).setCount(counts[i]);
			}
		}
		
		return nbt;
	}
	
	@Override
	public void readInventory(NBTTagCompound nbt) {
		NBTHelper.loadAllItems(nbt, inventoryStacks, filterStacks);
		
		for (int i = 0; i < inventoryStacks.size(); i++) {
			if (!inventoryStacks.get(i).isEmpty()) {
				inventoryStacks.get(i).setCount(nbt.getInteger("inventoryStackSize" + i));
			}
		}
	}
	
	// Capability
	
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return !getInventoryStacks().isEmpty() && hasInventorySideCapability(side);
		}
		return super.hasCapability(capability, side);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (!getInventoryStacks().isEmpty() && hasInventorySideCapability(side)) {
				return (T) getItemHandler(side);
			}
			return null;
		}
		return super.getCapability(capability, side);
	}
	
	@Override
	public IItemHandler getItemHandler(@Nullable EnumFacing side) {
		ITileInventory tile = !DEFAULT_NON.equals(masterPortPos) ? masterPort : this;
		return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new PortItemHandler(tile, side));
	}
}
