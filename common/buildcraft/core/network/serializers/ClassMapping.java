/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network.serializers;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.JavaTools;
import buildcraft.api.core.NetworkData;
import buildcraft.core.utils.Utils;

/**
 * This class implements custom class mapping. There are three advantages in
 * using a custom serializer here:
 *
 * (1) the approach is constructive instead of destructive, that is to say,
 * only marked fields will be taken. Granted, this is mostly coding style
 * related, but this prevent introduction of useless serialized data by
 * mistake.
 *
 * (2) we can introduce specific serialized types. For example (although not
 * yet implemented?) we will be able to implement a tile as a reference to
 * this tile through e.g. {x, y, z}, that is know what needs to be serialized,
 * know what needs to be referenced, and how to reference it.
 *
 * (3) again, not yet implemented, but we can in theory have different set
 * of serialization depending on the context.
 *
 * HISTORY NOTE
 *
 * This was initially developed because the initial network framework only
 * allowed for byte, float and int, so more things were needed. To the light
 * of current understanding, using only byte would have been good enough.
 *
 * It seems like the three points above indeed give more value and safety to
 * the whole code and make this system still relevant. To be re-evaluated.
 *
 * QUESTION ON OBJECTS
 *
 * At the moment, we do not support object creation from this interface, so
 * the objects are supposed to be already there and then updated. This may
 * not always make sense, in particular in the context of RPC
 *
 * Non-null arrays of objects are forbidden as well, and they need to be set
 * to the same null and non-null elements on both sides.
 *
 */
public class ClassMapping extends ClassSerializer {

	private static SerializerObject anonymousSerializer = new SerializerObject();
	private static Map<String, ClassSerializer> classes = new TreeMap<String, ClassSerializer>();

	private LinkedList<Field> floatFields = new LinkedList<Field>();
	private LinkedList<Field> doubleFields = new LinkedList<Field>();
	private LinkedList<Field> shortFields = new LinkedList<Field>();
	private LinkedList<Field> intFields = new LinkedList<Field>();
	private LinkedList<Field> booleanFields = new LinkedList<Field>();
	private LinkedList<Field> enumFields = new LinkedList<Field>();

	class FieldObject {
		public Field field;
		public ClassSerializer mapping;
	}

	private LinkedList<FieldObject> objectFields = new LinkedList<FieldObject>();

	enum CptType {
		Byte,
		Float,
		Double,
		Short,
		Int,
		Boolean,
		Object
	}

	private CptType cptType;
	private ClassSerializer cptMapping;

