/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftEnergy;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.DefaultProps;
import buildcraft.core.TileBuffer;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.energy.gui.ContainerEngine;

public abstract class TileEngine extends TileBuildCraft implements IPowerReceptor, IPowerEmitter, IOverrideDefaultTriggers, IPipeConnection {

	public static final ResourceLocation WOOD_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_wood.png");
	public static final ResourceLocation STONE_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_stone.png");
	public static final ResourceLocation IRON_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/base_iron.png");

	public enum EnergyStage {

		BLUE, GREEN, YELLOW, RED, OVERHEAT
	}
	public static final float MIN_HEAT = 20;
	public static final float IDEAL_HEAT = 100;
	public static final float MAX_HEAT = 250;
	protected int progressPart = 0;
	protected boolean lastPower = false;
	protected PowerHandler powerHandler;
	public float currentOutput = 0;
	public boolean isRedstonePowered = false;
	private boolean checkOrienation = false;
	private TileBuffer[] tileCache;
	public float progress;
	public float energy;
	public float heat = MIN_HEAT;
	//
	public @TileNetworkData
	EnergyStage energyStage = EnergyStage.BLUE;
	public @TileNetworkData
	ForgeDirection orientation = ForgeDirection.UP;
	public @TileNetworkData
	boolean isPumping = false; // Used for SMP synch

	public TileEngine() {
		powerHandler = new PowerHandler(this, Type.ENGINE);
		powerHandler.configurePowerPerdition(1, 100);
	}

	@Override
	public void initialize() {
		if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
			powerHandler.configure(minEnergyReceived(), maxEnergyReceived(), 1, getMaxEnergy());
			checkRedstonePower();
		}
	}

	public abstract ResourceLocation getTextureFile();

	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		return false;
	}

	public float getEnergyLevel() {
		return energy / getMaxEnergy();
	}

	protected EnergyStage computeEnergyStage() {
		float energyLevel = getHeatLevel();
		if (energyLevel < 0.25f)
			return EnergyStage.BLUE;
		else if (energyLevel < 0.5f)
			return EnergyStage.GREEN;
		else if (energyLevel < 0.75f)
			return EnergyStage.YELLOW;
		else if (energyLevel < 1f)
			return EnergyStage.RED;
		else
			return EnergyStage.OVERHEAT;
	}

	public final EnergyStage getEnergyStage() {
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			if (energyStage == EnergyStage.OVERHEAT)
				return energyStage;
			EnergyStage newStage = computeEnergyStage();

			if (energyStage != newStage) {
				energyStage = newStage;
				sendNetworkUpdate();
			}
		}

		return energyStage;
	}

	public void updateHeatLevel() {
		heat = ((MAX_HEAT - MIN_HEAT) * getEnergyLevel()) + MIN_HEAT;
	}

	public float getHeatLevel() {
		return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
	}

	public float getIdealHeatLevel() {
		return heat / IDEAL_HEAT;
	}

	public float getHeat() {
		return heat;
	}

	public float getPistonSpeed() {
		if (CoreProxy.proxy.isSimulating(worldObj))
			return Math.max(0.16f * getHeatLevel(), 0.01f);
		switch (getEnergyStage()) {
			case BLUE:
				return 0.02F;
			case GREEN:
				return 0.04F;
			case YELLOW:
				return 0.08F;
			case RED:
				return 0.16F;
			default:
				return 0;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
			if (progressPart != 0) {
				progress += getPistonSpeed();

				if (progress > 1) {
					progressPart = 0;
					progress = 0;
				}
			} else if (this.isPumping)
				progressPart = 1;

			return;
		}

		if (checkOrienation) {
			checkOrienation = false;
			if (!isOrientationValid())
				switchOrientation(true);
		}
		
		if (!isRedstonePowered)
			if (energy > 1)
				energy--;

		updateHeatLevel();
		getEnergyStage();
		engineUpdate();

		TileEntity tile = getTileBuffer(orientation).getTile();

		if (progressPart != 0) {
			progress += getPistonSpeed();

			if (progress > 0.5 && progressPart == 1) {
				progressPart = 2;
				sendPower(); // Comment out for constant power
			} else if (progress >= 1) {
				progress = 0;
				progressPart = 0;
			}
		} else if (isRedstonePowered && isActive())
			if (isPoweredTile(tile, orientation))
				if (getPowerToExtract() > 0) {
					progressPart = 1;
					setPumping(true);
				} else
					setPumping(false);
			else
				setPumping(false);
		else
			setPumping(false);

		// Uncomment for constant power
//		if (isRedstonePowered && isActive()) {
//			sendPower();
//		} else currentOutput = 0;

		burn();
	}

	private float getPowerToExtract() {
		TileEntity tile = getTileBuffer(orientation).getTile();
		PowerReceiver receptor = ((IPowerReceptor) tile).getPowerReceiver(orientation.getOpposite());
		return extractEnergy(receptor.getMinEnergyReceived(), receptor.getMaxEnergyReceived(), false); // Comment out for constant power
//		return extractEnergy(0, getActualOutput(), false); // Uncomment for constant power
	}

	private void sendPower() {
		TileEntity tile = getTileBuffer(orientation).getTile();
		if (isPoweredTile(tile, orientation)) {
			PowerReceiver receptor = ((IPowerReceptor) tile).getPowerReceiver(orientation.getOpposite());

			float extracted = getPowerToExtract();
			if (extracted > 0) {
				float needed = receptor.receiveEnergy(PowerHandler.Type.ENGINE, extracted, orientation.getOpposite());
				extractEnergy(receptor.getMinEnergyReceived(), needed, true); // Comment out for constant power
//				currentOutput = extractEnergy(0, needed, true); // Uncomment for constant power
			}
		}
	}

	// Uncomment out for constant power
