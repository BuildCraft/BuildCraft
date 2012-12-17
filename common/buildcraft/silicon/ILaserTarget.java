package buildcraft.silicon;

public interface ILaserTarget {
	boolean hasCurrentWork();

	void receiveLaserEnergy(float energy);

	boolean isInvalid();

	int getXCoord();

	int getYCoord();

	int getZCoord();
}
