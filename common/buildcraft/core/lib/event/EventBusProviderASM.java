package buildcraft.core.lib.event;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import buildcraft.api.core.BCLog;

public class EventBusProviderASM<T> implements IEventBusProvider<T> {
    private final Class<T> eventBaseClass;
    private final Class<? extends Annotation> annotationClass;

    // We don't synchronize this here as we only use this in one method, which handles sync properly.
    private final Map<Class<?>, EventProviderASM<T>> classMap = new HashMap<>();

    public EventBusProviderASM(Class<T> eventClass, Class<? extends Annotation> annotation) {
        this.eventBaseClass = eventClass;
        this.annotationClass = annotation;
    }

    @Override
    public IEventBus<T> newBus() {
        return new EventBusASM<>(this);
    }

    public EventProviderASM<T> getProviderFor(Class<?> clazz) {
        if (!classMap.containsKey(clazz)) {
            synchronized (classMap) {
                if (!classMap.containsKey(clazz)) {
                    EventProviderASM<T> prov = generateProvider(clazz);
                    classMap.put(clazz, prov);
                }
            }
        }
        return classMap.get(clazz);
    }

    private EventProviderASM<T> generateProvider(Class<?> clazz) {
        List<IEventHandlerProvider<T>> providers = Lists.newArrayList();
        for (Method meth : clazz.getMethods()) {
            if (!Modifier.isPublic(meth.getModifiers())) continue;
            Annotation annotation = meth.getAnnotation(annotationClass);
            if (annotation == null) continue;
            Class<?>[] parameters = meth.getParameterTypes();
            if (parameters.length != 1) {
                BCLog.logger.warn("Found a method " + meth.getName() + " in the class " + clazz.getName() + ", annoted with @" + annotationClass
                        .getSimpleName() + ", that does not have a single argument!");
                continue;
            }
            Class<?> par = parameters[0];
            // Disallow listening to classes directly, instead every event is an interface
            if (!par.isInterface()) {
                BCLog.logger.warn("Found a method " + meth.getName() + " in the class " + clazz.getName() + " that listened to a class directly!");
                continue;
            }

            if (!eventBaseClass.isAssignableFrom(par)) {
                BCLog.logger.warn("Found a method " + meth.getName() + " in the class " + clazz.getName() + ", annoted with @" + annotationClass
                        .getSimpleName() + ", that has an argument that does not extend " + eventBaseClass.getName() + " (Was " + par.getName()
                    + "!");
                continue;
            }
            providers.add(generateSingleProvider(meth, par));
        }
        return new EventProviderASM<>(providers);
    }

