package buildcraft.transport;

public class PipeTransportStructure extends PipeTransport {

	@Override
	public boolean allowsConnect(PipeTransport with) {
		return with instanceof PipeTransportStructure;
	}

}
