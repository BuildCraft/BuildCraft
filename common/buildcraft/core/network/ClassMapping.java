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
import buildcraft.core.ByteBuffer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.TreeMap;

public class ClassMapping {

	public static class Reporter {

		Class<? extends Object> clas;
		int occurences = 0;
		int dataInt = 0;
		int dataFloat = 0;
		int dataString = 0;
		int bytes = 0;

		@Override
		public String toString() {
			String res = clas + ": " + occurences + " times (" + dataInt + ", " + dataFloat + ", " + dataString + " = " + bytes + ")";

			return res;
		}
	}

	private static TreeMap<String, Reporter> report = new TreeMap<String, Reporter>();

	public static int report() {
		int bytes = 0;
		for (Reporter r : report.values()) {
			System.out.println(r);
			bytes += r.bytes;
		}

		report.clear();

		return bytes;
	}

	private LinkedList<Field> floatFields = new LinkedList<Field>();
	private LinkedList<Field> doubleFields = new LinkedList<Field>();
	private LinkedList<Field> stringFields = new LinkedList<Field>();
	private LinkedList<Field> shortFields = new LinkedList<Field>();
	private LinkedList<Field> intFields = new LinkedList<Field>();
	private LinkedList<Field> booleanFields = new LinkedList<Field>();
	private LinkedList<Field> enumFields = new LinkedList<Field>();
	private LinkedList<Field> unsignedByteFields = new LinkedList<Field>();
	private LinkedList<ClassMapping> objectFields = new LinkedList<ClassMapping>();

	private LinkedList<Field> doubleArrayFields = new LinkedList<Field>();
	private LinkedList<Field> shortArrayFields = new LinkedList<Field>();
	private LinkedList<Field> intArrayFields = new LinkedList<Field>();
	private LinkedList<Field> booleanArrayFields = new LinkedList<Field>();
	private LinkedList<Field> unsignedByteArrayFields = new LinkedList<Field>();
	private LinkedList<Field> stringArrayFields = new LinkedList<Field>();
	private LinkedList<ClassMapping> objectArrayFields = new LinkedList<ClassMapping>();

	private int sizeBytes;
	private int sizeFloat;
	private int sizeString;

	private Field field;

	private Class<? extends Object> clas;

	public static class Indexes {

		public Indexes(int initFloat, int initString) {
			floatIndex = initFloat;
			stringIndex = initString;
		}

