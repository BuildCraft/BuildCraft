package ct.buildcraft.core.lib.net;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class MessageContainer {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel NET = NetworkRegistry.newSimpleChannel(
	  new ResourceLocation("mymodid", "main"),
	  () -> PROTOCOL_VERSION,
	  PROTOCOL_VERSION::equals,
	  PROTOCOL_VERSION::equals
	);
	public static void registry() {
		NET.registerMessage(100, BCMSG.class , BCMSG::toByte , BCMSG::new, BCMSG::handle);//pump
	}
	public static class BCMSG {
		
		
		public BCMSG(){
			
		}
		public BCMSG(FriendlyByteBuf b){
			
		}
		public void toByte(FriendlyByteBuf b) {
			
		}
		public void handle(Supplier<NetworkEvent.Context> supplier ) {
			
		}
		Boolean build;
		
		
	}
}
