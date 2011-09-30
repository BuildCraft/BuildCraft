/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.core;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedList;

import net.minecraft.src.TileEntity;

public class ClassMapping {
	
	private LinkedList<Field> floatFields = new LinkedList<Field>();
	private LinkedList<Field> doubleFields = new LinkedList<Field>();
	private LinkedList<Field> stringFields = new LinkedList<Field>();
	private LinkedList<Field> shortFields = new LinkedList<Field>();
	private LinkedList<Field> intFields = new LinkedList<Field>();
	private LinkedList<Field> booleanFields = new LinkedList<Field>();
	private LinkedList<Field> enumFields = new LinkedList<Field>();
	private LinkedList<ClassMapping> objectFields = new LinkedList<ClassMapping>();
	
	private LinkedList<Field> intArrayFields = new LinkedList<Field>();
	private LinkedList<Field> booleanArrayFields = new LinkedList<Field>();
	private LinkedList<ClassMapping> objectArrayFields = new LinkedList<ClassMapping>();
	
	private int sizeInt;
	private int sizeFloat;
	private int sizeString;
	
	private Field field;
	
	public static class Indexes {
		public Indexes (int initInt, int initFloat, int initString) {
			intIndex = initInt;
			floatIndex = initFloat;
			stringIndex = initString;
		}
		
		int intIndex = 0;
		int floatIndex = 0;
		int stringIndex = 0;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ClassMapping(final Class <? extends TileEntity> c) {
		Field[] fields = c.getFields();

		try {
			for (Field f : fields) {
				if (!isSynchronizedField(f)) {
					continue;
				}
				
				Type t = f.getGenericType();

				// ??? take into account enumerations here!
							
				if (t instanceof Class && !((Class)t).isArray()) {
					Class fieldClass = (Class) t;
					
					if (fieldClass.equals(short.class)) {
						sizeInt++;
						shortFields.add(f);
					} else if (fieldClass.equals(int.class)) {
						sizeInt++;
						intFields.add(f);
					} else if (fieldClass.equals(boolean.class)) {
						sizeInt++;
						booleanFields.add(f);
					} else if (Enum.class.isAssignableFrom(fieldClass)) {
						sizeInt++;
						enumFields.add (f);
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
						sizeInt++; // to catch null / not null.

						sizeInt += mapping.sizeInt;
						sizeFloat += mapping.sizeFloat;
						sizeString += mapping.sizeString;
					}
				} if (t instanceof Class && ((Class)t).isArray()) {
					TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);
					
					if (updateAnnotation.staticSize() == -1) {
						throw new RuntimeException(
								"arrays must be provided with an explicit size");
					}
					
					Class fieldClass = (Class) t;
					
					Class cptClass = fieldClass.getComponentType();

					if (cptClass.equals(int.class)) {
						sizeInt += updateAnnotation.staticSize();
						intArrayFields.add(f);
					} else if (cptClass.equals(boolean.class)) {
						sizeInt += updateAnnotation.staticSize();
						booleanArrayFields.add(f);
					} else {
						// ADD SOME SAFETY HERE - if we're not child of Object

						ClassMapping mapping = new ClassMapping(cptClass);
						mapping.field = f;
						objectArrayFields.add(mapping);

						sizeInt += updateAnnotation.staticSize(); // to catch null / not null.

						sizeInt += updateAnnotation.staticSize() * mapping.sizeInt;
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
	
	private boolean isSynchronizedField (Field f) {
		TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);
		
		return updateAnnotation != null;			
	}
	
	@SuppressWarnings("rawtypes")
	public void setData(Object obj, int[] intValues, float[] floatValues,
			String[] stringValues, Indexes index) throws IllegalArgumentException, IllegalAccessException {				
		
		for (Field f : shortFields) {
			intValues [index.intIndex] = f.getShort(obj);
			index.intIndex++;
		}
		
		for (Field f : intFields) {
			intValues [index.intIndex] = f.getInt(obj);
			index.intIndex++;
		}
		
		for (Field f : booleanFields) {
			intValues [index.intIndex] = f.getBoolean(obj) ? 1 : 0;
			index.intIndex++;
		}
		
		for (Field f : enumFields) {
			intValues [index.intIndex] = ((Enum)f.get (obj)).ordinal();
			index.intIndex++;
		}
		
		for (Field f : floatFields) {
			floatValues [index.floatIndex] = f.getFloat(obj);
			index.floatIndex++;
		}
		
		for (Field f : doubleFields) {
			floatValues [index.floatIndex] = (float) f.getDouble(obj);
			index.floatIndex++;						
		}
		
		for (Field f : stringFields) {
			stringValues [index.stringIndex] = (String) f.get(obj);
			index.stringIndex++;
		}
		
		for (ClassMapping c : objectFields) {
			Object cpt = c.field.get (obj);	
			
			if (cpt == null) {
				intValues [index.intIndex] = 0;
				index.intIndex++;
				
				index.intIndex += c.sizeInt;
				index.floatIndex += c.sizeFloat;
				index.stringIndex += c.sizeString;
			} else {
				intValues [index.intIndex] = 1;
				index.intIndex++;
				c.setData(cpt, intValues, floatValues, stringValues, index);
			}								
		}
		
		for (Field f : intArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);
			
			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				intValues [index.intIndex] = ((int []) f.get (obj)) [i];
				index.intIndex++;
			}
		}
		
		for (Field f : booleanArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);
			
			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				intValues [index.intIndex] = ((boolean []) f.get (obj)) [i] ? 1 : 0;
				index.intIndex++;
			}
		}
		
