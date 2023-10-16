package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.template.DirectCondition;
import mx.kenzie.skriptlab.template.DirectEffect;
import mx.kenzie.skriptlab.template.DirectExpression;
import mx.kenzie.skriptlab.template.DirectPropertyCondition;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

interface Maker extends Closeable {
    
    String className();
    
    byte[] generate();
    
    default Class<?> make(SyntaxGenerator loader) {
        final byte[] bytecode = this.generate();
        return loader.loadClass(this.className(), bytecode);
    }
    
    @Override
    void close();
    
    default void addConstructor(String internalName, ClassWriter writer) {
        //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
        final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, internalName, "<init>", "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        //</editor-fold>
    }
    
    default void addPatternsMethod(String internalName, ClassWriter writer) {
        //<editor-fold desc="Generate getPatterns method" defaultstate="collapsed">
        final MethodVisitor getPatterns = writer.visitMethod(ACC_PUBLIC, "getPatterns", "()[Ljava/lang/String;", null,
            null);
        getPatterns.visitCode();
        getPatterns.visitFieldInsn(GETSTATIC, internalName, "patterns", "[Ljava/lang/String;");
        getPatterns.visitInsn(ARETURN);
        getPatterns.visitMaxs(1, 1);
        getPatterns.visitEnd();
        //</editor-fold>
    }
    
}

record ConditionMaker(String className, DirectCondition handler, String... patterns) implements Maker {
    
    @Override
    public void close() {
    }
    
    @Override
    public byte[] generate() {
        final String internalName = "mx/kenzie/skriptlab/generated/" + this.className();
        final ClassWriter writer = new ClassWriter(0);
        //<editor-fold desc="Class meta and fields" defaultstate="collapsed">
        writer.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null, "mx/kenzie/skriptlab/internal/GeneratedCondition",
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "handle", "Lmx/kenzie/skriptlab/template/DirectCondition;", null,
            null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "patterns", "[Ljava/lang/String;", null, null).visitEnd();
        //</editor-fold>
        //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
        this.addConstructor("mx/kenzie/skriptlab/internal/GeneratedCondition", writer);
        //</editor-fold>
        //<editor-fold desc="Generate getHandle method" defaultstate="collapsed">
        final MethodVisitor getHandle = writer.visitMethod(ACC_PUBLIC, "getHandle",
            "()Lmx/kenzie/skriptlab/template/DirectCondition;", null, null);
        getHandle.visitCode();
        getHandle.visitFieldInsn(GETSTATIC, internalName, "handle", "Lmx/kenzie/skriptlab/template/DirectCondition;");
        getHandle.visitInsn(ARETURN);
        getHandle.visitMaxs(1, 1);
        getHandle.visitEnd();
        //</editor-fold>
        //<editor-fold desc="Generate getPatterns method" defaultstate="collapsed">
        this.addPatternsMethod(internalName, writer);
        //</editor-fold>
        writer.visitEnd();
        return writer.toByteArray();
    }
    
}

record PropertyConditionMaker(String className, DirectPropertyCondition<?> handler, String pattern) implements Maker {
    
    @Override
    public void close() {
    }
    
    @Override
    public byte[] generate() {
        final String internalName = "mx/kenzie/skriptlab/generated/" + this.className();
        final ClassWriter writer = new ClassWriter(0);
        //<editor-fold desc="Class meta and fields" defaultstate="collapsed">
        writer.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null,
            "mx/kenzie/skriptlab/internal/GeneratedPropertyCondition", null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "handle", "Lmx/kenzie/skriptlab/template/DirectPropertyCondition;",
            null, null).visitEnd();
        //</editor-fold>
        //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
        this.addConstructor("mx/kenzie/skriptlab/internal/GeneratedPropertyCondition", writer);
        //</editor-fold>
        //<editor-fold desc="Generate getHandle method" defaultstate="collapsed">
        final MethodVisitor getHandle = writer.visitMethod(ACC_PUBLIC, "getHandle",
            "()Lmx/kenzie/skriptlab/template/DirectPropertyCondition;", null, null);
        getHandle.visitCode();
        getHandle.visitFieldInsn(GETSTATIC, internalName, "handle",
            "Lmx/kenzie/skriptlab/template/DirectPropertyCondition;");
        getHandle.visitInsn(ARETURN);
        getHandle.visitMaxs(1, 1);
        getHandle.visitEnd();
        //</editor-fold>
        //<editor-fold desc="Generate getPatterns method" defaultstate="collapsed">
        final MethodVisitor getPattern = writer.visitMethod(ACC_PUBLIC, "getPattern", "()Ljava/lang/String;", null,
            null);
        getPattern.visitCode();
        getPattern.visitLdcInsn(pattern);
        getPattern.visitInsn(ARETURN);
        getPattern.visitMaxs(1, 1);
        getPattern.visitEnd();
        //</editor-fold>
        writer.visitEnd();
        return writer.toByteArray();
    }
    
}

