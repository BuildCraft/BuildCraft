package buildcraft.lib.misc.data;

import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public class AutoIdAllocator {
    private static final Map<String, Integer> ALLOCATED = new HashMap<>();

    public static void allocate(ASMDataTable asmDataTable) {
        List<ASMDataTable.ASMData> asmDataList = new ArrayList<>(asmDataTable.getAll(AutoId.class.getName()));
        asmDataList.sort((a, b) -> {
            ToIntFunction<ASMDataTable.ASMData> f = asmData -> {
                Class<?> currentClass;
                try {
                    currentClass = Class.forName(asmData.getClassName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                int i = 0;
                while ((currentClass = currentClass.getSuperclass()) != null) {
                    i++;
                }
                return i;
            };
            if (f.applyAsInt(a) == f.applyAsInt(b)) {
                return a.getObjectName().compareTo(b.getObjectName());
            } else {
                return Integer.compare(f.applyAsInt(a), f.applyAsInt(b));
            }
        });
        for (ASMDataTable.ASMData asmData : asmDataList) {
            try {
                Class<?> clazz = Class.forName(asmData.getClassName());
                if (!Modifier.isPublic(clazz.getDeclaredField(asmData.getObjectName()).getModifiers())) {
                    throw new IllegalArgumentException("@" + AutoId.class.getSimpleName() +
                            " on non-public field " + asmData.getObjectName() +
                            " in class " + asmData.getClassName());
                }
                Field field = clazz.getField(asmData.getObjectName());
                if (!Modifier.isStatic(field.getModifiers())) {
                    throw new IllegalArgumentException("@" + AutoId.class.getSimpleName() +
                            " on non-static field " + asmData.getObjectName() +
                            " in class " + asmData.getClassName());
                }
                ALLOCATED.put(
                        clazz.getName(),
                        ALLOCATED.computeIfAbsent(
                                clazz.getName(),
                                fieldName -> {
                                    Class<?> currentClass = clazz;
                                    while ((currentClass = currentClass.getSuperclass()) != null) {
                                        if (ALLOCATED.containsKey(currentClass.getName())) {
                                            return ALLOCATED.get(currentClass.getName());
                                        }
                                    }
                                    return -1;
                                }
                        ) + 1
                );
                field.set(
                        null,
                        ALLOCATED.get(clazz.getName())
                );
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
