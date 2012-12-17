package buildcraft.core.network;

/**
 * Implemented by TileEntites
 * 
 * @author Krapht
 * 
 */
public interface ISyncedTile {

	/**
	 * called by the PacketHandler for each state contained in a StatePacket
	 * 
	 * @param stateId
	 * @return an object that should be refreshed from the state
	 */
	public IClientState getStateInstance(byte stateId);

	/**
	 * Called after a state has been updated
	 * 
	 * @param stateId
	 */
	public void afterStateUpdated(byte stateId);
}