		for (ClassMapping c : objectArrayFields) {
			TileNetworkData updateAnnotation = c.field.getAnnotation(TileNetworkData.class);
			
			Object [] cpts = (Object []) c.field.get (obj);	
			
			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				if (cpts [i] == null) {
					intValues[index.intIndex] = 0;
					index.intIndex++;

					index.intIndex += c.sizeInt;
					index.floatIndex += c.sizeFloat;
					index.stringIndex += c.sizeString;
				} else {
					intValues[index.intIndex] = 1;
					index.intIndex++;
					
					c.setData(cpts [i], intValues, floatValues, stringValues,
							index);
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void updateFromData(Object obj, int[] intValues,
			float[] floatValues, String[] stringValues, Indexes index)
			throws IllegalArgumentException, IllegalAccessException {
		
		for (Field f : shortFields) {
			f.setShort(obj, (short) intValues [index.intIndex]);
			index.intIndex++;
		}
		
		for (Field f : intFields) {
			f.setInt(obj, intValues [index.intIndex]);
			index.intIndex++;
		}
		
		for (Field f : booleanFields) {
			f.setBoolean(obj, intValues [index.intIndex] == 1);
			index.intIndex++;
		}
		
		for (Field f : enumFields) {
			f.set(obj,
					((Class) f.getGenericType()).getEnumConstants()[intValues[index.intIndex]]);
			index.intIndex++;
		}
		
		for (Field f : floatFields) {
			f.setFloat(obj, floatValues [index.floatIndex]);			
			index.floatIndex++;
		}
		
		for (Field f : doubleFields) {
			f.setDouble(obj, floatValues [index.floatIndex]);
			index.floatIndex++;
		}
		
		for (Field f : stringFields) {
			f.set(obj, stringValues [index.stringIndex]);
			index.stringIndex++;
		}
		
		for (ClassMapping c : objectFields) {
			boolean isNull = intValues [index.intIndex] == 0;
			index.intIndex++;	
			
			if (isNull) {
				index.intIndex += c.sizeInt;
				index.floatIndex += c.sizeFloat;
				index.stringIndex += c.sizeString;
			} else {
				c.updateFromData(c.field.get(obj), intValues, floatValues, stringValues,
						index);
			}								
		}	
		
		for (Field f : intArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);
			
			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				((int []) f.get (obj)) [i] = intValues [index.intIndex];
				index.intIndex++;
			}
		}
		
		for (Field f : booleanArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);
			
			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				((boolean []) f.get (obj)) [i] = intValues [index.intIndex] == 1;
				index.intIndex++;
			}
		}
		
		for (ClassMapping c : objectArrayFields) {
			TileNetworkData updateAnnotation = c.field.getAnnotation(TileNetworkData.class);
			
			Object [] cpts = (Object []) c.field.get (obj);	
			
			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				boolean isNull = intValues [index.intIndex] == 0;
				index.intIndex++;	
				
				if (isNull) {		
					index.intIndex += c.sizeInt;
					index.floatIndex += c.sizeFloat;
					index.stringIndex += c.sizeString;
				} else {
					c.updateFromData(cpts [i], intValues, floatValues, stringValues,
							index);
				}
			}
		}
	}
	
	public int [] getSize () {
		int [] result = new int [3];
				
		result [0] = sizeInt;
		result [1] = sizeFloat;
		result [2] = sizeString;
		
		return result;
	}
}