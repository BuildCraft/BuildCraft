package buildcraft.silicon;

public interface ILaserTarget {
	boolean hasCurrentWork();

	void receiveLaserEnergy(float energy);

	boolean isLaserInvalid();

	int getXCoord();

	int getYCoord();

	int getZCoord();
}
