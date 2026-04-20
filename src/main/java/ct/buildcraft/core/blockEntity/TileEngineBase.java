package ct.buildcraft.core.blockEntity;

import ct.buildcraft.api.enums.EnumPowerStage;

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */



import ct.buildcraft.core.block.BlockEngine;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;



public abstract class TileEngineBase extends BlockEntity {

	/** Heat per {@link MjAPI#MJ}. */
	public static final double HEAT_PER_MJ = 0.0023;

	public static final double MIN_HEAT = 20;
	public static final double IDEAL_HEAT = 100;
	public static final double MAX_HEAT = 250;

	public IEnergyStorage targe;

	protected double heat = MIN_HEAT;// TODO: sync gui data
	protected int power = 0;// TODO: sync gui data
//    private long lastPower = 0;
	/** Increments from 0 to 1. Above 0.5 all of the held power is emitted. */
	public float progress;

//	private float lastProgress;
	// private int progressPart = 0;

	protected EnumPowerStage powerStage = EnumPowerStage.BLUE;
	protected Direction currentDirection = Direction.UP;

	public long currentOutput;// TODO: sync gui data
	public boolean isRedstonePowered = false;
	protected boolean isPumping = true;

	private boolean a = false;

//    LazyOptional<IEnergyStorage> EnergyLazyOptional ;

	/**
	 * The model variables, used to keep track of the various state-based variables.
	 */
//    public final ModelVariableData clientModelData = new ModelVariableData();

	// Needed: Power stored

//    public TileEngineBase_BC8() {}

	public TileEngineBase(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
		super(p_155228_, p_155229_, p_155230_);
//		this.EnergyLazyOptional = LazyOptional.of(() -> battery);
		// TODO Auto-generated constructor stub
	}

	// Supplied instance (e.g. () -> inventoryHandler)
	// Ensure laziness as initialization should only happen when needed
	/*
	 * @Override public <T> LazyOptional<T> getCapability(@Nonnull Capability<T>
	 * cap, Direction facing) { if (cap == ForgeCapabilities.ENERGY) { return
	 * EnergyLazyOptional.cast();
	 * 
	 * } else { return super.getCapability(cap, facing); }
	 * 
	 * }
	 */

	public InteractionResult attemptRotation() {
//        OrderedEnumMap<Direction> possible = VanillaRotationHandlers.ROTATE_FACING;
		Direction current = currentDirection;
		for (int i = 0; i < 6; i++) {
//            current = possible.next(current);
			if (isFacingReceiver(current)) {
				if (currentDirection != current) {
					currentDirection = current;

					// makeTileCache();
//                    sendNetworkUpdate(NET_RENDER_DATA);
//                    redrawBlock();
//                    world.notifyNeighborsRespectDebug(getPos(), getBlockType(), true);
					return InteractionResult.SUCCESS;
				}
				return InteractionResult.FAIL;
			}
		}
		return InteractionResult.FAIL;
	}

	private boolean isFacingReceiver(Direction dir) {
		return true;
		// return this.targe != null;
	}

	protected final boolean canChain() {
		return getMaxChainLength() > 0;
	}

	/**
	 * @return The number of additional engines that this engine can send power
	 *         through.
	 */
	protected int getMaxChainLength() {
		return 2;
	}

	public void rotateIfInvalid() {
		if (currentDirection != null && isFacingReceiver(currentDirection)) {
			return;
		}
		attemptRotation();
		if (currentDirection == null) {
			currentDirection = Direction.UP;
		}
	}

	public void onPlaced() {
		currentDirection = null;// Force rotateIfInvalid to always attempt to rotate
		rotateIfInvalid();
	}

	@Deprecated
	/** @return The heat of the current biome, in celsius. */
	protected float getBiomeHeat() {
		return 30;
	}

	public double getPowerLevel() {
		return power / (double) getMaxPower();
	}

	protected EnumPowerStage computePowerStage() {
		double heatLevel = getHeatLevel();
		if (heatLevel < 0.25f)
			return EnumPowerStage.BLUE;
		else if (heatLevel < 0.5f)
			return EnumPowerStage.GREEN;
		else if (heatLevel < 0.75f)
			return EnumPowerStage.YELLOW;
		else if (heatLevel < 0.85f)
			return EnumPowerStage.RED;
		else
			return EnumPowerStage.OVERHEAT;
	}

	public final EnumPowerStage getPowerStage() {
//        if (!level.isClientSide()) {
		EnumPowerStage newStage = computePowerStage();

		if (powerStage != newStage) {
			powerStage = newStage;
//                sendNetworkUpdate(NET_RENDER_DATA);
		}
//        }

		return powerStage;
	}

	public void updateHeatLevel() {
		heat = ((MAX_HEAT - MIN_HEAT) * getPowerLevel()) + MIN_HEAT;
	}

	public double getHeatLevel() {
		return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
	}

	public double getIdealHeatLevel() {
		return heat / IDEAL_HEAT;
	}

	public double getHeat() {
		return heat;
	}

	public double getPistonSpeed() {
		switch (getPowerStage()) {
		case BLUE:
			return 0.02;
		case GREEN:
			return 0.04;
		case YELLOW:
			return 0.08;
		case RED:
			return 0.12;
		default:
			return 0;
		}
	}

	public void setPowered(boolean a) {
		isRedstonePowered = a;
	}