record EffectMaker(String className, DirectEffect handler, String... patterns) implements Maker {
    
    @Override
    public void close() {
    }
    
    @Override
    public byte[] generate() {
        final String internalName = "mx/kenzie/skriptlab/generated/" + this.className();
        final ClassWriter writer = new ClassWriter(0);
        //<editor-fold desc="Class meta and fields" defaultstate="collapsed">
        writer.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null, "mx/kenzie/skriptlab/internal/GeneratedEffect",
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "handle", "Lmx/kenzie/skriptlab/template/DirectEffect;", null, null)
            .visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "patterns", "[Ljava/lang/String;", null, null).visitEnd();
        //</editor-fold>
        //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
        this.addConstructor("mx/kenzie/skriptlab/internal/GeneratedEffect", writer);
        //</editor-fold>
        //<editor-fold desc="Generate getHandle method" defaultstate="collapsed">
        final MethodVisitor getHandle = writer.visitMethod(ACC_PUBLIC, "getHandle",
            "()Lmx/kenzie/skriptlab/template/DirectEffect;", null, null);
        getHandle.visitCode();
        getHandle.visitFieldInsn(GETSTATIC, internalName, "handle", "Lmx/kenzie/skriptlab/template/DirectEffect;");
        getHandle.visitInsn(ARETURN);
        getHandle.visitMaxs(1, 1);
        getHandle.visitEnd();
        //</editor-fold>
        //<editor-fold desc="Generate getPatterns method" defaultstate="collapsed">
        this.addPatternsMethod(internalName, writer);
        //</editor-fold>
        writer.visitEnd();
        return writer.toByteArray();
    }
    
}

record ExpressionMaker(Class<?> returnType, String className, DirectExpression<?> handler,
                       String... patterns) implements Maker {
    
    @Override
    public void close() {
    }
    
    @Override
    public byte[] generate() {
        final String internalName = "mx/kenzie/skriptlab/generated/" + this.className();
        final ClassWriter writer = new ClassWriter(0);
        //<editor-fold desc="Class meta and fields" defaultstate="collapsed">
        writer.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null,
            "mx/kenzie/skriptlab/internal/GeneratedExpression", null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "handle", "Lmx/kenzie/skriptlab/template/DirectExpression;", null,
            null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "patterns", "[Ljava/lang/String;", null, null).visitEnd();
        //</editor-fold>
        //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
        this.addConstructor("mx/kenzie/skriptlab/internal/GeneratedExpression", writer);
        //</editor-fold>
        //<editor-fold desc="Generate getHandle method" defaultstate="collapsed">
        final MethodVisitor getHandle = writer.visitMethod(ACC_PUBLIC, "getHandle",
            "()Lmx/kenzie/skriptlab/template/DirectExpression;", null, null);
        getHandle.visitCode();
        getHandle.visitFieldInsn(GETSTATIC, internalName, "handle", "Lmx/kenzie/skriptlab/template/DirectExpression;");
        getHandle.visitInsn(ARETURN);
        getHandle.visitMaxs(1, 1);
        getHandle.visitEnd();
        //</editor-fold>
        //<editor-fold desc="Generate getPatterns method" defaultstate="collapsed">
        this.addPatternsMethod(internalName, writer);
        //</editor-fold>
        //<editor-fold desc="Generate getReturnType method" defaultstate="collapsed">
        final MethodVisitor getReturnType = writer.visitMethod(ACC_PUBLIC, "getReturnType", "()Ljava/lang/Class;", null,
            null);
        getReturnType.visitCode();
        getReturnType.visitVarInsn(ALOAD, 0);
        getReturnType.visitMethodInsn(INVOKEVIRTUAL, "mx/kenzie/skriptlab/generated/" + this.className(), "getHandle",
            "()Lmx/kenzie/skriptlab/template/DirectExpression;", false);
        getReturnType.visitMethodInsn(INVOKEINTERFACE, "mx/kenzie/skriptlab/template/DirectExpression", "getReturnType",
            "()Ljava/lang/Class;", true);
        getReturnType.visitVarInsn(ASTORE, 1); // the user might have provided a return type
        getReturnType.visitVarInsn(ALOAD, 1);
        final Label otherwise = new Label();
        getReturnType.visitJumpInsn(IFNULL, otherwise); // if they didn't, we skip this part
        getReturnType.visitVarInsn(ALOAD, 1);
        getReturnType.visitInsn(ARETURN); // return their type
        getReturnType.visitLabel(otherwise);
        getReturnType.visitFrame(F_APPEND, 1, new Object[]{"java/lang/Class"}, 0, null);
        getReturnType.visitLdcInsn(Type.getType(returnType)); // we use the class-known type
        getReturnType.visitInsn(ARETURN);
        getReturnType.visitMaxs(1, 2);
        getReturnType.visitEnd();
        //</editor-fold>
        writer.visitEnd();
        return writer.toByteArray();
    }
    
}

