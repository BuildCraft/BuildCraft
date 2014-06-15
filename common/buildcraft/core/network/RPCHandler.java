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
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.JavaTools;
import buildcraft.core.DefaultProps;
import buildcraft.core.network.serializers.ClassMapping;
import buildcraft.core.network.serializers.ClassSerializer;
import buildcraft.core.network.serializers.SerializationContext;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;

/**
 * This is a first implementation of a RPC connector, using the regular tile
 * synchronization layers as a communication protocol. As a result, these
 * RPCs must be sent and received by a tile entity.
 */
public final class RPCHandler {

	public static int MAX_PACKET_SIZE = 30 * 1024;

	private static Map<String, RPCHandler> handlers = new TreeMap<String, RPCHandler>();

	private Class<? extends Object> handledClass;

	private Map<String, Integer> methodsMap = new TreeMap<String, Integer>();

	class MethodMapping {
		Method method;
		Class<?>[] parameters;
		ClassSerializer[] mappings;
		boolean hasInfo = false;
	}

	private MethodMapping[] methods;

	private RPCHandler(Class<? extends Object> c) {
		handledClass = c;
		Method[] sortedMethods = JavaTools.getAllMethods(c).toArray(new Method[0]);

		LinkedList<MethodMapping> mappings = new LinkedList<MethodMapping>();

		Arrays.sort(sortedMethods, new Comparator<Method>() {
			@Override
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		LinkedList<Method> rpcMethods = new LinkedList<Method>();

		for (Method sortedMethod : sortedMethods) {
			if (sortedMethod.getAnnotation(RPC.class) != null) {
				sortedMethod.setAccessible(true);
				methodsMap.put(sortedMethod.getName(), rpcMethods.size());
				rpcMethods.add(sortedMethod);

				MethodMapping mapping = new MethodMapping();
				mapping.method = sortedMethod;
				mapping.parameters = sortedMethod.getParameterTypes();
				mapping.mappings = new ClassSerializer[mapping.parameters.length];

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

	public static void rpcServer(Object object, String method, Object... actuals) {
		if (!handlers.containsKey(object.getClass().getName())) {
			handlers.put(object.getClass().getName(), new RPCHandler(object.getClass()));
		}

		BuildCraftPacket packet = createPacket(object, method, actuals);

		if (packet != null) {
			if (packet instanceof PacketRPCTile) {
				for (PacketRPCTile p : ((PacketRPCTile) packet).breakIntoSmallerPackets(MAX_PACKET_SIZE)) {
					BuildCraftCore.instance.sendToServer(p);
				}
			} else {
				BuildCraftCore.instance.sendToServer(packet);
			}
		}
	}

	public static void rpcPlayer(EntityPlayer player, Object object, String method, Object... actuals) {
		if (!handlers.containsKey(object.getClass().getName())) {
			handlers.put(object.getClass().getName(), new RPCHandler(object.getClass()));
		}

		BuildCraftPacket packet = createPacket(object, method, actuals);

		if (packet != null) {
			if (packet instanceof PacketRPCTile) {
				for (PacketRPCTile p : ((PacketRPCTile) packet).breakIntoSmallerPackets(MAX_PACKET_SIZE)) {
					BuildCraftCore.instance.sendToPlayer(player, p);
				}
			} else {
				BuildCraftCore.instance.sendToPlayer(player, packet);
			}
		}
	}

	public static void rpcBroadcastPlayers(World world, Object object, String method, Object... actuals) {
		RPCHandler.rpcBroadcastPlayersAtDistance(world, object, method, DefaultProps.NETWORK_UPDATE_RANGE, actuals);
	}

	public static void rpcBroadcastPlayersAtDistance(World world, Object object, String method, int maxDistance,
			Object... actuals) {
		if (!handlers.containsKey(object.getClass().getName())) {
			handlers.put(object.getClass().getName(), new RPCHandler(object.getClass()));
		}

		BuildCraftPacket packet = createPacket(object, method, actuals);

		if (packet != null) {
			if (packet instanceof PacketRPCTile) {
				TileEntity tile = (TileEntity) object;

				for (PacketRPCTile p : ((PacketRPCTile) packet)
						.breakIntoSmallerPackets(MAX_PACKET_SIZE)) {

					for (Object o : world.playerEntities) {
						EntityPlayerMP player = (EntityPlayerMP) o;

						if (Math.abs(player.posX - tile.xCoord) <= maxDistance
								&& Math.abs(player.posY - tile.yCoord) <= maxDistance
								&& Math.abs(player.posZ - tile.zCoord) <= maxDistance) {
							BuildCraftCore.instance.sendToPlayer(player, p);
						}
					}
				}
			} else {
				for (Object o : world.playerEntities) {
					EntityPlayerMP player = (EntityPlayerMP) o;

					BuildCraftCore.instance.sendToPlayer(player, packet);
				}
			}
		}
	}

	public static void receiveRPC(Object obj, RPCMessageInfo info, ByteBuf data) {
		if (obj != null) {
			if (!handlers.containsKey(obj.getClass().getName())) {
				handlers.put(obj.getClass().getName(),
						new RPCHandler(obj.getClass()));
			}

			handlers.get(obj.getClass().getName()).internalRpcReceive(obj,
					info, data);
		}
	}

	private PacketRPCPipe createRCPPacketPipe(Pipe<?> pipe, String method, Object ... actuals) {
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

	private static BuildCraftPacket createPacket(Object object, String method, Object... actuals) {
		BuildCraftPacket packet = null;

		if (object instanceof Container) {
			packet = handlers.get(object.getClass().getName()).createRCPPacketContainer(method, actuals);
		} else if (object instanceof TileEntity) {
			packet = handlers.get(object.getClass().getName())
					.createRCPPacketTile((TileEntity) object, method, actuals);
		} else if (object instanceof Entity) {
			packet = handlers.get(object.getClass().getName()).createRCPPacketEntity((Entity) object, method, actuals);
		}

		return packet;
	}

	private byte[] getBytes(String method, Object... actuals) {
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

		return bytes;
	}

	private PacketRPCTile createRCPPacketTile(TileEntity tile, String method, Object... actuals) {
		return new PacketRPCTile(tile, getBytes(method, actuals));
	}

	private PacketRPCGui createRCPPacketContainer(String method, Object... actuals) {
		return new PacketRPCGui(getBytes(method, actuals));
	}

	private PacketRPCEntity createRCPPacketEntity(Entity entity, String method, Object... actuals) {
		return new PacketRPCEntity(entity, getBytes(method, actuals));
	}

	private void writeParameters(String method, ByteBuf data, Object... actuals)
			throws IOException, IllegalArgumentException,
			IllegalAccessException {
		if (!methodsMap.containsKey(method)) {
			throw new RuntimeException(method + " is not a callable method of "
					+ handledClass.getName());
		}

		int methodIndex = methodsMap.get(method);
		MethodMapping m = methods[methodIndex];
		Class<?>[] formals = m.parameters;

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
			if (!writePrimitive(data, formals[i], actuals[i])) {
				m.mappings[i].write(data, actuals[i], context);
			}
		}
	}

	private boolean writePrimitive(ByteBuf data, Class<?> formal, Object actual) {
		if (int.class.equals(formal)) {
			data.writeInt((Integer) actual);
		} else if (float.class.equals(formal)) {
			data.writeFloat((Float) actual);
		} else if (double.class.equals(formal)) {
			data.writeDouble((Double) actual);
		} else if (char.class.equals(formal)) {
			data.writeChar((Character) actual);
		} else if (boolean.class.equals(formal)) {
			data.writeBoolean((Boolean) actual);
		} else if (String.class.equals(formal)) {
			Utils.writeUTF(data, (String) actual);
		} else if (formal.isArray()) {
			Object[] array = (Object[]) actual;
			Class<?> componentType = formal.getComponentType();
			data.writeInt(array.length);
			for (int i = 0; i < array.length; i++) {
				writePrimitive(data, componentType, array[i]);
			}
		} else {
			return false;
		}
		return true;
	}

	private void internalRpcReceive (Object o, RPCMessageInfo info, ByteBuf data) {
		try {
			short methodIndex = data.readShort();

			MethodMapping m = methods [methodIndex];
			Class<?>[] formals = m.parameters;

			Object[] actuals = new Object [formals.length];

			int expectedParameters = m.hasInfo ? formals.length - 1 : formals.length;

			SerializationContext context = new SerializationContext();

			for (int i = 0; i < expectedParameters; ++i) {
				if (!readPrimitive(data, formals[i], actuals, i)) {
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

	private boolean readPrimitive(ByteBuf data, Class<?> formal, Object[] actuals, int i) {
		if (int.class.equals(formal)) {
			actuals[i] = data.readInt();
		} else if (float.class.equals(formal)) {
			actuals[i] = data.readFloat();
		} else if (double.class.equals(formal)) {
			actuals[i] = data.readDouble();
		} else if (char.class.equals(formal)) {
			actuals[i] = data.readChar();
		} else if (boolean.class.equals(formal)) {
			actuals[i] = data.readBoolean();
		} else if (String.class.equals(formal)) {
			actuals[i] = Utils.readUTF(data);
		} else if (formal.isArray()) {
			final int size = data.readInt();
			Class<?> componentType = formal.getComponentType();
			Object[] a = (Object[]) Array.newInstance(componentType, size);
			for (int z = 0; z < size; z++) {
				readPrimitive(data, componentType, a, z);
			}
			actuals[i] = a;
		} else {
			return false;
		}
		return true;
	}
}
