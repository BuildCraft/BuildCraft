package net.minecraft.src.buildcraft.core;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.LinkedList;

import net.minecraft.src.TileEntity;

public class ClassMapping {
	
	private LinkedList<Field> floatFields = new LinkedList<Field>();
	private LinkedList<Field> stringFields = new LinkedList<Field>();
	private LinkedList<Field> intFields = new LinkedList<Field>();
	private LinkedList<Field> booleanFields = new LinkedList<Field>();
	private LinkedList<Field> enumFields = new LinkedList<Field>();
	private LinkedList<ClassMapping> objectFields = new LinkedList<ClassMapping>();
	
	private LinkedList<Field> intArrayFields = new LinkedList<Field>();
	private LinkedList<ClassMapping> objectArrayFields = new LinkedList<ClassMapping>();
	
	private int sizeInt;
	private int sizeFloat;
	private int sizeString;
	

	private PacketIds packetType;	
	private Field field;
	
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
				
				System.out.println (t + ", " + (t instanceof Class) + ", " + (t instanceof GenericArrayType));
				
				if (t instanceof Class && !((Class)t).isArray()) {
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
						// ADD SOME SAFETY HERE - if we're not child of Object
						
						ClassMapping mapping = new ClassMapping(fieldClass, packetType); 
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
					} else {
						// ADD SOME SAFETY HERE - if we're not child of Object

						ClassMapping mapping = new ClassMapping(cptClass, packetType);
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
		
		for (Field f : intArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);
			
			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				intValues [intIndex] = ((int []) f.get (obj)) [i];
				intIndex++;
			}
		}
		
		for (ClassMapping c : objectArrayFields) {
			TileNetworkData updateAnnotation = c.field.getAnnotation(TileNetworkData.class);
			
			Object [] cpts = (Object []) c.field.get (obj);	
			
			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				if (cpts [i] == null) {
					intValues[intIndex] = 0;
					intIndex++;

					intIndex += c.sizeInt;
					floatIndex += c.sizeFloat;
					stringIndex += c.sizeString;
				} else {
					intValues[intIndex] = 1;
					intIndex++;
					
					c.setData(cpts [i], intValues, floatValues, stringValues,
							intIndex, floatIndex, stringIndex);
				}
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
		
		for (Field f : intArrayFields) {
			TileNetworkData updateAnnotation = f.getAnnotation(TileNetworkData.class);
			
			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				((int []) f.get (obj)) [i] = intValues [intIndex];
				intIndex++;
			}
		}
		
		for (ClassMapping c : objectArrayFields) {
			TileNetworkData updateAnnotation = c.field.getAnnotation(TileNetworkData.class);
			
			Object [] cpts = (Object []) c.field.get (obj);	
			
			for (int i = 0; i < updateAnnotation.staticSize(); ++i) {
				boolean isNull = intValues [intIndex] == 0;
				intIndex++;	
				
				if (isNull) {		
					intIndex += c.sizeInt;
					floatIndex += c.sizeFloat;
					stringIndex += c.sizeString;
				} else {
					c.updateFromData(cpts [i], intValues, floatValues, stringValues,
							intIndex, floatIndex, stringIndex);
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