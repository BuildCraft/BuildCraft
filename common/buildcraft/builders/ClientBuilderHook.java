package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.blueprints.BptPlayerIndex;
import buildcraft.core.blueprints.BptRootIndex;
import buildcraft.core.proxy.CoreProxy;
import java.io.IOException;

public class ClientBuilderHook implements IBuilderHook {

	@Override
	public void rootIndexInitialized(BptRootIndex rootBptIndex) throws IOException {
		BptPlayerIndex playerIndex = new BptPlayerIndex(CoreProxy.proxy.playerName() + ".list", rootBptIndex);
		BuildCraftBuilders.playerLibrary.put(CoreProxy.proxy.playerName(), playerIndex);
	}
}