    private IEventHandlerProvider<T> generateSingleProvider(Method meth, Class<?> parClass) {
        String clsName = "buildcraft.core.lib.event._GENERATED_.";
        clsName += meth.getDeclaringClass().getName() + "._METHOD_.";
        clsName += meth.getName() + "._EVENT_." + parClass.getName();

        String name = clsName + ".Caller";
        byte[] bytecode = generateDirectHandler(meth, parClass, name);
        Class<IEventHandler<?>> handler = writeAndLoadClassOfA(bytecode, name);

        name = clsName + ".Generator";
        bytecode = generateGenerator(handler, meth.getDeclaringClass(), name);
        Class<IEventHandlerProvider<T>> provider = writeAndLoadClassOfA(bytecode, name);

        try {
            return provider.newInstance();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private byte[] generateGenerator(Class<? extends IEventHandler<?>> handlerClass, Class<?> parClass, String clsName) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassNode node = new ClassNode();
        node.name = clsName.replace('.', '/');
        node.version = Opcodes.V1_6;
        node.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL;
        node.interfaces = Lists.newArrayList(IEventHandlerProvider.class.getName().replace('.', '/'));
        node.superName = "java/lang/Object";

        // Method:
        // public ClassName() {
        // super();
        // }
        {
            MethodNode consturctorMethod = new MethodNode();
            consturctorMethod.access = Opcodes.ACC_PUBLIC;
            consturctorMethod.desc = "()V";
            consturctorMethod.name = "<init>";
            consturctorMethod.exceptions = Lists.newArrayList();

            consturctorMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            consturctorMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
            consturctorMethod.instructions.add(new InsnNode(Opcodes.RETURN));

            node.methods.add(consturctorMethod);
        }

        // Method:
        // public IEventHandler createNewHandler(Object obj) {
        // return new ClassHandler(obj);
        // }
        {
            MethodNode generationMethod = new MethodNode();
            // public final void handle(Object)
            generationMethod.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL;
            generationMethod.desc = "(Ljava/lang/Object;)Lbuildcraft/core/lib/event/IEventHandler;";
            generationMethod.name = "createNewHandler";
            generationMethod.exceptions = Lists.newArrayList();

            {
                generationMethod.instructions.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(handlerClass)));
                generationMethod.instructions.add(new InsnNode(Opcodes.DUP));
                generationMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                generationMethod.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(parClass)));
                generationMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(handlerClass), "<init>", Type
                        .getMethodDescriptor(Type.VOID_TYPE, Type.getType(parClass)), false));
                generationMethod.instructions.add(new InsnNode(Opcodes.ARETURN));
            }

            node.methods.add(generationMethod);
        }

        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] generateDirectHandler(Method meth, Class<?> parClass, String clsName) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassNode node = new ClassNode();
        node.name = clsName.replace('.', '/');
        node.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL;
        node.interfaces = Lists.newArrayList(IEventHandler.class.getName().replace('.', '/'));
        node.version = Opcodes.V1_6;
        node.superName = "java/lang/Object";

        String fd = Type.getDescriptor(meth.getDeclaringClass());

        node.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "listener", fd, null, null));

        // This method does:
        // public ClassName(ListenerObject obj) {
        // super();
        // this.listener = obj;
        // }
        {
            MethodNode consturctorMethod = new MethodNode();
            consturctorMethod.access = Opcodes.ACC_PUBLIC;
            consturctorMethod.desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(meth.getDeclaringClass()));
            consturctorMethod.name = "<init>";
            consturctorMethod.exceptions = Lists.newArrayList();

            consturctorMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            consturctorMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
            consturctorMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            consturctorMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            consturctorMethod.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, node.name, "listener", fd));
            consturctorMethod.instructions.add(new InsnNode(Opcodes.RETURN));

            node.methods.add(consturctorMethod);
        }

        // This method does:
        // public final void handle(Object event) {
        // if (!(event instanceof EventClass)) return;
        // listener.<method_name>((EventClass) event);
        // }
        {
            MethodNode generationMethod = new MethodNode();
            // public final void handle(Object)
            generationMethod.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL;
            generationMethod.desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class));
            generationMethod.name = "handle";
            generationMethod.exceptions = Lists.newArrayList();

            // This block does:
            // if (!(event instanceof EventClass)) return;
            {
                // ADD the first object (given as [this, event])
                // -> event
                generationMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                // event -> event instanceof PAR_CLASS -> boolean
                generationMethod.instructions.add(new TypeInsnNode(Opcodes.INSTANCEOF, Type.getInternalName(parClass)));
                LabelNode instanceLabel = new LabelNode();
                // boolean -> if (boolean) GOTO instanceLabel ->
                generationMethod.instructions.add(new JumpInsnNode(Opcodes.IFNE, instanceLabel));
                // return;
                generationMethod.instructions.add(new InsnNode(Opcodes.RETURN));
                generationMethod.instructions.add(instanceLabel);
            }
            // This block does:
            // listener.<method_name>(event);
            {
                generationMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                // -> ListenerObject
                String desc = Type.getDescriptor(meth.getDeclaringClass());
                generationMethod.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, node.name, "listener", desc));
                // -> Object
                generationMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                // CheckCast
                generationMethod.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(parClass)));
                // ListenerObject, EventObject -> <method_name> ->
                generationMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(meth.getDeclaringClass()), meth
                        .getName(), Type.getMethodDescriptor(meth), false));
            }
            // return;
            generationMethod.instructions.add(new InsnNode(Opcodes.RETURN));

            node.methods.add(generationMethod);
        }

        node.accept(writer);

        byte[] bytecode = writer.toByteArray();
        return bytecode;
    }

    private <A> Class<A> writeAndLoadClassOfA(byte[] bytes, String clsName) {
        try {
            // Just output the classes for debugging. Remove this later when we know it works fully.
            File folder = new File("./asm/buildcraft/");
            folder.mkdirs();
            FileOutputStream fos = new FileOutputStream("./asm/buildcraft/" + clsName + ".class");
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            // We should never NOT be able to write the class to debug it.
            throw new RuntimeException(e);
        }

        Class<?> cls = ByteCodeLoader.INSTANCE.define(clsName, bytes);
        return (Class<A>) cls;
    }

    static class ByteCodeLoader extends ClassLoader {
        public static final ByteCodeLoader INSTANCE = new ByteCodeLoader();

        private Map<String, Class<?>> classDefinitionMap = new HashMap<>();

        private ByteCodeLoader() {
            super(ByteCodeLoader.class.getClassLoader());
        }

        public Class<?> define(String name, byte[] data) {
            // Synchronise around the map otherwise two different threads can try to define the same class at the same
            // time
            synchronized (classDefinitionMap) {
                if (!classDefinitionMap.containsKey(name)) {
                    BCLog.logger.info("Defining the class " + name);
                    classDefinitionMap.put(name, defineClass(name, data, 0, data.length));
                }
                return classDefinitionMap.get(name);
            }
        }
    }
}
