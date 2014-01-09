/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.network;

import buildcraft.BuildCraftCore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

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
	private LinkedList<Field> floatFields = new LinkedList<Field>();
	private LinkedList<Field> doubleFields = new LinkedList<Field>();
	private LinkedList<Field> stringFields = new LinkedList<Field>();
	private LinkedList<Field> shortFields = new LinkedList<Field>();
	private LinkedList<Field> intFields = new LinkedList<Field>();
	private LinkedList<Field> booleanFields = new LinkedList<Field>();
	private LinkedList<Field> enumFields = new LinkedList<Field>();
	private LinkedList<ClassMapping> objectFields = new LinkedList<ClassMapping>();

	private LinkedList<Field> byteArrayFields = new LinkedList<Field>();
	private LinkedList<Field> doubleArrayFields = new LinkedList<Field>();
	private LinkedList<Field> shortArrayFields = new LinkedList<Field>();
	private LinkedList<Field> intArrayFields = new LinkedList<Field>();
	private LinkedList<Field> booleanArrayFields = new LinkedList<Field>();
	private LinkedList<Field> stringArrayFields = new LinkedList<Field>();
	private LinkedList<ClassMapping> objectArrayFields = new LinkedList<ClassMapping>();

	private Field field;

	private Class<? extends Object> clas;

	private static Map <String, ClassMapping> classes = new TreeMap <String, ClassMapping> ();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ClassMapping(final Class<? extends Object> c) {
		clas = c;
		Field[] fields = c.getFields();

		try {
			for (Field f : fields) {
				if (!isSynchronizedField(f)) {
					continue;
				}

				Type t = f.getGenericType();

				// ??? take into account enumerations here!

				if (t instanceof Class && !((Class) t).isArray()) {
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
					} else {
						// ADD SOME SAFETY HERE - if we're not child of Object

						ClassMapping mapping = new ClassMapping(fieldClass);
						mapping.field = f;

						objectFields.add(mapping);
					}
				}

				if (t instanceof Class && ((Class) t).isArray()) {
					Class fieldClass = (Class) t;
					Class cptClass = fieldClass.getComponentType();

					if (cptClass.equals(byte.class)) {
						byteArrayFields.add(f);
					} else if (cptClass.equals(double.class)) {
						doubleArrayFields.add(f);
					} else if (cptClass.equals(short.class)) {
						shortArrayFields.add(f);
					} else if (cptClass.equals(int.class)) {
						intArrayFields.add(f);
					} else if (cptClass.equals(String.class)) {
						stringArrayFields.add(f);
					} else if (cptClass.equals(boolean.class)) {
						booleanArrayFields.add(f);
					} else {
						// ADD SOME SAFETY HERE - if we're not child of Object

						ClassMapping mapping = new ClassMapping(cptClass);
						mapping.field = f;
						objectArrayFields.add(mapping);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isSynchronizedField(Field f) {
		NetworkData updateAnnotation = f.getAnnotation(NetworkData.class);

		return updateAnnotation != null;
	}

	@SuppressWarnings("rawtypes")
	public void setData(Object obj, DataOutputStream data) throws IllegalArgumentException,
			IllegalAccessException, IOException {

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
			data.writeFloat((float) f.getDouble(obj));
		}

		for (Field f : stringFields) {
			String s = (String) f.get(obj);

			if (s == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				data.writeUTF(s);
			}
		}

		for (ClassMapping c : objectFields) {
			Object cpt = c.field.get(obj);

			if (cpt == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				c.setData(cpt, data);
			}
		}

		for (Field f : byteArrayFields) {
			byte [] val = (byte[]) f.get(obj);

			if (val == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				data.writeInt(val.length);
				data.write(val);
			}
		}

		for (Field f : doubleArrayFields) {
			double [] val = (double[]) f.get(obj);

			if (val == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				data.writeInt(val.length);

				for (int i = 0; i < val.length; ++i) {
					data.writeFloat((float) val [i]);
				}
			}
		}

		for (Field f : shortArrayFields) {
			short [] val = (short[]) f.get(obj);

			if (val == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				data.writeInt(val.length);

				for (int i = 0; i < val.length; ++i) {
					data.writeShort(val [i]);
				}
			}
		}

		for (Field f : intArrayFields) {
			int [] val = (int[]) f.get(obj);

			if (val == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				data.writeInt(val.length);

				for (int i = 0; i < val.length; ++i) {
					data.writeShort(val [i]);
				}
			}
		}

		for (Field f : booleanArrayFields) {
			boolean [] val = (boolean[]) f.get(obj);

			if (val == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				data.writeInt(val.length);

				for (int i = 0; i < val.length; ++i) {
					data.writeBoolean(val [i]);
				}
			}
		}

		for (Field f : stringArrayFields) {
			String [] val = (String[]) f.get(obj);

			if (val == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				data.writeInt(val.length);

				for (int i = 0; i < val.length; ++i) {
					if (val [i] == null) {
						data.writeBoolean(false);
					} else {
						data.writeBoolean(true);
						data.writeUTF(val [i]);
					}
				}
			}
		}

		for (ClassMapping c : objectArrayFields) {
			NetworkData updateAnnotation = c.field.getAnnotation(NetworkData.class);

			Object[] cpts = (Object[]) c.field.get(obj);

			if (cpts == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				data.writeInt(cpts.length);

				for (int i = 0; i < cpts.length; ++i)
					if (cpts[i] == null) {
						data.writeBoolean(false);
					} else {
						data.writeBoolean(true);
						c.setData(cpts[i], data);
					}
			}
		}
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
	 * - class instantiation will be done after the field type, not
	 * the actual type used in serialization. Generally speaking, this system
	 * is not robust to polymorphism
	 * - only public non-final fields can be serialized
	 */
	@SuppressWarnings("rawtypes")
	public void updateFromData(Object obj, DataInputStream data) throws IllegalArgumentException,
			IllegalAccessException, IOException, InstantiationException {

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
			f.setDouble(obj, data.readFloat());
		}

		for (Field f : stringFields) {
			if (data.readBoolean()) {
				f.set(obj, data.readUTF());
			} else {
				f.set(obj, null);
			}
		}

		for (ClassMapping c : objectFields) {
			if (data.readBoolean()) {
				if (c.field.get(obj) == null) {
					c.field.set
					(obj, c.field.getDeclaringClass().newInstance());
				}

				c.updateFromData(c.field.get(obj), data);
			} else {
				c.updateFromData(c.field.get(obj), null);
			}
		}

		for (Field f : byteArrayFields) {
			if (data.readBoolean()) {
				int length = data.readInt();
				byte[] tmp = new byte [length];
				data.read(tmp);
				f.set(obj, tmp);
			} else {
				f.set(obj, null);
			}
		}

		for (Field f : doubleArrayFields) {
			if (data.readBoolean()) {
				int length = data.readInt();
				double[] tmp = new double [length];

				for (int i = 0; i < tmp.length; ++i) {
					tmp [i] = data.readFloat();
				}

				f.set(obj, tmp);
			} else {
				f.set(obj, null);
			}
		}

		for (Field f : shortArrayFields) {
			if (data.readBoolean()) {
				int length = data.readInt();
				short[] tmp = new short [length];

				for (int i = 0; i < tmp.length; ++i) {
					tmp [i] = data.readShort();
				}

				f.set(obj, tmp);
			} else {
				f.set(obj, null);
			}
		}

		for (Field f : intArrayFields) {
			if (data.readBoolean()) {
				int length = data.readInt();
				int[] tmp = new int [length];

				for (int i = 0; i < tmp.length; ++i) {
					tmp [i] = data.readInt();
				}

				f.set(obj, tmp);
			} else {
				f.set(obj, null);
			}
		}

		for (Field f : booleanArrayFields) {
			if (data.readBoolean()) {
				int length = data.readInt();
				boolean[] tmp = new boolean [length];

				for (int i = 0; i < tmp.length; ++i) {
					tmp [i] = data.readBoolean();
				}

				f.set(obj, tmp);
			} else {
				f.set(obj, null);
			}
		}

		for (Field f : stringArrayFields) {
			if (data.readBoolean()) {
				int length = data.readInt();
				String [] tmp = new String [length];

				for (int i = 0; i < tmp.length; ++i) {
					if (data.readBoolean()) {
						tmp [i] = data.readUTF();
					} else {
						tmp [i] = null;
					}
				}

				f.set(obj, tmp);
			} else {
				f.set(obj, null);
			}
		}

		for (ClassMapping c : objectArrayFields) {
			Object[] cpts = (Object[]) c.field.get(obj);

			if (data.readBoolean()) {
				int serializedSize = data.readInt();

				if (serializedSize != cpts.length) {
					throw new IOException
					("Expected size doesn't match serialized size on object array");
				}

				for (int i = 0; i < cpts.length; ++i) {
					boolean isNull = data.readBoolean();

					if (!isNull) {
						if (cpts [i] == null) {
							cpts [i] = c.field.getDeclaringClass().getComponentType().newInstance();
						}

						c.updateFromData(cpts[i], data);
					} else {
						cpts [i] = null;
					}
				}
			} else {
				c.field.set(obj, null);
			}
		}
	}

	public static ClassMapping get (Class clas) {
		if (!classes.containsKey(clas.getCanonicalName())) {
			classes.put(clas.getCanonicalName(), new ClassMapping(clas));
		}

		return classes.get(clas.getCanonicalName());
	}
}