record DirectEffectMaker(String className, SyntaxExtractor.MaybeEffect effect, String... patterns) implements Maker {
    
    private static void get(MethodVisitor visitor, int index, Class<?> expected) {
        //<editor-fold desc="Get expression value" defaultstate="collapsed">
        final boolean array = expected.isArray();
        visitor.visitVarInsn(ALOAD, 2);
        switch (index) {
            case 0 -> visitor.visitInsn(ICONST_0);
            case 1 -> visitor.visitInsn(ICONST_1);
            case 2 -> visitor.visitInsn(ICONST_2);
            case 3 -> visitor.visitInsn(ICONST_3);
            default -> visitor.visitIntInsn(BIPUSH, index);
        }
        visitor.visitMethodInsn(INVOKEVIRTUAL, "mx/kenzie/skriptlab/Expressions", array ? "getArray" : "get",
            array ? "(I)[Ljava/lang/Object;" : "(I)Ljava/lang/Object;", false);
        visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(expected));
        //</editor-fold>
    }
    
    static void writeCall(MethodVisitor visitor, Method method) {
        final boolean isInterface = method.getDeclaringClass().isInterface(), isDynamic = !Modifier.isStatic(
            method.getModifiers());
        int index = 0;
        if (isDynamic) DirectEffectMaker.get(visitor, index++, method.getDeclaringClass());
        for (final Class<?> type : method.getParameterTypes())
            DirectEffectMaker.get(visitor, index++, type);
        final int opcode = !isDynamic ? INVOKESTATIC : isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL;
        visitor.visitMethodInsn(opcode, Type.getInternalName(method.getDeclaringClass()), method.getName(),
            Type.getMethodDescriptor(method), isInterface);
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public byte[] generate() {
        final String internalName = "mx/kenzie/skriptlab/generated/" + this.className();
        final ClassWriter writer = new ClassWriter(0);
        //<editor-fold desc="Class meta and fields" defaultstate="collapsed">
        writer.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null, "java/lang/Record",
            new String[]{"mx/kenzie/skriptlab/template/DirectEffect"});
        writer.visitRecordComponent("effect", "Lmx/kenzie/skriptlab/annotation/Effect;", null).visitEnd();
        writer.visitField(ACC_PRIVATE | ACC_FINAL, "effect", "Lmx/kenzie/skriptlab/annotation/Effect;", null, null)
            .visitEnd();
        //</editor-fold>
        //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
        final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>",
            "(Lmx/kenzie/skriptlab/annotation/Effect;)V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Record", "<init>", "()V", false);
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitVarInsn(ALOAD, 1);
        constructor.visitFieldInsn(PUTFIELD, internalName, "effect", "Lmx/kenzie/skriptlab/annotation/Effect;");
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(2, 2);
        constructor.visitEnd();
        //</editor-fold>
        //<editor-fold desc="Generate execute method" defaultstate="collapsed">
        final boolean isDynamic = !Modifier.isStatic(effect.method.getModifiers());
        final MethodVisitor execute = writer.visitMethod(ACC_PUBLIC, "execute",
            "(Lorg/bukkit/event/Event;Lmx/kenzie/skriptlab/Expressions;)V", null, null);
        execute.visitCode();
        final Class<?> result = effect.method.getReturnType();
        DirectEffectMaker.writeCall(execute, effect.method);
        if (result == double.class || result == long.class) execute.visitInsn(POP2);
        else if (result != void.class) execute.visitInsn(POP);
        execute.visitInsn(RETURN);
        execute.visitMaxs(Math.max(2, 1 + effect.method.getParameterCount() + (isDynamic ? 1 : 0)), 3);
        execute.visitEnd();
        //</editor-fold>
        writer.visitEnd();
        return writer.toByteArray();
    }
    
}

