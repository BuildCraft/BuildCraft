package buildcraft.core.network;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.proxy.CoreProxyClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

/**
 * This is a first implementation of a RPC connector, using the regular tile
 * synchronization layers as a communication protocol. As a result, these
 * RPCs must be sent and received by a tile entity.
 */
public class RPCHandler {

	private static Map <String, RPCHandler> handlers =
			new TreeMap <String, RPCHandler> ();

	private Map<String, Integer> methodsMap = new TreeMap<String, Integer>();
	private Method [] methods;

	public RPCHandler (Class c) {
		methods = c.getMethods();

		Arrays.sort(methods, new Comparator <Method> () {
			@Override
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		LinkedList <Method> rpcMethods = new LinkedList<Method>();

		for (int i = 0; i < methods.length; ++i) {
			if (methods [i].getAnnotation (RPC.class) != null) {
				methodsMap.put(methods [i].getName(), rpcMethods.size());
				rpcMethods.add(methods [i]);

				Class formals [] = methods [i].getParameterTypes();

				for (int j = 0; i < formals.length; ++i) {
					if (formals [j].equals(int.class)) {
						// accepted
					} else if (formals [j].equals(String.class)) {
						// accepted
					} else if (formals [j].equals(RPCMessageInfo.class)) {

						// accepted
						// FIXME: only if last one
					} else {
						// all other will be serialized
						//throw new RuntimeException
						//("parameter type " + formals [j].getName() + " not supported in RPC.");
					}
				}
			}
		}

		methods = rpcMethods.toArray(new Method [rpcMethods.size()]);
	}

	public static void rpcServer (TileEntity tile, String method, Object ... actuals) {
		if (!handlers.containsKey(tile.getClass().getName())) {
			handlers.put (tile.getClass().getName(), new RPCHandler (tile.getClass()));
		}

		PacketRPC packet = handlers.get (tile.getClass().getName()).createRCPPacket(tile, method, actuals);

		if (packet != null) {
			CoreProxy.proxy.sendToServer(packet.getPacket());
		}
	}

	public static void rpcPlayer (TileEntity tile, String method, EntityPlayer player, Object ... actuals) {
		if (!handlers.containsKey(tile.getClass().getName())) {
			handlers.put (tile.getClass().getName(), new RPCHandler (tile.getClass()));
		}

		PacketRPC packet = handlers.get (tile.getClass().getName()).createRCPPacket(tile, method, actuals);

		if (packet != null) {
			CoreProxy.proxy.sendToPlayer(player, packet);
		}
	}

	public static void receiveRPC (TileEntity tile, RPCMessageInfo info, DataInputStream data) {
		if (!handlers.containsKey(tile.getClass().getName())) {
			handlers.put (tile.getClass().getName(), new RPCHandler (tile.getClass()));
		}

		handlers.get (tile.getClass().getName()).internalRpcReceive(tile, info, data);
	}

	private PacketRPC createRCPPacket (TileEntity tile, String method, Object ... actuals) {
		if (!methodsMap.containsKey(method)) {
			throw new RuntimeException(method + " is not a callable method of " + getClass().getName());
		}

		int methodIndex = methodsMap.get(method);
		Method m = methods [methodIndex];
		Class formals [] = m.getParameterTypes();

		boolean hasSpecialMessageInfo = formals.length > 0 && formals [formals.length - 1].equals(RPCMessageInfo.class);

		int expectedParameters = hasSpecialMessageInfo ? formals.length - 1 : formals.length;

		if (expectedParameters != actuals.length) {
			// We accept formals + 1 as an argument, in order to support the
			// special last argument RPCMessageInfo

			throw new RuntimeException(getClass().getName() + "." + method
					+ " expects " + m.getParameterTypes().length + "parameters, not " + actuals.length);
		}

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try {
			data.writeInt(tile.xCoord);
			data.writeInt(tile.yCoord);
			data.writeInt(tile.zCoord);

			data.writeShort(methodIndex);

			for (int i = 0; i < actuals.length; ++i) {
				if (formals [i].equals(int.class)) {
					data.writeInt((Integer) actuals [i]);
				} else if (formals [i].equals(String.class)) {
					data.writeUTF((String) actuals [i]);
				} else {
					//data.write
				}
			}

			data.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new PacketRPC(bytes.toByteArray());
	}

	private void internalRpcReceive (TileEntity tile, RPCMessageInfo info, DataInputStream data) {
		try {
			short methodIndex = data.readShort();

			Method m = methods [methodIndex];
			Class formals [] = m.getParameterTypes();

			Object [] actuals = new Object [formals.length];

			boolean hasSpecialMessageInfo = formals.length > 0 && formals [formals.length - 1].equals(RPCMessageInfo.class);

			int expectedParameters = hasSpecialMessageInfo ? formals.length - 1 : formals.length;

			for (int i = 0; i < expectedParameters; ++i) {
				if (formals [i].equals(int.class)) {
					actuals [i] = data.readInt();
				} else if (formals [i].equals(String.class)) {
					actuals [i] = data.readUTF();
				}
			}

			if (hasSpecialMessageInfo) {
				actuals [actuals.length - 1] = info;
			}

			m.invoke(tile, actuals);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
