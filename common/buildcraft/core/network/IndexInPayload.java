package buildcraft.core.network;

/**
 * Keeps track of the indices to use when writing data to payload arrays. Internal use only.
 */
public class IndexInPayload {

	public IndexInPayload(int intIndex, int floatIndex, int stringIndex, int itemStackIndex) {
		this.intIndex = intIndex;
		this.floatIndex = floatIndex;
		this.stringIndex = stringIndex;
		this.itemStackIndex = itemStackIndex;
	}

	public int intIndex = 0;
	public int floatIndex = 0;
	public int stringIndex = 0;
	public int itemStackIndex = 0;
}
