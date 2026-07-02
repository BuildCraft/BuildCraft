package ct.buildcraft.core.test;

import java.util.function.Supplier;

import com.mojang.logging.LogUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class MessageManager {

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
	  new ResourceLocation("test", "main"),
	  () -> PROTOCOL_VERSION,
	  PROTOCOL_VERSION::equals,
	  PROTOCOL_VERSION::equals
	);
	
	public static void preint(){
		INSTANCE.registerMessage(0, TestPacket.class, MessageManager::coder, MessageManager::deCoder, TestPacket.HANDLER);
	}
	
	public static void coder(TestPacket p, FriendlyByteBuf b) {
		b.writeInt(p.i);
	}
	
	public static TestPacket deCoder(FriendlyByteBuf b) {
		return new TestPacket(b.getInt(0));
	}
	
}
