/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import buildcraft.core.utils.Utils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

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
public class ClassMapping {

	private class SerializationContext {
		public ArrayList<ClassMapping> idToClass = new ArrayList<ClassMapping> ();
		public Map <String, Integer> classToId = new TreeMap<String, Integer> ();
	}

	private LinkedList<Field> floatFields = new LinkedList<Field>();
	private LinkedList<Field> doubleFields = new LinkedList<Field>();
	private LinkedList<Field> stringFields = new LinkedList<Field>();
	private LinkedList<Field> shortFields = new LinkedList<Field>();
	private LinkedList<Field> intFields = new LinkedList<Field>();
	private LinkedList<Field> booleanFields = new LinkedList<Field>();
	private LinkedList<Field> enumFields = new LinkedList<Field>();
	private LinkedList<Field> nbtFields = new LinkedList<Field>();

	class FieldObject {
		public Field field;
		public ClassMapping mapping;
	}

	private LinkedList<FieldObject> objectFields = new LinkedList<FieldObject>();

	private Class<? extends Object> mappedClass;

	enum CptType {
		Byte,
		Float,
		Double,
		String,
		Short,
		Int,
		Boolean,
		Object
	}

	private CptType cptType;
	private ClassMapping cptMapping;

	private static Map <String, ClassMapping> classes = new TreeMap <String, ClassMapping> ();

	public ClassMapping() {

	}

