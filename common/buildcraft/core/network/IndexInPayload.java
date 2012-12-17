package buildcraft.core.network;

/**
 * Keeps track of the indices to use when writing data to payload arrays. Internal use only.
 */
public class IndexInPayload {

	public IndexInPayload(int intIndex, int floatIndex, int stringIndex) {
		this.intIndex = intIndex;
		this.floatIndex = floatIndex;
		this.stringIndex = stringIndex;
	}

	public int intIndex = 0;
	public int floatIndex = 0;
	public int stringIndex = 0;
}
