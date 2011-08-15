package net.minecraft.src.buildcraft.core;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.LinkedList;

import net.minecraft.src.TileEntity;

class ClassMapping {
	private LinkedList<Field> floatFields = new LinkedList<Field>();
	private LinkedList<Field> stringFields = new LinkedList<Field>();
	private LinkedList<Field> intFields = new LinkedList<Field>();
	private LinkedList<Field> booleanFields = new LinkedList<Field>();
	private LinkedList<Field> enumFields = new LinkedList<Field>();
	private LinkedList<ClassMapping> objectFields = new LinkedList<ClassMapping>();
	private LinkedList<ClassMapping> objectArrayFields = new LinkedList<ClassMapping>();
	
	private int sizeInt;
	private int sizeFloat;
	private int sizeString;
	
	private boolean dynamicSize = false;

	PacketIds packetType;
	
	Field field;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ClassMapping(final Class <? extends TileEntity> c, PacketIds packetType) {
		this.packetType = packetType;
		
		Field[] fields = c.getFields();

		try {
			for (Field f : fields) {
				if (!isSynchronizedField(f)) {
					continue;
				}

				Type t = f.getGenericType();

				// ??? take into account enumerations here!
				
				if (t instanceof Class) {
					Class fieldClass = (Class) t;
					if (fieldClass.equals(int.class)) {
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
					} else {
						ClassMapping mapping = new ClassMapping(fieldClass, packetType); 
						mapping.field = f;

						objectFields.add(mapping);
						sizeInt++; // to catch null / not null.

						sizeInt += mapping.sizeInt;
						sizeFloat += mapping.sizeFloat;
						sizeString += mapping.sizeString;
					}
				} else if (t instanceof GenericArrayType) {
					// Here, only take into account arrays with a given size,
					// that can be handled by the generic mechanism above.
					
					GenericArrayType array = (GenericArrayType) t;
										
					if (array.getGenericComponentType() instanceof Class) {
						Class arrayClass = (Class) array.getGenericComponentType(); 
					
						if (arrayClass.equals(int.class)) {

						} else if (arrayClass.equals(boolean.class)) {

						} else if (arrayClass.equals(String.class)) {
	
						} else if (arrayClass.equals(float.class)) {

						} else {
							//  ??? complete this for the marker implementation
							ClassMapping mapping = new ClassMapping(arrayClass, packetType); 
							mapping.field = f;

							objectArrayFields.add(mapping);
							dynamicSize = true;
						}
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
		
		if (updateAnnotation == null) {
			return false;
		} else {			
			if (updateAnnotation.packetFilter().length == 0) {
				return true;
			}
				
			for (PacketIds id : updateAnnotation.packetFilter()) {
				if (id == packetType) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	public void setData(Object obj, int[] intValues, float[] floatValues,
			String[] stringValues, Integer intIndex, Integer floatIndex,
			Integer stringIndex) throws IllegalArgumentException, IllegalAccessException {
		
		for (Field f : intFields) {
			intValues [intIndex] = f.getInt(obj);
			intIndex++;
		}
		
		for (Field f : booleanFields) {
			intValues [intIndex] = f.getBoolean(obj) ? 1 : 0;
			intIndex++;
		}
		
		for (Field f : enumFields) {
			intValues [intIndex] = ((Enum)f.get (obj)).ordinal();
			intIndex++;
		}
		
		for (Field f : floatFields) {
			floatValues [floatIndex] = f.getFloat(obj);
			floatIndex++;
		}
		
		for (Field f : stringFields) {
			stringValues [stringIndex] = (String) f.get(obj);
			stringIndex++;
		}
		
		for (ClassMapping c : objectFields) {
			Object cpt = c.field.get (obj);	
			
			if (cpt == null) {
				intValues [intIndex] = 0;
				intIndex++;
				
				intIndex += c.sizeInt;
				floatIndex += c.sizeFloat;
				stringIndex += c.sizeString;
			} else {
				intValues [intIndex] = 1;
				intIndex++;
				c.setData(cpt, intValues, floatValues, stringValues,
						intIndex, floatIndex, stringIndex);
			}								
		}						
	}
	
	@SuppressWarnings("rawtypes")
	public void updateFromData (Object obj, int[] intValues, float[] floatValues,
			String[] stringValues, Integer intIndex, Integer floatIndex,
			Integer stringIndex) throws IllegalArgumentException, IllegalAccessException {
		
		for (Field f : intFields) {
			f.setInt(obj, intValues [intIndex]);
			intIndex++;
		}
		
		for (Field f : booleanFields) {
			f.setBoolean(obj, intValues [intIndex] == 1);
			intIndex++;
		}
		
		for (Field f : enumFields) {
			f.set(obj,
					((Class) f.getGenericType()).getEnumConstants()[intValues[intIndex]]);
			intIndex++;
		}
		
		for (Field f : floatFields) {
			f.setFloat(obj, floatValues [floatIndex]);			
			floatIndex++;
		}
		
		for (Field f : stringFields) {
			f.set(obj, stringValues [stringIndex]);
			stringIndex++;
		}
		
		for (ClassMapping c : objectFields) {
			boolean isNull = intValues [intIndex] == 0;
			intIndex++;	
			
			if (isNull) {
				intIndex += c.sizeInt;
				floatIndex += c.sizeFloat;
				stringIndex += c.sizeString;
			} else {
				c.updateFromData(c.field.get(obj), intValues, floatValues, stringValues,
						intIndex, floatIndex, stringIndex);
			}								
		}	
	}
	
	public int [] getSize () {
		int [] result = new int [3];
		
		if (!dynamicSize) {
			result [0] = sizeInt;
			result [1] = sizeFloat;
			result [2] = sizeString;
		} else {
			
		}
		
		return result;
	}
}