//	public float getActualOutput() {
//		float heatLevel = getIdealHeatLevel();
//		return getCurrentOutput() * heatLevel;
//	}
	protected void burn() {
	}

	protected void engineUpdate() {
		if (!isRedstonePowered)
			if (energy >= 1)
				energy -= 1;
			else if (energy < 1)
				energy = 0;
	}

	public boolean isActive() {
		return true;
	}

	protected final void setPumping(boolean isActive) {
		if (this.isPumping == isActive)
			return;

		this.isPumping = isActive;
		sendNetworkUpdate();
	}

	public boolean isOrientationValid() {
		TileEntity tile = getTileBuffer(orientation).getTile();
		return isPoweredTile(tile, orientation);
	}

	public boolean switchOrientation(boolean preferPipe) {
		if (preferPipe && switchOrientation_do(true))
			return true;
		return switchOrientation_do(false);
	}

	private boolean switchOrientation_do(boolean pipesOnly) {
		for (int i = orientation.ordinal() + 1; i <= orientation.ordinal() + 6; ++i) {
			ForgeDirection o = ForgeDirection.VALID_DIRECTIONS[i % 6];

			TileEntity tile = getTileBuffer(o).getTile();

			if ((!pipesOnly || tile instanceof IPipeTile) && isPoweredTile(tile, o)) {
				orientation = o;
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));

				return true;
			}
		}
		return false;
	}

	public TileBuffer getTileBuffer(ForgeDirection side) {
		if (tileCache == null)
			tileCache = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
		return tileCache[side.ordinal()];
	}

	@Override
	public void invalidate() {
		super.invalidate();
		tileCache = null;
		checkOrienation = true;
	}

	@Override
	public void validate() {
		super.validate();
		tileCache = null;
		checkOrienation = true;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		orientation = ForgeDirection.getOrientation(data.getInteger("orientation"));
		progress = data.getFloat("progress");
		energy = data.getFloat("energyF");
		NBTBase tag = data.getTag("heat");
		if (tag instanceof NBTTagFloat)
			heat = data.getFloat("heat");
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setInteger("orientation", orientation.ordinal());
		data.setFloat("progress", progress);
		data.setFloat("energyF", energy);
		data.setFloat("heat", heat);
	}

	public void getGUINetworkData(int id, int value) {
		switch (id) {
			case 0:
				int iEnergy = Math.round(energy * 10);
				iEnergy = (iEnergy & 0xffff0000) | (value & 0xffff);
				energy = iEnergy / 10;
				break;
			case 1:
				iEnergy = Math.round(energy * 10);
				iEnergy = (iEnergy & 0xffff) | ((value & 0xffff) << 16);
				energy = iEnergy / 10;
				break;
			case 2:
				currentOutput = value / 10F;
				break;
			case 3:
				heat = value / 100F;
				break;
		}
	}

	public void sendGUINetworkData(ContainerEngine containerEngine, ICrafting iCrafting) {
		iCrafting.sendProgressBarUpdate(containerEngine, 0, Math.round(energy * 10) & 0xffff);
		iCrafting.sendProgressBarUpdate(containerEngine, 1, (Math.round(energy * 10) & 0xffff0000) >> 16);
		iCrafting.sendProgressBarUpdate(containerEngine, 2, Math.round(currentOutput * 10));
		iCrafting.sendProgressBarUpdate(containerEngine, 3, Math.round(heat * 100));
	}

	public void delete() {
	}

	/* STATE INFORMATION */
	public abstract boolean isBurning();

	public abstract int getScaledBurnTime(int scale);

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;

		addEnergy(powerHandler.useEnergy(1, maxEnergyReceived(), true) * 0.95F);
	}

	public void addEnergy(float addition) {
		energy += addition;

		if (getEnergyStage() == EnergyStage.OVERHEAT) {
			worldObj.createExplosion(null, xCoord, yCoord, zCoord, explosionRange(), true);
			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
		}

		if (energy > getMaxEnergy())
			energy = getMaxEnergy();
	}

	public float extractEnergy(float min, float max, boolean doExtract) {
		if (energy < min)
			return 0;

		float actualMax;

		if (max > maxEnergyExtracted())
			actualMax = maxEnergyExtracted();
		else
			actualMax = max;

		if (actualMax < min)
			return 0;

		float extracted;

		if (energy >= actualMax) {
			extracted = actualMax;
			if (doExtract)
				energy -= actualMax;
		} else {
			extracted = energy;
			if (doExtract)
				energy = 0;
		}

		return extracted;
	}

	public boolean isPoweredTile(TileEntity tile, ForgeDirection side) {
		if (tile instanceof IPowerReceptor)
			return ((IPowerReceptor) tile).getPowerReceiver(side.getOpposite()) != null;

		return false;
	}

	public abstract float getMaxEnergy();

	public float minEnergyReceived() {
		return 2;
	}

	public abstract float maxEnergyReceived();

	public abstract float maxEnergyExtracted();

	public abstract float explosionRange();

	public float getEnergyStored() {
		return energy;
	}

	public abstract float getCurrentOutput();

	@Override
	public LinkedList<ITrigger> getTriggers() {
		LinkedList<ITrigger> triggers = new LinkedList<ITrigger>();

		triggers.add(BuildCraftEnergy.triggerBlueEngineHeat);
		triggers.add(BuildCraftEnergy.triggerGreenEngineHeat);
		triggers.add(BuildCraftEnergy.triggerYellowEngineHeat);
		triggers.add(BuildCraftEnergy.triggerRedEngineHeat);

		return triggers;
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with) {
		if (type == PipeType.POWER)
			return ConnectOverride.DEFAULT;
		if (with == orientation)
			return ConnectOverride.DISCONNECT;
		return ConnectOverride.DEFAULT;
	}

	@Override
	public boolean canEmitPowerFrom(ForgeDirection side) {
		return side == orientation;
	}

	public void checkRedstonePower() {
		isRedstonePowered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}
}