record DirectConditionMaker(String className, SyntaxExtractor.MaybeCondition condition,
                            String... patterns) implements Maker {
    
    @Override
    public void close() {
    }
    
    @Override
    public byte[] generate() {
        final String internalName = "mx/kenzie/skriptlab/generated/" + this.className();
        final ClassWriter writer = new ClassWriter(0);
        //<editor-fold desc="Class meta and fields" defaultstate="collapsed">
        writer.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null, "java/lang/Record",
            new String[]{"mx/kenzie/skriptlab/template/DirectCondition"});
        writer.visitRecordComponent("condition", "Lmx/kenzie/skriptlab/annotation/Condition;", null).visitEnd();
        writer.visitField(ACC_PRIVATE | ACC_FINAL, "condition", "Lmx/kenzie/skriptlab/annotation/Condition;", null,
            null).visitEnd();
        //</editor-fold>
        //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
        final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>",
            "(Lmx/kenzie/skriptlab/annotation/Condition;)V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Record", "<init>", "()V", false);
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitVarInsn(ALOAD, 1);
        constructor.visitFieldInsn(PUTFIELD, internalName, "condition", "Lmx/kenzie/skriptlab/annotation/Condition;");
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(2, 2);
        constructor.visitEnd();
        //</editor-fold>
        //<editor-fold desc="Generate execute method" defaultstate="collapsed">
        final boolean isDynamic = !Modifier.isStatic(condition.method.getModifiers());
        final MethodVisitor execute = writer.visitMethod(ACC_PUBLIC, "check",
            "(Lorg/bukkit/event/Event;Lmx/kenzie/skriptlab/Expressions;)Z", null, null);
        execute.visitCode();
        DirectEffectMaker.writeCall(execute, condition.method);
        execute.visitInsn(IRETURN);
        execute.visitMaxs(Math.max(2, 1 + condition.method.getParameterCount() + (isDynamic ? 1 : 0)), 3);
        execute.visitEnd();
        //</editor-fold>
        writer.visitEnd();
        return writer.toByteArray();
    }
    
}

