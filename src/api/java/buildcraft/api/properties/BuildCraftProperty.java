package buildcraft.api.properties;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.property.IUnlistedProperty;

import buildcraft.api.core.BCLog;

/** This class exists primarily to allow for a property to be used as either a normal IProperty, or an IUnlistedProperty.
 * It also exists to give IProperty's generic types. */
public class BuildCraftProperty<T> implements IProperty, IUnlistedProperty<T> {
    private final String name;
    private final Class<T> clazz;
    private final Collection<T> values;

    public static <E extends Enum<E>> BuildCraftProperty<E> create(String name, Class<E> enumeration) {
        List<E> values = Arrays.asList(enumeration.getEnumConstants());
        return new BuildCraftProperty<E>(name, enumeration, values);
    }

    public static <E extends Enum<E>> BuildCraftProperty<E> create(String name, E... values) {
        Class<E> clazz = values[0].getDeclaringClass();
        List<E> list = Arrays.asList(values);
        return new BuildCraftProperty<E>(name, clazz, list);
    }

    public static BuildCraftProperty<Boolean> create(String name, boolean first) {
        return new BuildCraftProperty<Boolean>(name, Boolean.class, new Boolean[] { first, !first });
    }

    public static BuildCraftProperty<Integer> create(String name, int first, int last) {
        return create(name, first, last, 1);
    }

    public static BuildCraftProperty<Integer> create(String name, int first, int last, int difference) {
        int actualDiff = Math.abs(difference);
        Integer[] array = new Integer[Math.abs(first - last) / actualDiff];
        int addedDiff = actualDiff * (first > last ? -1 : 1);
        BCLog.logger.info("create<Integer>(" + first + ", " + last + ", " + difference + ")");
        for (int i = 0; i <= array.length; i++) {
            array[i] = first + (first - last) / addedDiff * i;
            BCLog.logger.info("  " + array[i]);
        }
        return new BuildCraftProperty<Integer>(name, Integer.class, array);
    }

    public static BuildCraftProperty<Double> create(String name, double first, double last) {
        return create(name, first, last, 1);
    }

    public static BuildCraftProperty<Double> create(String name, double first, double last, double difference) {
        double actualDiff = Math.abs(difference);
        Double[] array = new Double[(int) (Math.abs(first - last) / actualDiff)];
        double addedDiff = actualDiff * (first > last ? -1 : 1);
        BCLog.logger.info("create<Integer>(" + first + ", " + last + ", " + difference + ")");
        for (int i = 0; i <= array.length; i++) {
            array[i] = first + (first - last) / addedDiff * i;
            BCLog.logger.info("  " + array[i]);
        }
        return new BuildCraftProperty<Double>(name, Double.class, array);
    }

    public BuildCraftProperty(String name, Class<T> clazz, T[] values) {
        this(name, clazz, Arrays.asList(values));
    }

    public BuildCraftProperty(String name, Class<T> clazz, Collection<T> values) {
        this.name = name;
        this.values = values;
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<T> getAllowedValues() {
        return values;
    }

    @Override
    public Class<T> getValueClass() {
        return clazz;
    }

    @Override
    public String getName(@SuppressWarnings("rawtypes") Comparable value) {
        return valueName(value);
    }

    @Override
    public boolean isValid(T value) {
        return values.contains(value);
    }

    @Override
    public Class<T> getType() {
        return clazz;
    }

    @Override
    public String valueToString(T value) {
        return valueName(value);
    }

    private String valueName(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof IStringSerializable) {
            return ((IStringSerializable) value).getName();
        } else if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        } else {
            return value.toString();
        }
    }

    @SuppressWarnings("unchecked")
    public T getValue(IBlockState state) {
        return (T) state.getValue(this);
    }
}
