package buildcraft.energy;

import buildcraft.core.DefaultProps;

public class EngineCreative extends Engine {

	public EngineCreative(TileEngine engine) {
		super(engine);

		maxEnergy = 10000000;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_PATH_BLOCKS + "/base_wood.png";
	}

	@Override
	public int explosionRange() {
		return 0;
	}

	@Override
	public int minEnergyReceived() {
		return 1;
	}

	@Override
	public int maxEnergyReceived() {
		return 10000;
	}

	@Override
	public float getPistonSpeed() {
		switch (getEnergyStage()) {
		case Blue:
			return 0.5F;
		case Green:
			return 0.6F;
		case Yellow:
			return 0.7F;
		case Red:
			return 0.8F;
		default:
			return 0;
		}
	}

	@Override
	public void update() {
		super.update();

		if (tile.isRedstonePowered) {
			energy=maxenergy/4-1;
		}
	}

	@Override
	public boolean isBurning() {
		return tile.isRedstonePowered;
	}

	@Override
	public int getScaledBurnTime(int i) {
		return 0;
	}

	@Override
	public void delete() {

	}

	@Override
	public void burn() {

	}
}