	public ClassMapping(final Class<? extends Object> c) {
		analyzeClass (c);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void analyzeClass(final Class<? extends Object> c) {
		try {
			mappedClass = c;

			if (c.isArray()) {
				Class cptClass = c.getComponentType();

				if (cptClass.equals(byte.class)) {
					cptType = CptType.Byte;
				} else if (cptClass.equals(float.class)) {
					cptType = CptType.Float;
				} else if (cptClass.equals(double.class)) {
					cptType = CptType.Double;
				} else if (cptClass.equals(short.class)) {
					cptType = CptType.Short;
				} else if (cptClass.equals(int.class)) {
					cptType = CptType.Int;
				} else if (cptClass.equals(String.class)) {
					cptType = CptType.String;
				} else if (cptClass.equals(boolean.class)) {
					cptType = CptType.Byte;
				} else {
					cptType = CptType.Object;
					cptMapping = get (cptClass);
				}
			} else {
				Field[] fields = c.getFields();

				for (Field f : fields) {
					if (!isSynchronizedField(f)) {
						continue;
					}

					Type t = f.getGenericType();

					if (t instanceof Class) {
						Class fieldClass = (Class) t;

						if (fieldClass.equals(short.class)) {
							shortFields.add(f);
						} else if (fieldClass.equals(int.class)) {
							intFields.add(f);
						} else if (fieldClass.equals(boolean.class)) {
							booleanFields.add(f);
						} else if (Enum.class.isAssignableFrom(fieldClass)) {
							enumFields.add(f);
						} else if (fieldClass.equals(String.class)) {
							stringFields.add(f);
						} else if (fieldClass.equals(float.class)) {
							floatFields.add(f);
						} else if (fieldClass.equals(double.class)) {
							doubleFields.add(f);
						} else if (NBTBase.class.isAssignableFrom(fieldClass)) {
							nbtFields.add(f);
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


	public void setData(Object obj, ByteBuf data) throws IllegalArgumentException,
	IllegalAccessException {
		SerializationContext context = new SerializationContext();

		setDataInt(obj, data, context);
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
	 *
	 * @throws ClassNotFoundException
	 */
	public Object updateFromData (Object obj, ByteBuf data) throws IllegalArgumentException,
	IllegalAccessException, InstantiationException, ClassNotFoundException {
		SerializationContext context = new SerializationContext();

		return updateFromDataInt(obj, data, context);
	}

	public void setDataInt(Object obj, ByteBuf data, SerializationContext context) throws IllegalArgumentException,
	IllegalAccessException {
		if (mappedClass.isArray()) {
			setDataArray(obj, data, context);
		} else {
			setDataClass(obj, data, context);
		}
	}

	private Object updateFromDataInt (Object obj, ByteBuf data, SerializationContext context) throws IllegalArgumentException,
	IllegalAccessException, InstantiationException, ClassNotFoundException {
		if (mappedClass.isArray()) {
			return updateFromDataArray(obj, data, context);
		} else {
			return updateFromDataClass(obj, data, context);
		}
	}

	@SuppressWarnings("rawtypes")
	private void setDataClass(Object obj, ByteBuf data, SerializationContext context) throws IllegalArgumentException,
			IllegalAccessException {

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
			data.writeDouble((double) f.getDouble(obj));
		}

		for (Field f : stringFields) {
			String s = (String) f.get(obj);

			if (s == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				Utils.writeUTF(data, s);
			}
		}

		for (Field f : nbtFields) {
			NBTTagCompound nbt = (NBTTagCompound) f.get(obj);

			if (nbt == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				Utils.writeNBT(data, nbt);
			}
		}

		for (FieldObject f : objectFields) {
			Object cpt = f.field.get(obj);

			if (cpt == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);

				setDataObject(cpt, f.mapping, data, context);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public Object updateFromDataClass(Object obj, ByteBuf data, SerializationContext context) throws IllegalArgumentException,
			IllegalAccessException, InstantiationException, ClassNotFoundException {

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

		for (Field f : stringFields) {
			if (data.readBoolean()) {
				f.set(obj, Utils.readUTF(data));
			} else {
				f.set(obj, null);
			}
		}

		for (Field f : nbtFields) {
			if (data.readBoolean()) {
				f.set(obj, Utils.readNBT(data));
			} else {
				f.set(obj, null);
			}
		}

		for (FieldObject f : objectFields) {
			if (data.readBoolean()) {
				f.field.set(
						obj,
						updateFromDataObject(f.field.get(obj), f.mapping,
								data, context));
			} else {
				f.field.set(obj, null);
			}
		}

		return obj;
	}

	private void setDataArray(Object obj, ByteBuf data, SerializationContext context) throws IllegalArgumentException,
	IllegalAccessException {
		Class<? extends Object> cpt = mappedClass.getComponentType();

		switch (cptType) {
			case Byte: {
				byte [] arr = (byte []) obj;
				data.writeInt (arr.length);

				for (int i = 0; i < arr.length; ++i) {
					data.writeByte(arr [i]);
				}

				break;
			}
			case Float: {
				float [] arr = (float []) obj;
				data.writeInt (arr.length);

				for (int i = 0; i < arr.length; ++i) {
					data.writeFloat(arr [i]);
				}

				break;
			}
			case Double: {
				double [] arr = (double []) obj;
				data.writeInt (arr.length);

				for (int i = 0; i < arr.length; ++i) {
					data.writeDouble(arr [i]);
				}

				break;
			}
			case String: {
				String [] arr = (String []) obj;
				data.writeInt (arr.length);

				for (int i = 0; i < arr.length; ++i) {
					if (arr [i] == null) {
						data.writeBoolean(false);
					} else {
						data.writeBoolean(true);
						Utils.writeUTF(data, arr [i]);
					}
				}

				break;
			}
			case Short: {
				short [] arr = (short []) obj;
				data.writeInt (arr.length);

				for (int i = 0; i < arr.length; ++i) {
					data.writeShort(arr [i]);
				}

				break;
			}
			case Int: {
				int [] arr = (int []) obj;
				data.writeInt (arr.length);

				for (int i = 0; i < arr.length; ++i) {
					data.writeInt(arr [i]);
				}

				break;
			}
			case Boolean: {
				boolean [] arr = (boolean []) obj;
				data.writeInt (arr.length);

				for (int i = 0; i < arr.length; ++i) {
					data.writeBoolean(arr [i]);
				}

				break;
			}
			case Object: {
				Object [] arr = (Object []) obj;
				data.writeInt (arr.length);

				for (int i = 0; i < arr.length; ++i) {
					if (arr [i] == null) {
						data.writeBoolean(false);
					} else {
						data.writeBoolean(true);

						if (cptMapping.mappedClass.isArray()) {
							cptMapping.setDataInt(arr [i], data, context);
						} else {
							setDataObject(arr [i], cptMapping, data, context);
						}
					}
				}

				break;
			}
		}
	}

	private Object updateFromDataArray(Object obj, ByteBuf data, SerializationContext context) throws IllegalArgumentException,
	IllegalAccessException, InstantiationException, ClassNotFoundException {
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

				for (int i = 0; i < arr.length; ++i) {
					arr [i] = data.readByte();
				}

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
			case String: {
				String [] arr;

				if (obj == null) {
					arr = new String [size];
				} else {
					arr = (String []) obj;
				}

				for (int i = 0; i < arr.length; ++i) {
					if (data.readBoolean()) {
						arr [i] = Utils.readUTF(data);
					} else {
						arr [i] = null;
					}
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
					if (data.readBoolean()) {
						if (cptMapping.mappedClass.isArray()) {
							arr [i] = cptMapping.updateFromDataInt(arr[i], data, context);
						} else {
							arr [i] = updateFromDataObject(arr[i], cptMapping, data, context);
						}
					} else {
						arr [i] = null;
					}
				}

				obj = arr;

				break;
			}
		}

		return obj;
	}

	private void setDataObject(Object obj,
			ClassMapping baseMapping, ByteBuf data,
			SerializationContext context) throws IllegalArgumentException,
			IllegalAccessException {
		ClassMapping mapping = baseMapping;

		Class realClass = obj.getClass();

		if (realClass.equals(baseMapping.mappedClass)) {
			data.writeByte(0);
		} else {
			if (context.classToId.containsKey(realClass.getCanonicalName())) {
				int index = context.classToId.get(realClass.getCanonicalName()) + 1;
				data.writeByte(index);
				mapping = context.idToClass.get(index - 1);
			} else {
				int index = context.classToId.size() + 1;
				mapping = get(realClass);

				data.writeByte(index);
				Utils.writeUTF(data, realClass.getCanonicalName());
				context.classToId.put(realClass.getCanonicalName(),
						context.classToId.size());
				context.idToClass.add(mapping);
			}
		}

		mapping.setDataInt(obj, data, context);
	}

	private Object updateFromDataObject(Object obj,
			ClassMapping baseMapping, ByteBuf data,
			SerializationContext context) throws IllegalArgumentException,
			IllegalAccessException, InstantiationException,
			ClassNotFoundException {

		// The data layout for an object is the following:
		// [boolean] does the object exist (e.g. non-null)
		//    {false} exit
		// [int] what is the object real class?
		//    {0} the same as the declared class
		//    {1-x} a different one
		//       [string] if the number is not yet registered, the name of the
		//                class
		// [bytes] the actual contents

		int index = data.readByte();

		ClassMapping mapping = baseMapping;

		if (index != 0) {
			if (context.idToClass.size() < index) {
				String className = Utils.readUTF(data);

				Class cls = Class.forName(className);

				mapping = get(cls);

				context.idToClass.add(get(cls));
			} else {
				mapping = context.idToClass.get(index - 1);
			}
		}

		return mapping.updateFromDataInt(obj, data, context);
	}

	public static ClassMapping get (Class clas) {
		ClassMapping mapping;

		if (!classes.containsKey(clas.getCanonicalName())) {
			mapping = new ClassMapping ();
			classes.put(clas.getCanonicalName(), mapping);
			mapping.analyzeClass(clas);
		} else {
			mapping = classes.get(clas.getCanonicalName());
		}

		return mapping;
	}
}