	public void tick() {
//        deltaManager.tick();
		if (!hasLevel())
			return;
		BlockState bs = this.getBlockState();
		isRedstonePowered = getBlockState().getValue(BlockEngine.ENABLED);
		if (targe == null && bs.getValue(BlockEngine.CONNECTED)) {
			this.currentDirection = bs.getValue(BlockEngine.FACING);
			BlockEntity be = level.getBlockEntity(getBlockPos().offset(currentDirection.getNormal()));
			if (be != null)
				be.getCapability(ForgeCapabilities.ENERGY).ifPresent((a) -> targe = a);
		} else if (targe != null && !bs.getValue(BlockEngine.CONNECTED)) {
			this.targe = null;
		}
		boolean overheat = getPowerStage() == EnumPowerStage.OVERHEAT;

		if (true) {
			if (isRedstonePowered && this.isPumping) {
				if (a)
					progress += getPistonSpeed();
				else
					progress -= getPistonSpeed();

				if (progress >= 1) {
					progress = 1;
					a = false;
				} else if (progress <= 0) {
					progress = 0;
					a = true;
				}
			} else if (progress > 0) {
				progress -= getPistonSpeed();
			}
//            clientModelData.tick();
//            return;
		}

//        lastPower = 0;

		if (!isRedstonePowered) {
			if (power > 1) {
				power -= 1;
			} else if (power > 0) {
				power = 0;
			}
		}

		updateHeatLevel();
		getPowerStage();

		engineUpdate();

		/*
		 * if (progressPart != 0) { progress += getPistonSpeed();
		 * 
		 * if (progress > 0.5 && progressPart == 1) { progressPart = 2; sendPower(); //
		 * Comment out for constant power } else if (progress >= 1) { progress = 0;
		 * progressPart = 0; } } else
		 */
		if (isRedstonePowered && isActive()) {
			if (targe != null && targe.canReceive()) {
				if ((progress == 1 || progress == 0)) {
					power -= targe.receiveEnergy(power, false);
//            		LogUtils.getLogger().info(Integer.toString(power));
				}
//                progressPart = 1;
				setPumping(true);
			} else {
				setPumping(false);
			}
		} else {
			setPumping(false);
		}

		// Uncomment for constant power
		// if (isRedstonePowered && isActive()) {
		// sendPower();
		// } else currentOutput = 0;

		if (!overheat) {
			burn();
		}

		// level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(),
		// progressPart);
	}

	// Uncomment out for constant power
	// public float getActualOutput() {
	// float heatLevel = getIdealHeatLevel();
	// return getCurrentOutput() * heatLevel;
	// }

	protected void burn() {
	}

	protected void engineUpdate() {
		if (!isRedstonePowered) {
			if (power >= 1) {
				power -= 1;
			} else if (power < 1) {
				power = 0;
			}
		}
	}

	public boolean isActive() {
		return true;
	}

	protected final void setPumping(boolean isActive) {
		if (this.isPumping == isActive) {
			return;
		}

		this.isPumping = isActive;
//        sendNetworkUpdate(NET_RENDER_DATA);
	}

	// TEMP
	public interface ITileBuffer {
		BlockEntity getTile();
	}

	/** Temp! This should be replaced with a tile buffer! */
	public ITileBuffer getTileBuffer(Direction side) {
		BlockEntity tile = level.getBlockEntity(getBlockPos().offset(side.getNormal()));
		return () -> tile;
	}

	/*
	 * @Override public void invalidate() { super.invalidate(); // tileCache = null;
	 * // checkOrientation = true; }
	 * 
	 * @Override public void validate() { super.validate(); // tileCache = null; //
	 * checkOrientation = true;
	 */

	/* STATE INFORMATION */
	public abstract boolean isBurning();

	// IPowerReceptor stuffs -- move!
	// @Override
	// public PowerReceiver getPowerReceiver(ForgeDirection side) {
	// return powerHandler.getPowerReceiver();
	// }
	//
	// @Override
	// public void doWork(PowerHandler workProvider) {
	// if (worldObj.isClientSide()) {
	// return;
	// }
	//
	// addEnergy(powerHandler.useEnergy(1, maxEnergyReceived(), true) * 0.95F);
	// }

	public void addPower(long microJoules) {
		power += microJoules;
//        lastPower += microJoules;

		if (getPowerStage() == EnumPowerStage.OVERHEAT) {
			// TODO: turn engine off
			// worldObj.createExplosion(null, xCoord, yCoord, zCoord, explosionRange(),
			// true);
			// worldObj.setBlockToAir(xCoord, yCoord, zCoord);
		}

		if (power > getMaxPower()) {
			power = getMaxPower();
		}
	}

	public abstract int getMaxPower();

	public abstract float explosionRange();

	public long getEnergyStored() {
		return power;
	}

	public boolean isEngineOn() {
		return isPumping;
	}

	public Direction getCurrentFacing() {
		return currentDirection;
	}


	@Override
	public void onLoad() {

		super.onLoad();
	}

	@Override
	public void load(CompoundTag nbt) {
		power = nbt.getInt("power");
		progress = nbt.getFloat("progress");
		heat = nbt.getDouble("heat");
		super.load(nbt);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt) {
		nbt.putInt("power", power);
		nbt.putFloat("progress", progress);
		nbt.putDouble("heat", heat);
		super.saveAdditional(nbt);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		// TODO Auto-generated method stub
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		// TODO Auto-generated method stub
		CompoundTag ct = super.getUpdateTag();
		saveAdditional(ct);
		return ct;
	}
	
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		load(tag);
	}

	@OnlyIn(Dist.CLIENT)
	public abstract TextureAtlasSprite getTextureBack();

	@OnlyIn(Dist.CLIENT)
	public abstract TextureAtlasSprite getTextureSide();


	

	public int getCurrentOutput() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void onActivated(Player player, InteractionHand hand, Direction side) {
		// TODO Auto-generated method stub
	}

}