record DirectPropertyConditionMaker(String className, SyntaxExtractor.MaybePropertyCondition condition,
                                    String pattern) implements Maker {
    
    @Override
    public void close() {
    }
    
    @Override
    public byte[] generate() {
        final String internalName = "mx/kenzie/skriptlab/generated/" + this.className();
        final ClassWriter writer = new ClassWriter(0);
        //<editor-fold desc="Class meta and fields" defaultstate="collapsed">
        writer.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null, "java/lang/Record",
            new String[]{"mx/kenzie/skriptlab/template/DirectPropertyCondition"});
        writer.visitRecordComponent("condition", "Lmx/kenzie/skriptlab/annotation/PropertyCondition;", null).visitEnd();
        writer.visitField(ACC_PRIVATE | ACC_FINAL, "condition", "Lmx/kenzie/skriptlab/annotation/PropertyCondition;",
            null, null).visitEnd();
        //</editor-fold>
        //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
        final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>",
            "(Lmx/kenzie/skriptlab/annotation/PropertyCondition;)V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Record", "<init>", "()V", false);
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitVarInsn(ALOAD, 1);
        constructor.visitFieldInsn(PUTFIELD, internalName, "condition",
            "Lmx/kenzie/skriptlab/annotation/PropertyCondition;");
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(2, 2);
        constructor.visitEnd();
        //</editor-fold>
        //<editor-fold desc="Generate execute method" defaultstate="collapsed">
        final Method method = condition.method;
        final MethodVisitor execute = writer.visitMethod(ACC_PUBLIC, "check", "(Ljava/lang/Object;)Z", null, null);
        execute.visitCode();
        final boolean isInterface = method.getDeclaringClass().isInterface();
        final int opcode = isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL;
        execute.visitVarInsn(ALOAD, 1);
        execute.visitTypeInsn(CHECKCAST, Type.getInternalName(method.getDeclaringClass()));
        execute.visitMethodInsn(opcode, Type.getInternalName(method.getDeclaringClass()), method.getName(),
            Type.getMethodDescriptor(method), isInterface);
        execute.visitInsn(IRETURN);
        execute.visitMaxs(1, 2);
        execute.visitEnd();
        //</editor-fold>
        writer.visitEnd();
        return writer.toByteArray();
    }
    
}

