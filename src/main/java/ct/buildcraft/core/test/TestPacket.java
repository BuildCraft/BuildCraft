package ct.buildcraft.core.test;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ct.buildcraft.lib.net.MessageUpdateTile;
import com.mojang.logging.LogUtils;

import net.minecraftforge.network.NetworkEvent;

public class TestPacket {
	public TestPacket(int int1) {
		i = int1;
	}

	int i ;
    public static final BiConsumer<TestPacket, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
    	LogUtils.getLogger().info(Integer.toString(message.i));
    	ctx.get().setPacketHandled(true);
    };
}
