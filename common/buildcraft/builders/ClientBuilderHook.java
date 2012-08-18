package buildcraft.builders;

import java.io.IOException;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.IBuilderHook;
import buildcraft.core.BptPlayerIndex;
import buildcraft.core.BptRootIndex;
import buildcraft.core.ProxyCore;

import net.minecraft.src.ModLoader;

public class ClientBuilderHook implements IBuilderHook {

	@Override
	public void rootIndexInitialized(BptRootIndex rootBptIndex) throws IOException {
		if (ProxyCore.proxy.isRemote(ModLoader.getMinecraftInstance().theWorld)) {
			// If we're on a SSP game, then pre-load the player list

			BptPlayerIndex playerIndex = new BptPlayerIndex(ProxyCore.proxy.playerName() + ".list", rootBptIndex);
			BuildCraftBuilders.playerLibrary.put(ProxyCore.proxy.playerName(), playerIndex);
		}
	}

}