record DirectExpressionMaker(String className, SyntaxExtractor.MaybeExpression expression,
                             String... patterns) implements Maker {
    
    private static void get(MethodVisitor visitor, int index, Class<?> expected) {
        //<editor-fold desc="Get expression value" defaultstate="collapsed">
        final boolean array = expected.isArray();
        visitor.visitVarInsn(ALOAD, 2);
        switch (index) {
            case 0 -> visitor.visitInsn(ICONST_0);
            case 1 -> visitor.visitInsn(ICONST_1);
            case 2 -> visitor.visitInsn(ICONST_2);
            case 3 -> visitor.visitInsn(ICONST_3);
            default -> visitor.visitIntInsn(BIPUSH, index);
        }
        visitor.visitMethodInsn(INVOKEVIRTUAL, "mx/kenzie/skriptlab/Expressions", array ? "getArray" : "get",
            array ? "(I)[Ljava/lang/Object;" : "(I)Ljava/lang/Object;", false);
        visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(expected));
        //</editor-fold>
    }
    
    static void writeCall(MethodVisitor visitor, Method method) {
        final boolean isInterface = method.getDeclaringClass().isInterface(), isDynamic = !Modifier.isStatic(
            method.getModifiers());
        int index = 0;
        if (isDynamic) DirectExpressionMaker.get(visitor, index++, method.getDeclaringClass());
        for (final Class<?> type : method.getParameterTypes())
            DirectExpressionMaker.get(visitor, index++, type);
        final int opcode = !isDynamic ? INVOKESTATIC : isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL;
        visitor.visitMethodInsn(opcode, Type.getInternalName(method.getDeclaringClass()), method.getName(),
            Type.getMethodDescriptor(method), isInterface);
    }
    
    private static void wrapPrimitiveNumber(MethodVisitor method, Class<?> result) {
        if (result == int.class)
            method.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        else if (result == float.class)
            method.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        else if (result == long.class)
            method.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        else if (result == double.class)
            method.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
    }
    
    static void writeChangeCall(String internalName, MethodVisitor visitor, AccessMode mode, Method method) {
        //<editor-fold desc="Call the change method using the delta" defaultstate="collapsed">
        final boolean isInterface = method.getDeclaringClass().isInterface(), isDynamic = !Modifier.isStatic(
            method.getModifiers());
        final Class<?> source;
        if (isDynamic) source = method.getDeclaringClass();
        else if (method.getParameterCount() == 0) source = null;
        else source = method.getParameterTypes()[0];
        if (source != null) { // if this is an expression relating to something
            visitor.visitVarInsn(ALOAD, 1);
            visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(source));
        }
        if (mode.expectArguments) { // if we're setting or adding
            final Class<?> input = method.getParameterTypes()[method.getParameterCount() - 1];
            if (input.isArray()) {
                visitor.visitVarInsn(ALOAD, 0);
                visitor.visitLdcInsn(Type.getType(input.getComponentType()));
                visitor.visitVarInsn(ALOAD, 2);
                visitor.visitMethodInsn(INVOKEVIRTUAL, internalName, "getArray",
                    "(Ljava/lang/Class;[Ljava/lang/Object;)[Ljava/lang/Object;", false);
                visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(input));
            } else {
                visitor.visitVarInsn(ALOAD, 0);
                visitor.visitLdcInsn(Type.getType(input));
                visitor.visitVarInsn(ALOAD, 2);
                visitor.visitMethodInsn(INVOKEVIRTUAL, internalName, "getSingle",
                    "(Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Object;", false);
                visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(input));
            }
        }
        final int opcode = !isDynamic ? INVOKESTATIC : isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL;
        visitor.visitMethodInsn(opcode, Type.getInternalName(method.getDeclaringClass()), method.getName(),
            Type.getMethodDescriptor(method), isInterface);
        final Class<?> result = method.getReturnType();
        if (result == double.class || result == long.class) visitor.visitInsn(POP2);
        else if (result != void.class) visitor.visitInsn(POP);
        //</editor-fold>
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public byte[] generate() {
        final String internalName = "mx/kenzie/skriptlab/generated/" + this.className();
        final ClassWriter writer = new ClassWriter(0);
        final Map<AccessMode, SyntaxExtractor.MaybeExpression.Pair> changers = new HashMap<>(expression.changers);
        final SyntaxExtractor.MaybeExpression.Pair getter = changers.remove(AccessMode.GET);
        assert getter != null;
        final Method get = getter.method();
        final Class<?> result = get.getReturnType();
        final boolean single = !result.isArray(); // we use the single version to pack an array
        //<editor-fold desc="Class meta and fields" defaultstate="collapsed">
        writer.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null, "java/lang/Record",
            new String[]{"mx/kenzie/skriptlab/template/DirectExpression" + (single ? "$Single" : "")});
        writer.visitRecordComponent("expression", "Lmx/kenzie/skriptlab/annotation/Expression;", null).visitEnd();
        writer.visitField(ACC_PRIVATE | ACC_FINAL, "expression", "Lmx/kenzie/skriptlab/annotation/Expression;", null,
            null).visitEnd();
        //</editor-fold>
        //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
        final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>",
            "(Lmx/kenzie/skriptlab/annotation/Expression;)V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Record", "<init>", "()V", false);
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitVarInsn(ALOAD, 1);
        constructor.visitFieldInsn(PUTFIELD, internalName, "expression", "Lmx/kenzie/skriptlab/annotation/Expression;");
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(2, 2);
        constructor.visitEnd();
        //</editor-fold>
        final boolean isDynamic = !Modifier.isStatic(getter.method().getModifiers());
        if (single) {
            //<editor-fold desc="Generate getSingle method" defaultstate="collapsed">
            final MethodVisitor getSingle = writer.visitMethod(ACC_PUBLIC, "getSingle",
                "(Lorg/bukkit/event/Event;Lmx/kenzie/skriptlab/Expressions;)Ljava/lang/Object;", null, null);
            getSingle.visitCode();
            DirectExpressionMaker.writeCall(getSingle, get);
            DirectExpressionMaker.wrapPrimitiveNumber(getSingle, result);
            getSingle.visitInsn(ARETURN);
            getSingle.visitMaxs(Math.max(2, 1 + get.getParameterCount() + (isDynamic ? 1 : 0)), 3);
            getSingle.visitEnd();
            //</editor-fold>
            //<editor-fold desc="Create array type getter" defaultstate="collapsed">
            final MethodVisitor getArrayType = writer.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC,
                "getArrayType", "([Ljava/lang/Object;)Ljava/lang/Class;", null, null);
            getArrayType.visitCode();
            getArrayType.visitLdcInsn(Type.getType(Object.class));
            getArrayType.visitInsn(ARETURN);
            getArrayType.visitMaxs(1, 2);
            getArrayType.visitEnd();
            //</editor-fold>
        } else {
            //<editor-fold desc="Generate get method" defaultstate="collapsed">
            final MethodVisitor getArray = writer.visitMethod(ACC_PUBLIC, "get",
                "(Lorg/bukkit/event/Event;Lmx/kenzie/skriptlab/Expressions;)[Ljava/lang/Object;", null, null);
            getArray.visitCode();
            DirectExpressionMaker.writeCall(getArray, get);
            getArray.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
            getArray.visitInsn(ARETURN);
            getArray.visitMaxs(Math.max(2, 1 + get.getParameterCount() + (isDynamic ? 1 : 0)), 3);
            getArray.visitEnd();
            //</editor-fold>
        }
        if (!changers.isEmpty()) {
            //<editor-fold desc="Create acceptChange method" defaultstate="collapsed">
            final MethodVisitor acceptChange = writer.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC,
                "acceptChange", "(Lch/njol/skript/classes/Changer$ChangeMode;)[Ljava/lang/Class;", null, null);
            acceptChange.visitCode();
            for (final Map.Entry<AccessMode, SyntaxExtractor.MaybeExpression.Pair> entry : changers.entrySet()) {
                //<editor-fold desc="Check type for accessor" defaultstate="collapsed">
                final AccessMode mode = entry.getKey();
                final SyntaxExtractor.MaybeExpression.Pair pair = entry.getValue();
                final Method method = pair.method();
                acceptChange.visitVarInsn(ALOAD, 1);
                acceptChange.visitFieldInsn(GETSTATIC, "ch/njol/skript/classes/Changer$ChangeMode", mode.name(),
                    "Lch/njol/skript/classes/Changer$ChangeMode;");
                final Label not = new Label();
                acceptChange.visitJumpInsn(IF_ACMPNE, not);
                if (mode.expectArguments) { // we want to accept a certain type
                    acceptChange.visitInsn(ICONST_1);
                    acceptChange.visitTypeInsn(ANEWARRAY, "java/lang/Class");
                    acceptChange.visitInsn(DUP);
                    acceptChange.visitInsn(ICONST_0);
                    acceptChange.visitLdcInsn(Type.getType(method.getParameterTypes()[method.getParameterCount() - 1]));
                    acceptChange.visitInsn(AASTORE);
                } else { // allow this but with no inputs (e.g. DELETE)
                    acceptChange.visitInsn(ICONST_0);
                    acceptChange.visitTypeInsn(ANEWARRAY, "java/lang/Class");
                }
                acceptChange.visitInsn(ARETURN);
                acceptChange.visitLabel(not);
                acceptChange.visitFrame(F_SAME, 0, null, 0, null);
                //</editor-fold>
            }
            acceptChange.visitInsn(ACONST_NULL); // changer isn't supported :(
            acceptChange.visitInsn(ARETURN);
            acceptChange.visitMaxs(4, 2);
            acceptChange.visitEnd();
            //</editor-fold>
            //<editor-fold desc="Create change method" defaultstate="collapsed">
            final MethodVisitor change = writer.visitMethod(ACC_PUBLIC, "change",
                "(Ljava/lang/Object;[Ljava/lang/Object;Lch/njol/skript/classes/Changer$ChangeMode;)V", null, null);
            change.visitCode();
            for (final Map.Entry<AccessMode, SyntaxExtractor.MaybeExpression.Pair> entry : changers.entrySet()) {
                final AccessMode mode = entry.getKey();
                final SyntaxExtractor.MaybeExpression.Pair pair = entry.getValue();
                final Method method = pair.method();
                change.visitVarInsn(ALOAD, 3);
                change.visitFieldInsn(GETSTATIC, "ch/njol/skript/classes/Changer$ChangeMode", mode.name(),
                    "Lch/njol/skript/classes/Changer$ChangeMode;");
                final Label next = new Label();
                change.visitJumpInsn(IF_ACMPNE, next);
                DirectExpressionMaker.writeChangeCall(internalName, change, mode, method);
                change.visitLabel(next);
                change.visitFrame(F_SAME, 0, null, 0, null);
            }
            change.visitInsn(RETURN);
            change.visitMaxs(4, 4);
            change.visitEnd();
            //</editor-fold>
        }
        writer.visitEnd();
        return writer.toByteArray();
    }
    
}
