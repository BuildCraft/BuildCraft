/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.JavaTools;
import buildcraft.core.DefaultProps;
import buildcraft.core.network.serializers.ClassMapping;
import buildcraft.core.network.serializers.ClassSerializer;
import buildcraft.core.network.serializers.SerializationContext;
import buildcraft.transport.Pipe;

/**
 * This is a first implementation of a RPC connector, using the regular tile
 * synchronization layers as a communication protocol. As a result, these
 * RPCs must be sent and received by a tile entity.
 */
public final class RPCHandler {

	public static int MAX_PACKET_SIZE = 30 * 1024;

	private static Map<String, RPCHandler> handlers = new TreeMap<String, RPCHandler>();

	private Map<String, Integer> methodsMap = new TreeMap<String, Integer>();

	class MethodMapping {
		Method method;
		Class [] parameters;
		ClassSerializer [] mappings;
		boolean hasInfo = false;
	}

	private MethodMapping [] methods;

	private RPCHandler(Class c) {
		Method [] sortedMethods = JavaTools.getAllMethods (c).toArray(new Method [0]);

		LinkedList<MethodMapping> mappings = new LinkedList<MethodMapping>();

		Arrays.sort(sortedMethods, new Comparator<Method>() {
			@Override
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		LinkedList<Method> rpcMethods = new LinkedList<Method>();

		for (Method sortedMethod : sortedMethods) {
			if (sortedMethod.getAnnotation (RPC.class) != null) {
				sortedMethod.setAccessible(true);
				methodsMap.put(sortedMethod.getName(), rpcMethods.size());
				rpcMethods.add(sortedMethod);

				MethodMapping mapping = new MethodMapping();
				mapping.method = sortedMethod;
				mapping.parameters = sortedMethod.getParameterTypes();
				mapping.mappings = new ClassSerializer [mapping.parameters.length];

				for (int j = 0; j < mapping.parameters.length; ++j) {
					if (int.class.equals(mapping.parameters[j])) {
						// accepted
					} else if (char.class.equals(mapping.parameters[j])) {
						// accepted
					} else if (float.class.equals(mapping.parameters[j])) {
						// accepted
					} else if (mapping.parameters [j].equals(RPCMessageInfo.class)) {
						mapping.hasInfo = true;
					} else {
						mapping.mappings [j] = ClassMapping.get(mapping.parameters [j]);
					}
				}

				mappings.add(mapping);
			}
		}

		methods = mappings.toArray(new MethodMapping [mappings.size()]);
	}

	public static void rpcServer (TileEntity tile, String method, Object ... actuals) {
		if (!handlers.containsKey(tile.getClass().getName())) {
			handlers.put (tile.getClass().getName(), new RPCHandler (tile.getClass()));
		}

		PacketRPCTile packet = handlers.get (tile.getClass().getName()).createRCPPacket(tile, method, actuals);

		if (packet != null) {
			ArrayList<PacketRPCTile> packets = packet.breakIntoSmallerPackets(30 * 1024);

			for (PacketRPCTile p : packet.breakIntoSmallerPackets(MAX_PACKET_SIZE)) {
				BuildCraftCore.instance.sendToServer(p);
			}
		}
	}

	public static void rpcPlayer (TileEntity tile, String method, EntityPlayer player, Object ... actuals) {
		if (!handlers.containsKey(tile.getClass().getName())) {
			handlers.put (tile.getClass().getName(), new RPCHandler (tile.getClass()));
		}

		PacketRPCTile packet = handlers.get (tile.getClass().getName()).createRCPPacket(tile, method, actuals);

		if (packet != null) {
			for (PacketRPCTile p : packet.breakIntoSmallerPackets(MAX_PACKET_SIZE)) {
				BuildCraftCore.instance.sendToPlayer(player, p);
			}
		}
	}

	public static void rpcBroadcastDefaultPlayers (Pipe pipe, String method, Object ... actuals) {
		RPCHandler.rpcBroadcastPlayers(pipe, method, DefaultProps.NETWORK_UPDATE_RANGE, actuals);
	}

	public static void rpcBroadcastPlayers (TileEntity tile, String method, Object ... actuals) {
		RPCHandler.rpcBroadcastPlayersAtDistance(tile, method, DefaultProps.NETWORK_UPDATE_RANGE, actuals);
	}

	public static void rpcBroadcastPlayersAtDistance (TileEntity tile, String method, int maxDistance, Object ... actuals) {
		if (!handlers.containsKey(tile.getClass().getName())) {
			handlers.put (tile.getClass().getName(), new RPCHandler (tile.getClass()));
		}

		PacketRPCTile packet = handlers.get (tile.getClass().getName()).createRCPPacket(tile, method, actuals);

		if (packet != null) {
			for (PacketRPCTile p : packet
					.breakIntoSmallerPackets(MAX_PACKET_SIZE)) {
				for (Object o : tile.getWorldObj().playerEntities) {
					EntityPlayerMP player = (EntityPlayerMP) o;

					if (Math.abs(player.posX - tile.xCoord) <= maxDistance
							&& Math.abs(player.posY - tile.yCoord) <= maxDistance
							&& Math.abs(player.posZ - tile.zCoord) <= maxDistance) {
						BuildCraftCore.instance.sendToPlayer(player, p);
					}
				}
			}
		}
	}

	public static void rpcBroadcastPlayers (Pipe pipe, String method, int maxDistance, Object ... actuals) {
		if (!handlers.containsKey(pipe.getClass().getName())) {
			handlers.put (pipe.getClass().getName(), new RPCHandler (pipe.getClass()));
		}

		PacketRPCPipe packet = handlers.get (pipe.getClass().getName()).createRCPPacket(pipe, method, actuals);

		if (packet != null) {
			for (Object o : pipe.container.getWorld().playerEntities) {
				EntityPlayerMP player = (EntityPlayerMP) o;

				if (Math.abs(player.posX - pipe.container.xCoord) <= maxDistance
						&& Math.abs(player.posY - pipe.container.yCoord) <= maxDistance
						&& Math.abs(player.posZ - pipe.container.zCoord) <= maxDistance) {
					BuildCraftCore.instance.sendToPlayer(player, packet);
				}
			}
		}
	}

	public static void receiveRPC (TileEntity tile, RPCMessageInfo info, ByteBuf data) {
		if (tile != null) {
			if (!handlers.containsKey(tile.getClass().getName())) {
				handlers.put(tile.getClass().getName(),
						new RPCHandler(tile.getClass()));
			}

			handlers.get(tile.getClass().getName()).internalRpcReceive(tile,
					info, data);
		}
	}

	public static void receiveRPC (Pipe pipe, RPCMessageInfo info, ByteBuf data) {
		if (pipe != null) {
			if (!handlers.containsKey(pipe.getClass().getName())) {
				handlers.put(pipe.getClass().getName(),
						new RPCHandler(pipe.getClass()));
			}

			handlers.get(pipe.getClass().getName()).internalRpcReceive(pipe,
					info, data);
		}
	}

	private PacketRPCPipe createRCPPacket (Pipe pipe, String method, Object ... actuals) {
		ByteBuf data = Unpooled.buffer();

		try {
			TileEntity tile = pipe.container;

			// In order to save space on message, we assuming dimensions ids
			// small. Maybe worth using a varint instead
			data.writeShort(tile.getWorldObj().provider.dimensionId);
			data.writeInt(tile.xCoord);
			data.writeInt(tile.yCoord);
			data.writeInt(tile.zCoord);

			writeParameters(method, data, actuals);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		byte [] bytes = new byte [data.readableBytes()];
		data.readBytes(bytes);

		return new PacketRPCPipe(bytes);
	}

	private PacketRPCTile createRCPPacket (TileEntity tile, String method, Object ... actuals) {
		ByteBuf data = Unpooled.buffer();

		try {
			writeParameters(method, data, actuals);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		byte [] bytes = new byte [data.readableBytes()];
		data.readBytes(bytes);

		return new PacketRPCTile(tile, bytes);
	}

	private void writeParameters(String method, ByteBuf data, Object... actuals)
			throws IOException, IllegalArgumentException,
			IllegalAccessException {
		if (!methodsMap.containsKey(method)) {
			throw new RuntimeException(method + " is not a callable method of "
					+ getClass().getName());
		}

		int methodIndex = methodsMap.get(method);
		MethodMapping m = methods[methodIndex];
		Class[] formals = m.parameters;

		int expectedParameters = m.hasInfo ? formals.length - 1
				: formals.length;

		if (expectedParameters != actuals.length) {
			// We accept formals + 1 as an argument, in order to support the
			// special last argument RPCMessageInfo

			throw new RuntimeException(getClass().getName() + "." + method
					+ " expects " + m.parameters.length + " parameters, not "
					+ actuals.length);
		}

		data.writeShort(methodIndex);

		SerializationContext context = new SerializationContext();

		for (int i = 0; i < actuals.length; ++i) {
			if (int.class.equals(formals[i])) {
				data.writeInt((Integer) actuals[i]);
			} else if (float.class.equals(formals[i])) {
				data.writeFloat((Float) actuals[i]);
			} else if (char.class.equals(formals[i])) {
				data.writeChar((Character) actuals[i]);
			} else {
				m.mappings[i].write(data, actuals[i], context);
			}
		}
	}

	private void internalRpcReceive (Object o, RPCMessageInfo info, ByteBuf data) {
		try {
			short methodIndex = data.readShort();

			MethodMapping m = methods [methodIndex];
			Class[] formals = m.parameters;

			Object [] actuals = new Object [formals.length];

			int expectedParameters = m.hasInfo ? formals.length - 1 : formals.length;

			SerializationContext context = new SerializationContext();

			for (int i = 0; i < expectedParameters; ++i) {
				if (int.class.equals(formals[i])) {
					actuals [i] = data.readInt();
				} else if (float.class.equals(formals[i])) {
					actuals [i] = data.readFloat();
				} else if (char.class.equals(formals[i])) {
					actuals [i] = data.readChar();
				} else {
					actuals [i] = m.mappings [i].read (data, actuals [i], context);
				}
			}

			if (m.hasInfo) {
				actuals [actuals.length - 1] = info;
			}

			m.method.invoke(o, actuals);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

}
