package net.minecraft.src.buildcraft.builders;

import java.io.IOException;

import net.minecraft.src.BuildCraftBuilders;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.BptPlayerIndex;
import net.minecraft.src.buildcraft.core.BptRootIndex;
import net.minecraft.src.buildcraft.core.CoreProxy;

public class ClientBuilderHook implements IBuilderHook {

	@Override
	public void rootIndexInitialized(BptRootIndex rootBptIndex) throws IOException {
		if (!APIProxy.isServerSide() && !APIProxy.isClient(ModLoader.getMinecraftInstance().theWorld)) {
			// If we're on a SSP game, then pre-load the player list

			BptPlayerIndex playerIndex = new BptPlayerIndex(
					CoreProxy.playerName() + ".list", rootBptIndex);
			BuildCraftBuilders.playerLibrary.put(CoreProxy.playerName(), playerIndex);
		}
	}

}