		int floatIndex = 0;
		int stringIndex = 0;
	}

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
						sizeBytes += 2;
						shortFields.add(f);
					} else if (fieldClass.equals(int.class)) {
						TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

						if (updateAnnotation.intKind() == TileNetworkData.UNSIGNED_BYTE) {
							sizeBytes += 1;
							unsignedByteFields.add(f);
						} else {
							sizeBytes += 4;
							intFields.add(f);
						}
					} else if (fieldClass.equals(boolean.class)) {
						sizeBytes += 1;
						booleanFields.add(f);
					} else if (Enum.class.isAssignableFrom(fieldClass)) {
						sizeBytes += 1;
						enumFields.add(f);
					} else if (fieldClass.equals(String.class)) {
						sizeString++;
						stringFields.add(f);
					} else if (fieldClass.equals(float.class)) {
						sizeFloat++;
						floatFields.add(f);
					} else if (fieldClass.equals(double.class)) {
						sizeFloat++;
						doubleFields.add(f);
					} else {
						// ADD SOME SAFETY HERE - if we're not child of Object

						ClassMapping mapping = new ClassMapping(fieldClass);
						mapping.field = f;

						objectFields.add(mapping);
						sizeBytes += 1; // to catch null / not null.

						sizeBytes += mapping.sizeBytes;
						sizeFloat += mapping.sizeFloat;
						sizeString += mapping.sizeString;
					}
				}
				if (t instanceof Class && ((Class) t).isArray()) {
					TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

					if (updateAnnotation.staticSize() == -1)
						throw new RuntimeException("arrays must be provided with an explicit size");

					Class fieldClass = (Class) t;

					Class cptClass = fieldClass.getComponentType();

					if (cptClass.equals(double.class)) {
						sizeFloat += updateAnnotation.staticSize();
						doubleArrayFields.add(f);
					} else if (cptClass.equals(short.class)) {
						sizeBytes += updateAnnotation.staticSize() * 2;
						shortArrayFields.add(f);
					} else if (cptClass.equals(int.class)) {
						updateAnnotation = f.getAnnotation(TileNetworkData.class);

						if (updateAnnotation.intKind() == TileNetworkData.UNSIGNED_BYTE) {
							sizeBytes += updateAnnotation.staticSize();
							unsignedByteArrayFields.add(f);
						} else {
							sizeBytes += updateAnnotation.staticSize() * 4;
							intArrayFields.add(f);
						}
					} else if (cptClass.equals(String.class)) {
						sizeString += updateAnnotation.staticSize();
						stringArrayFields.add(f);
					} else if (cptClass.equals(boolean.class)) {
						sizeBytes += updateAnnotation.staticSize();
						booleanArrayFields.add(f);
					} else {
						// ADD SOME SAFETY HERE - if we're not child of Object

						ClassMapping mapping = new ClassMapping(cptClass);
						mapping.field = f;
						objectArrayFields.add(mapping);

						sizeBytes += updateAnnotation.staticSize(); // to catch
																	// null /
																	// not null.

						sizeBytes += updateAnnotation.staticSize() * mapping.sizeBytes;
						sizeFloat += updateAnnotation.staticSize() * mapping.sizeFloat;
						sizeString += updateAnnotation.staticSize() * mapping.sizeString;
					}
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isSynchronizedField(Field f) {
		TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

		return updateAnnotation != null;
	}

	@SuppressWarnings("rawtypes")
	public void setData(Object obj, ByteBuffer byteBuffer, float[] floatValues, String[] stringValues, Indexes index) throws IllegalArgumentException,
			IllegalAccessException {

		Reporter r = null;

		if (BuildCraftCore.trackNetworkUsage) {
			if (!report.containsKey(clas.getName())) {
				report.put(clas.getName(), new Reporter());
			}

			r = report.get(clas.getName());
			r.clas = clas;
		} else {
			r = new Reporter();
		}

		r.occurences++;

		for (Field f : shortFields) {
			byteBuffer.writeShort(f.getShort(obj));
			r.bytes += 2;
			r.dataInt += 1;
		}

		for (Field f : intFields) {
			byteBuffer.writeInt(f.getInt(obj));
			r.bytes += 4;
			r.dataInt += 1;
		}

		for (Field f : booleanFields) {
			byteBuffer.writeUnsignedByte(f.getBoolean(obj) ? 1 : 0);
			r.bytes += 1;
			r.dataInt += 1;
		}

		for (Field f : enumFields) {
			byteBuffer.writeUnsignedByte(((Enum) f.get(obj)).ordinal());
			r.bytes += 1;
			r.dataInt += 1;
		}

		for (Field f : unsignedByteFields) {
			byteBuffer.writeUnsignedByte(f.getInt(obj));
			r.bytes += 1;
			r.dataInt += 1;
		}

		for (Field f : floatFields) {
			floatValues[index.floatIndex] = f.getFloat(obj);
			index.floatIndex++;
			r.bytes += 4;
			r.dataFloat += 1;
		}

		for (Field f : doubleFields) {
			floatValues[index.floatIndex] = (float) f.getDouble(obj);
			index.floatIndex++;
			r.bytes += 4;
			r.dataFloat += 1;
		}

		for (Field f : stringFields) {
			stringValues[index.stringIndex] = (String) f.get(obj);
			r.bytes += stringValues[index.stringIndex].length();
			index.stringIndex++;
			r.dataString += 1;
		}

		for (ClassMapping c : objectFields) {
			Object cpt = c.field.get(obj);

			if (cpt == null) {
				byteBuffer.writeUnsignedByte(0);

				for (int i = 0; i < c.sizeBytes; ++i) {
					byteBuffer.writeUnsignedByte(0);
					r.bytes += 1;
					r.dataInt += 1;
				}

				index.floatIndex += c.sizeFloat;
				index.stringIndex += c.sizeString;
			} else {
				byteBuffer.writeUnsignedByte(1);
				r.bytes += 1;
				r.dataInt += 1;

				c.setData(cpt, byteBuffer, floatValues, stringValues, index);
			}
		}

		for (Field f : doubleArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				floatValues[index.floatIndex] = (float) ((double[]) f.get(obj))[i];
				index.floatIndex++;
				r.bytes += 4;
				r.dataFloat += 1;
			}
		}

		for (Field f : shortArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				byteBuffer.writeShort(((short[]) f.get(obj))[i]);
				r.bytes += 2;
				r.dataInt += 1;
			}
		}

		for (Field f : intArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				byteBuffer.writeInt(((int[]) f.get(obj))[i]);
				r.bytes += 4;
				r.dataInt += 1;
			}
		}

		for (Field f : booleanArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				byteBuffer.writeUnsignedByte(((boolean[]) f.get(obj))[i] ? 1 : 0);
				r.bytes += 1;
				r.dataInt += 1;
			}
		}

		for (Field f : unsignedByteFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				byteBuffer.writeUnsignedByte(((int[]) f.get(obj))[i]);
				r.bytes += 1;
				r.dataInt += 1;
			}
		}

		for (Field f : stringArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				stringValues[index.stringIndex] = ((String[]) f.get(obj))[i];
				r.bytes += stringValues[index.stringIndex].length();
				index.stringIndex++;
				r.dataString += 1;
			}
		}

		for (ClassMapping c : objectArrayFields) {
			TileNetworkData updateAnnotation = c.field.getAnnotation(TileNetworkData.class);

			Object[] cpts = (Object[]) c.field.get(obj);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i)
				if (cpts[i] == null) {
					byteBuffer.writeUnsignedByte(0);

					for (int j = 0; j < c.sizeBytes; ++j) {
						byteBuffer.writeUnsignedByte(0);
						r.bytes += 1;
						r.dataInt += 1;
					}

					index.floatIndex += c.sizeFloat;
					index.stringIndex += c.sizeString;
				} else {
					byteBuffer.writeUnsignedByte(1);
					r.bytes += 1;
					r.dataInt += 1;

					c.setData(cpts[i], byteBuffer, floatValues, stringValues, index);
				}
		}
	}

	@SuppressWarnings("rawtypes")
	public void updateFromData(Object obj, ByteBuffer byteBuffer, float[] floatValues, String[] stringValues, Indexes index) throws IllegalArgumentException,
			IllegalAccessException {

		Reporter r = null;

		if (BuildCraftCore.trackNetworkUsage) {
			if (!report.containsKey(clas.getName())) {
				report.put(clas.getName(), new Reporter());
			}

			r = report.get(clas.getName());
			r.clas = clas;
		} else {
			r = new Reporter();
		}

		r.occurences++;

		for (Field f : shortFields) {
			f.setShort(obj, byteBuffer.readShort());
			r.bytes += 2;
			r.dataInt += 1;
		}

		for (Field f : intFields) {
			f.setInt(obj, byteBuffer.readInt());
			r.bytes += 4;
			r.dataInt += 1;
		}

		for (Field f : booleanFields) {
			f.setBoolean(obj, byteBuffer.readUnsignedByte() == 1);
			r.bytes += 1;
			r.dataInt += 1;
		}

		for (Field f : enumFields) {
			f.set(obj, ((Class) f.getGenericType()).getEnumConstants()[byteBuffer.readUnsignedByte()]);
			r.bytes += 1;
			r.dataInt += 1;
		}

		for (Field f : unsignedByteFields) {
			f.setInt(obj, byteBuffer.readUnsignedByte());
			r.bytes += 1;
			r.dataInt += 1;
		}

		for (Field f : floatFields) {
			f.setFloat(obj, floatValues[index.floatIndex]);
			index.floatIndex++;
			r.bytes += 4;
			r.dataFloat += 1;
		}

		for (Field f : doubleFields) {
			f.setDouble(obj, floatValues[index.floatIndex]);
			index.floatIndex++;
			r.bytes += 4;
			r.dataFloat += 1;
		}

		for (Field f : stringFields) {
			f.set(obj, stringValues[index.stringIndex]);
			r.bytes += stringValues[index.stringIndex].length();
			index.stringIndex++;
			r.dataString += 1;
		}

		for (ClassMapping c : objectFields) {
			boolean isNull = byteBuffer.readUnsignedByte() == 0;
			r.bytes += 1;
			r.dataInt += 1;

			if (isNull) {
				for (int i = 0; i < c.sizeBytes; ++i) {
					byteBuffer.readUnsignedByte();
				}

				index.floatIndex += c.sizeFloat;
				index.stringIndex += c.sizeString;
			} else {
				c.updateFromData(c.field.get(obj), byteBuffer, floatValues, stringValues, index);
			}
		}

		for (Field f : doubleArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				((double[]) f.get(obj))[i] = floatValues[index.floatIndex];
				index.floatIndex++;
				r.bytes += 4;
				r.dataFloat += 1;
			}
		}

		for (Field f : shortArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				((short[]) f.get(obj))[i] = byteBuffer.readShort();
				r.bytes += 2;
				r.dataInt += 1;

			}
		}

		for (Field f : intArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				((int[]) f.get(obj))[i] = byteBuffer.readInt();
				r.bytes += 4;
				r.dataInt += 1;

			}
		}

		for (Field f : booleanArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				((boolean[]) f.get(obj))[i] = byteBuffer.readUnsignedByte() == 1;
				r.bytes += 1;
				r.dataInt += 1;
			}
		}

		for (Field f : unsignedByteArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				((int[]) f.get(obj))[i] = byteBuffer.readUnsignedByte();
				r.bytes += 1;
				r.dataInt += 1;
			}
		}

		for (Field f : stringArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);

			String[] strs = (String[]) f.get(obj);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				strs[i] = stringValues[index.stringIndex];
				r.bytes += stringValues[index.stringIndex].length();
				index.stringIndex++;
				r.dataString += 1;
			}
		}

		for (ClassMapping c : objectArrayFields) {
			TileNetworkData updateAnnotation = c.field.getAnnotation(TileNetworkData.class);

			Object[] cpts = (Object[]) c.field.get(obj);

			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				boolean isNull = byteBuffer.readUnsignedByte() == 0;
				r.bytes += 1;
				r.dataInt += 1;

				if (isNull) {
					for (int j = 0; j < c.sizeBytes; ++j) {
						byteBuffer.readUnsignedByte();
					}

					index.floatIndex += c.sizeFloat;
					index.stringIndex += c.sizeString;
				} else {
					c.updateFromData(cpts[i], byteBuffer, floatValues, stringValues, index);
				}
			}
		}
	}

	public int[] getSize() {
		int[] result = new int[3];

		result[0] = sizeBytes;
		result[1] = sizeFloat;
		result[2] = sizeString;

		return result;
	}
}
