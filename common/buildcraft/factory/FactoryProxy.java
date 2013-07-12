package buildcraft.factory;

import buildcraft.core.EntityBlock;
import cpw.mods.fml.common.SidedProxy;
import net.minecraft.world.World;

public class FactoryProxy {
	@SidedProxy(clientSide = "buildcraft.factory.FactoryProxyClient", serverSide = "buildcraft.factory.FactoryProxy")
	public static FactoryProxy proxy;

	public void initializeTileEntities() {
	}

	public void initializeEntityRenders() {
	}

	public void initializeNEIIntegration() {
	}

    public EntityBlock newPumpTube(World w)
    {
        return new EntityBlock(w);
    }

    public EntityBlock newDrill(World w, double i, double j, double k, double l, double d, double e)
    {
        return new EntityBlock(w, i, j, k, l, d, e);
    }

    public EntityBlock newDrillHead(World w, double i, double j, double k, double l, double d, double e)
    {
        return new EntityBlock(w, i, j, k, l, d, e);
    }
}
