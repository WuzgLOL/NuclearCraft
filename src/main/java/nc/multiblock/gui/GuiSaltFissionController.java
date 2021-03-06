package nc.multiblock.gui;

import java.util.*;

import nc.Global;
import nc.multiblock.fission.FissionReactor;
import nc.multiblock.fission.salt.MoltenSaltFissionLogic;
import nc.multiblock.gui.element.MultiblockButton;
import nc.multiblock.network.ClearAllMaterialPacket;
import nc.network.PacketHandler;
import nc.util.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;

public class GuiSaltFissionController extends GuiLogicMultiblockController<FissionReactor, MoltenSaltFissionLogic> {
	
	protected final ResourceLocation gui_texture;
	
	public GuiSaltFissionController(FissionReactor multiblock, BlockPos controllerPos, Container container) {
		super(multiblock, controllerPos, container);
		gui_texture = new ResourceLocation(Global.MOD_ID + ":textures/gui/container/" + "salt_fission_controller" + ".png");
		xSize = 176;
		ySize = 114;
	}
	
	@Override
	protected ResourceLocation getGuiTexture() {
		return gui_texture;
	}
	
	@Override
	public void renderTooltips(int mouseX, int mouseY) {
		if (NCUtil.isModifierKeyDown()) {
			drawTooltip(clearAllInfo(), mouseX, mouseY, 153, 71, 18, 18);
		}
		
		drawHeatTooltip(mouseX, mouseY, 6, 103, 164, 6);
	}
	
	// TODO
	public List<String> heatInfo() {
		List<String> info = new ArrayList<>();
		info.add(TextFormatting.YELLOW + Lang.localise("gui.nc.container.fission_controller.heat_stored") + " " + TextFormatting.WHITE + UnitHelper.prefix(multiblock.getLogic().heatBuffer.getHeatStored(), multiblock.getLogic().heatBuffer.getHeatCapacity(), 6, "H"));
		info.add(TextFormatting.YELLOW + Lang.localise("gui.nc.container.fission_controller.net_cluster_heating") + " " + TextFormatting.WHITE + UnitHelper.prefix(logic.getNetClusterHeating(), 6, "H/t"));
		info.add(TextFormatting.BLUE + Lang.localise("gui.nc.container.fission_controller.total_cluster_cooling") + " " + TextFormatting.WHITE + UnitHelper.prefix(-multiblock.cooling, 6, "H/t"));
		return info;
	}
	
	public void drawHeatTooltip(int mouseX, int mouseY, int x, int y, int width, int height) {
		drawTooltip(heatInfo(), mouseX, mouseY, x, y, width, height);
	}
	
	// TODO
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int fontColor = multiblock.isReactorOn ? -1 : 15641088;
		String title = multiblock.getInteriorLengthX() + "*" + multiblock.getInteriorLengthY() + "*" + multiblock.getInteriorLengthZ() + " " + Lang.localise("gui.nc.container.salt_fission_controller.reactor");
		fontRenderer.drawString(title, xSize / 2 - width(title) / 2, 6, fontColor);
		
		String underline = StringHelper.charLine('-', MathHelper.ceil((double) width(title) / width("-")));
		fontRenderer.drawString(underline, xSize / 2 - width(underline) / 2, 12, fontColor);
		
		String clusters = Lang.localise("gui.nc.container.fission_controller.clusters") + " " + multiblock.clusterCount;
		fontRenderer.drawString(clusters, xSize / 2 - width(clusters) / 2, 22, fontColor);
		
		String heatMult = NCUtil.isModifierKeyDown() ? Lang.localise("gui.nc.container.fission_controller.efficiency") + " " + NCMath.decimalPlaces(100D * multiblock.meanEfficiency, 1) + "%" : Lang.localise("gui.nc.container.fission_controller.heat_mult") + " " + NCMath.decimalPlaces(100D * multiblock.meanHeatMult, 1) + "%";
		fontRenderer.drawString(heatMult, xSize / 2 - width(heatMult) / 2, 34, fontColor);
		
		String speedMult = Lang.localise("gui.nc.container.salt_fission_controller.heating_speed_multiplier") + " " + NCMath.decimalPlaces(100D * logic.meanHeatingSpeedMultiplier, 1) + "%";
		fontRenderer.drawString(speedMult, xSize / 2 - width(speedMult) / 2, 46, fontColor);
		
		String sparsity = NCUtil.isModifierKeyDown() ? Lang.localise("gui.nc.container.fission_controller.useful_parts") + " " + multiblock.usefulPartCount + "/" + multiblock.getInteriorVolume() : Lang.localise("gui.nc.container.fission_controller.sparsity") + " " + NCMath.decimalPlaces(100D * multiblock.sparsityEfficiencyMult, 1) + "%";
		fontRenderer.drawString(sparsity, xSize / 2 - width(sparsity) / 2, 58, fontColor);
		
		String temperature = Lang.localise("gui.nc.container.fission_controller.temperature") + " " + (NCUtil.isModifierKeyDown() ? multiblock.getLogic().getTemperature() - 273 + " C" : multiblock.getLogic().getTemperature() + " K");
		fontRenderer.drawString(temperature, xSize / 2 - width(temperature) / 2, NCUtil.isModifierKeyDown() ? 70 : 76, fontColor);
		
		if (!NCUtil.isModifierKeyDown()) {
			String netClusterHeating = Lang.localise("gui.nc.container.fission_controller.net_cluster_heating") + " " + UnitHelper.prefix(logic.getNetClusterHeating(), 6, "H/t");
			fontRenderer.drawString(netClusterHeating, xSize / 2 - width(netClusterHeating) / 2, 88, fontColor);
		}
	}
	
	// TODO
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		
		int h = (int) Math.round((double) multiblock.getLogic().heatBuffer.getHeatStored() / (double) multiblock.getLogic().heatBuffer.getHeatCapacity() * 164);
		drawTexturedModalRect(guiLeft + 6, guiTop + 102, 3, 114, h, 6);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new MultiblockButton.ClearAllMaterial(0, guiLeft + 153, guiTop + 81));
	}
	
	@Override
	protected void actionPerformed(GuiButton guiButton) {
		if (multiblock.WORLD.isRemote) {
			if (guiButton.id == 0 && NCUtil.isModifierKeyDown()) {
				PacketHandler.instance.sendToServer(new ClearAllMaterialPacket(controllerPos));
			}
		}
	}
}