	public ClassMapping() {

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void analyzeClass(final Class<? extends Object> c) {
		try {
			if (c.isArray()) {
				Class cptClass = c.getComponentType();

				if (byte.class.equals(cptClass)) {
					cptType = CptType.Byte;
				} else if (float.class.equals(cptClass)) {
					cptType = CptType.Float;
				} else if (double.class.equals(cptClass)) {
					cptType = CptType.Double;
				} else if (short.class.equals(cptClass)) {
					cptType = CptType.Short;
				} else if (int.class.equals(cptClass)) {
					cptType = CptType.Int;
				} else if (boolean.class.equals(cptClass)) {
					cptType = CptType.Byte;
				} else {
					cptType = CptType.Object;
					cptMapping = get (cptClass);
				}
			} else {
				List<Field> fields = JavaTools.getAllFields(c);

				for (Field f : fields) {
					if (!isSynchronizedField(f)) {
						continue;
					}

					f.setAccessible(true);

					Type t = f.getType();

					if (t instanceof Class) {
						Class fieldClass = (Class) t;

						if (short.class.equals(fieldClass)) {
							shortFields.add(f);
						} else if (int.class.equals(fieldClass)) {
							intFields.add(f);
						} else if (boolean.class.equals(fieldClass)) {
							booleanFields.add(f);
						} else if (Enum.class.isAssignableFrom(fieldClass)) {
							enumFields.add(f);
						} else if (float.class.equals(fieldClass)) {
							floatFields.add(f);
						} else if (double.class.equals(fieldClass)) {
							doubleFields.add(f);
						} else {
							FieldObject obj = new FieldObject();
							obj.mapping = get (fieldClass);
							obj.field = f;

							objectFields.add(obj);
						}
					}
				}

			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	private boolean isSynchronizedField(Field f) {
		NetworkData updateAnnotation = f.getAnnotation(NetworkData.class);

		return updateAnnotation != null;
	}

	/**
	 * This class will update data in an object from a stream. Public data
	 * market #NetworkData will get synchronized. The following rules will
	 * apply:
	 *
	 * In the following description, we consider strings as primitive objects.
	 *
	 * Market primitives data will be directly updated on the destination
	 * object after the value of the source object
	 *
	 * Market primitive arrays will be re-created in the destination object
	 * after the primitive array of the source object. This means that array
	 * references are not preserved by the proccess. If an array is null
	 * in the source array and not in the destination one, it will be turned to
	 * null.
	 *
	 * Market object will be synchronized - that it we do not create new
	 * instances in the destination object if they are already there but rather
	 * recursively synchronize values. If destination is null and not
	 * source, the destination will get the instance created. If destination is
	 * not null and source is, the destination will get truned to null.
	 *
	 * Market object arrays will be synchronized - not re-created. If
	 * destination is null and not source, the destination will get the instance
	 * created. If destination is not null and source is, the destination will
	 * get turned to null. The same behavior applies to the contents of the
	 * array. Trying to synchronize two arrays of different size is an error
	 * and will lead to an exception - so if the array needs to change on the
	 * destination it needs to be set to null first.
	 *
	 * WARNINGS
	 *
	 *  - only public non-final fields can be serialized
	 *  - non static nested classes are not supported
	 *  - no reference analysis is done, e.g. an object referenced twice will
	 *    be serialized twice
	 */
	@Override
	public void write(ByteBuf data, Object o, SerializationContext context) throws IllegalArgumentException, IllegalAccessException {
		if (o == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);

			if (mappedClass.isArray()) {
				writeArray(o, data, context);
			} else {
				writeClass(o, data, context);
			}
		}
	}

	@Override
	public Object read(ByteBuf data, Object o, SerializationContext context)
			throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException {

		if (!data.readBoolean()) {
			return null;
		} else {
			if (mappedClass.isArray()) {
				return readArray(o, data, context);
			} else {
				return readClass(o, data, context);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	void writeClass(Object obj, ByteBuf data, SerializationContext context) throws IllegalArgumentException,
			IllegalAccessException {

		Class realClass = obj.getClass();

		if (realClass.equals(this.mappedClass)) {
			data.writeByte(0);
		} else {
			ClassMapping delegateMapping;

			if (context.classToId.containsKey(realClass.getCanonicalName())) {
				int index = context.classToId.get(realClass.getCanonicalName()) + 1;
				data.writeByte(index);
				delegateMapping = (ClassMapping) context.idToClass.get(index - 1);
			} else {
				int index = context.classToId.size() + 1;
				delegateMapping = (ClassMapping) get(realClass);

				data.writeByte(index);
				Utils.writeUTF(data, realClass.getCanonicalName());
				context.classToId.put(realClass.getCanonicalName(),
						context.classToId.size());
				context.idToClass.add(delegateMapping);
			}

			delegateMapping.writeClass(obj, data, context);

			return;
		}

		for (Field f : shortFields) {
			data.writeShort(f.getShort(obj));
		}

		for (Field f : intFields) {
			data.writeInt(f.getInt(obj));
		}

		for (Field f : booleanFields) {
			data.writeBoolean(f.getBoolean(obj));
		}

		for (Field f : enumFields) {
			data.writeByte(((Enum) f.get(obj)).ordinal());
		}

		for (Field f : floatFields) {
			data.writeFloat(f.getFloat(obj));
		}

		for (Field f : doubleFields) {
			data.writeDouble(f.getDouble(obj));
		}

		for (FieldObject f : objectFields) {
			Object cpt = f.field.get(obj);
			f.mapping.write(data, cpt, context);
		}
	}

	@SuppressWarnings("rawtypes")
	Object readClass(Object objI, ByteBuf data, SerializationContext context) throws IllegalArgumentException,
			IllegalAccessException, InstantiationException, ClassNotFoundException {

		Object obj = objI;

		// The data layout for an object is the following:
		// [boolean] does the object exist (e.g. non-null)
		// {false} exit
		// [int] what is the object real class?
		// {0} the same as the declared class
		// {1-x} a different one
		// [string] if the number is not yet registered, the name of the
		// class
		// [bytes] the actual contents

		int index = data.readByte();

		if (index != 0) {
			ClassMapping delegateMapping;

			if (context.idToClass.size() < index) {
				String className = Utils.readUTF(data);

				Class cls = Class.forName(className);

				delegateMapping = (ClassMapping) get(cls);

				context.idToClass.add(get(cls));
			} else {
				delegateMapping = (ClassMapping) context.idToClass.get(index - 1);
			}

			return delegateMapping.readClass(obj, data, context);
		}

		if (obj == null) {
			obj = mappedClass.newInstance();
		}

		for (Field f : shortFields) {
			f.setShort(obj, data.readShort());
		}

		for (Field f : intFields) {
			f.setInt(obj, data.readInt());
		}

		for (Field f : booleanFields) {
			f.setBoolean(obj, data.readBoolean());
		}

		for (Field f : enumFields) {
			f.set(obj, ((Class) f.getGenericType()).getEnumConstants()[data.readByte()]);
		}

		for (Field f : floatFields) {
			f.setFloat(obj, data.readFloat());
		}

		for (Field f : doubleFields) {
			f.setDouble(obj, data.readDouble());
		}

		for (FieldObject f : objectFields) {
			f.field.set(obj, f.mapping.read(data, f.field.get(obj), context));
		}

		return obj;
	}

	private void writeArray(Object obj, ByteBuf data, SerializationContext context) throws IllegalArgumentException,
	IllegalAccessException {
		Class<? extends Object> cpt = mappedClass.getComponentType();

		switch (cptType) {
			case Byte: {
				byte [] arr = (byte []) obj;
				data.writeInt (arr.length);

				data.writeBytes(arr);

				break;
			}
			case Float: {
				float [] arr = (float []) obj;
				data.writeInt (arr.length);

				for (float element : arr) {
					data.writeFloat(element);
				}

				break;
			}
			case Double: {
				double [] arr = (double []) obj;
				data.writeInt (arr.length);

				for (double element : arr) {
					data.writeDouble(element);
				}

				break;
			}
			case Short: {
				short [] arr = (short []) obj;
				data.writeInt (arr.length);

				for (short element : arr) {
					data.writeShort(element);
				}

				break;
			}
			case Int: {
				int [] arr = (int []) obj;
				data.writeInt (arr.length);

				for (int element : arr) {
					data.writeInt(element);
				}

				break;
			}
			case Boolean: {
				boolean [] arr = (boolean []) obj;
				data.writeInt (arr.length);

				for (boolean element : arr) {
					data.writeBoolean(element);
				}

				break;
			}
			case Object: {
				Object [] arr = (Object []) obj;
				data.writeInt (arr.length);

				for (Object element : arr) {
					cptMapping.write(data, element, context);
				}

				break;
			}
		}
	}

	private Object readArray(Object objI, ByteBuf data, SerializationContext context) throws IllegalArgumentException,
	IllegalAccessException, InstantiationException, ClassNotFoundException {
		Object obj = objI;

		Class<? extends Object> cpt = mappedClass.getComponentType();

		int size = data.readInt();

		switch (cptType) {
			case Byte: {
				byte [] arr;

				if (obj == null) {
					arr = new byte [size];
				} else {
					arr = (byte []) obj;
				}

				data.readBytes (arr);

				obj = arr;

				break;
			}
			case Float: {
				float [] arr;

				if (obj == null) {
					arr = new float [size];
				} else {
					arr = (float []) obj;
				}

				for (int i = 0; i < arr.length; ++i) {
					arr [i] = data.readFloat();
				}

				obj = arr;

				break;
			}
			case Double: {
				double [] arr;

				if (obj == null) {
					arr = new double [size];
				} else {
					arr = (double []) obj;
				}

				for (int i = 0; i < arr.length; ++i) {
					arr [i] = data.readDouble();
				}

				obj = arr;

				break;
			}
			case Short: {
				short [] arr;

				if (obj == null) {
					arr = new short [size];
				} else {
					arr = (short []) obj;
				}

				for (int i = 0; i < arr.length; ++i) {
					arr [i] = data.readShort();
				}

				obj = arr;

				break;
			}
			case Int: {
				int [] arr;

				if (obj == null) {
					arr = new int [size];
				} else {
					arr = (int []) obj;
				}

				for (int i = 0; i < arr.length; ++i) {
					arr [i] = data.readInt();
				}

				obj = arr;

				break;
			}
			case Boolean: {
				boolean [] arr;

				if (obj == null) {
					arr = new boolean [size];
				} else {
					arr = (boolean []) obj;
				}

				for (int i = 0; i < arr.length; ++i) {
					arr [i] = data.readBoolean();
				}

				obj = arr;

				break;
			}
			case Object: {
				Object [] arr;

				if (obj == null) {
					arr = (Object[]) Array.newInstance(cpt, size);
				} else {
					arr = (Object []) obj;
				}

				for (int i = 0; i < arr.length; ++i) {
					arr [i] = cptMapping.read(data, arr[i], context);
				}

				obj = arr;

				break;
			}
		}

		return obj;
	}

	private static void registerSerializer (Class clas, ClassSerializer s) {
		try {
			s.mappedClass = clas;
			classes.put(clas.getCanonicalName(), s);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("Can't register " + clas.getCanonicalName() + " in serializers");
		}
	}

	public static ClassSerializer get (Class clas) {
		ClassSerializer mapping;

		if (Block.class.isAssignableFrom(clas)) {
			mapping = classes.get(Block.class.getCanonicalName());
		} else if (Item.class.isAssignableFrom(clas)) {
			mapping = classes.get(Item.class.getCanonicalName());
		} else if (!classes.containsKey(clas.getCanonicalName())) {
			mapping = new ClassMapping ();
			registerSerializer(clas, mapping);
			((ClassMapping) mapping).analyzeClass(clas);
		} else {
			mapping = classes.get(clas.getCanonicalName());
		}

		return mapping;
	}

	static {
		registerSerializer(String.class, new SerializerString());
		registerSerializer(HashMap.class, new SerializerHashMap());
		registerSerializer(LinkedList.class, new SerializerLinkedList());
		registerSerializer(ArrayList.class, new SerializerArrayList());
		registerSerializer(Block.class, new SerializerBlock());
		registerSerializer(Item.class, new SerializerItem());
		registerSerializer(NBTTagCompound.class, new SerializerNBT());
		registerSerializer(ItemStack.class, new SerializerItemStack());
		registerSerializer(FluidStack.class, new SerializerFluidStack());
		registerSerializer(Integer.class, new SerializerInteger());
	}
}
