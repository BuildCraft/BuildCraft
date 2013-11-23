package buildcraft.builders;

import buildcraft.BuildCraftBuilders;

public class BuilderProxyClient extends BuilderProxy {

    @Override
	public void registerClientHook() {
		BuildCraftBuilders.addHook(new ClientBuilderHook());
	}
}
