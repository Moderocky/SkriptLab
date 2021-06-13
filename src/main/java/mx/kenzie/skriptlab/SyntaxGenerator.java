package mx.kenzie.skriptlab;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.base.CaseFormat;
import mx.kenzie.skriptlab.annotation.*;
import mx.kenzie.skriptlab.error.SyntaxCreationException;
import mx.kenzie.skriptlab.error.SyntaxLoadException;
import mx.kenzie.skriptlab.template.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.*;

import java.lang.reflect.*;

import static org.objectweb.asm.Opcodes.*;

public class SyntaxGenerator {

    protected final JavaPlugin plugin;
    protected final SkriptAddon addon;
    protected RuntimeClassLoader loader = new RuntimeClassLoader(this.getClass().getClassLoader());
    protected volatile int index = 0;
    
    //region Test Only
    @Deprecated
    protected SyntaxGenerator() {
        plugin = null;
        addon = null;
    }
    //endregion
    
    public SyntaxGenerator(final JavaPlugin plugin)
        throws IllegalStateException {
        final Plugin skript = Bukkit.getPluginManager().getPlugin("Skript");
        if (skript == null) throw new IllegalStateException("Skript is not installed.");
        if (!skript.isEnabled()) throw new IllegalStateException("Skript is not enabled.");
        if (!Skript.isAcceptRegistrations()) throw new IllegalStateException("Skript is not accepting registrations.");
        this.plugin = plugin;
        this.addon = Skript.registerAddon(plugin);
    }
    
    //region Syntax Registration
    public void registerSyntaxFrom(final Class<?>... classes) {
        for (final Class<?> cls : classes) {
            registerSyntax(cls);
        }
    }
    
    protected void registerSyntax(final Class<?> cls) {
        if (cls.isAnnotationPresent(SkriptType.class)) {
            registerType(cls);
        }
        for (final Method method : cls.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Effect.class)) continue;
            method.setAccessible(true);
            final Effect effect = method.getAnnotation(Effect.class);
            final String[] syntax = (effect.value().length < 1)
                ? new String[]{CaseFormat.LOWER_CAMEL
                .to(CaseFormat.LOWER_UNDERSCORE, method.getName()).replace("_", " ")}
                : effect.value();
            registerEffect(method, syntax);
        }
        for (final Method method : cls.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Condition.class)) continue;
            method.setAccessible(true);
            final Condition condition = method.getAnnotation(Condition.class);
            final String name = method.getName();
            final String syntax = (condition.value().isEmpty())
                ? CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)
                .replace("_", " ")
                .replaceFirst("get ", "")
                .replaceFirst("can ", "")
                .replaceFirst("has ", "")
                .replaceFirst("is ", "")
                : condition.value();
            registerPropertyCondition(method, syntax, name.startsWith("can")
                ? PropertyCondition.PropertyType.CAN
                : name.startsWith("has")
                ? PropertyCondition.PropertyType.HAVE
                : PropertyCondition.PropertyType.BE);
        }
        for (final Field field : cls.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Condition.class)) continue;
            field.setAccessible(true);
            final String name = field.getName();
            final Condition condition = field.getAnnotation(Condition.class);
            final String syntax = (condition.value().isEmpty())
                ? CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)
                .replace("_", " ")
                .replaceFirst("is ", "")
                .replaceFirst("can ", "")
                .replaceFirst("has ", "")
                : condition.value();
            registerPropertyCondition(field, syntax, name.startsWith("can")
                ? PropertyCondition.PropertyType.CAN
                : name.startsWith("has")
                ? PropertyCondition.PropertyType.HAVE
                : PropertyCondition.PropertyType.BE);
        }
        for (final Field field : cls.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Property.class)) continue;
            field.setAccessible(true);
            final String name = field.getName();
            final Property property = field.getAnnotation(Property.class);
            final String syntax = (property.value().isEmpty())
                ? CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)
                .replace("_", " ")
                : property.value();
            registerPropertyExpression(field, syntax);
        }
        for (final Method method : cls.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Expression.class)) continue;
            method.setAccessible(true);
            final String name = method.getName();
            final Expression expression = method.getAnnotation(Expression.class);
            final String[] syntax = (expression.value().length < 1)
                ? new String[]{CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)
                .replace("_", " ")}
                : expression.value();
            registerSimpleExpression(method, syntax);
        }
    }
    
    protected <ObjectType>
    void registerType(Class<ObjectType> cls) {
        if (Classes.getExactClassInfo(cls) != null) return;
        if (!cls.isAnnotationPresent(SkriptType.class)) return;
        SkriptType type = cls.getDeclaredAnnotation(SkriptType.class);
        String codename = type.value().isEmpty()
            ? cls.getName().toLowerCase()
            : type.value();
        String[] user = cls.isAnnotationPresent(Doc.User.class)
            ? cls.getDeclaredAnnotation(Doc.User.class).value()
            : new String[]{codename};
        String[] description = cls.isAnnotationPresent(Doc.Description.class)
            ? cls.getDeclaredAnnotation(Doc.Description.class).value()
            : new String[]{"Description missing."};
        String since = cls.isAnnotationPresent(Doc.Since.class)
            ? cls.getDeclaredAnnotation(Doc.Since.class).value()
            : "Unknown";
        String name = cls.isAnnotationPresent(Doc.Name.class)
            ? cls.getDeclaredAnnotation(Doc.Name.class).value()
            : codename.toUpperCase().substring(0, 1) + codename.toLowerCase().substring(1);
        final String coded = codename;
        Classes.registerClass(new ClassInfo<>(cls, codename)
            .user(user)
            .name(name)
            .description(description)
            .since(since)
            .changer(new Changer<>() {
                @Override
                public Class<?>[] acceptChange(ChangeMode mode) {
                    if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET)
                        return CollectionUtils.array();
                    return null;
                }
                
                @Override
                public void change(ObjectType[] what, Object[] delta, ChangeMode mode) {
                    final Method reset = getResetMethod(cls);
                    if (reset != null) {
                        if (mode == ChangeMode.RESET) {
                            for (ObjectType objectType : what) {
                                try {
                                    reset.invoke(objectType);
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();;
                                }
                            }
                        }
                    }
                }
            })
            .parser(new Parser<>() {
                
                @Override
                @SuppressWarnings("unchecked")
                public ObjectType parse(String string, ParseContext context) {
                    Method method = getParseMethod(cls);
                    try {
                        if (method == null) throw new IllegalAccessException();
                        return (ObjectType) method.invoke(cls, string);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new UnsupportedOperationException(e);
                    }
                }
                
                @Override
                public boolean canParse(ParseContext context) {
                    return getParseMethod(cls) != null;
                }
                
                @Override
                public String toString(ObjectType o, int flags) {
                    return o.toString();
                }
                
                @Override
                public String toVariableNameString(ObjectType o) {
                    return coded;
                }
                
                @Override
                public String getVariableNamePattern() {
                    return "\\S+";
                }
            })
        );
    }
    
    protected <ClassMember extends AccessibleObject & AnnotatedElement & Member>
    void registerPropertyCondition(final ClassMember object, final String name, final PropertyCondition.PropertyType type) {
        final Class<GeneratedPropertyCondition<?>> cls = this.generatePropertyConditionClass();
        assert cls != null;
        try {
            cls.getField("target").set(null, object);
            cls.getField("name").set(null, name);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SyntaxLoadException("Unable to set method targets.", e);
        }
        ClassInfo<?> info = Classes.getExactClassInfo(object.getDeclaringClass());
        assert info != null;
        PropertyCondition.register(cls, type, name, info.getCodeName());
    }
    
    @SuppressWarnings("unchecked")
    protected <Expr>
    void registerPropertyExpression(final Field field, final String name) {
        final Class<GeneratedPropertyExpression<Expr>> cls = this.generatePropertyExpressionClass();
        assert cls != null;
        final Changer.ChangeMode[] modes = field.isAnnotationPresent(Property.class)
            ? field.getAnnotation(Property.class).allowedModes()
            : new Changer.ChangeMode[0];
        try {
            cls.getField("field").set(null, field);
            cls.getField("modes").set(null, modes);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SyntaxLoadException("Unable to set field targets.", e);
        }
        final ClassInfo<?> info = Classes.getExactClassInfo(field.getDeclaringClass());
        final String objectType = (info != null)
            ? info.getCodeName()
            :  "object";
        Skript.registerExpression(cls, (Class<Expr>) GeneratedExpression.ensureWrapper(field.getType()), ExpressionType.PROPERTY,
            "[the] " + name + " of %" + objectType + "%", "%"+ objectType+"%" + "'s " + name);
    }
    
    @SuppressWarnings("unchecked")
    protected <Expr>
    void registerSimpleExpression(final Method method, final String... syntax) {
        final Class<GeneratedSimpleExpression<Expr>> cls = this.generateSimpleExpressionClass();
        assert cls != null;
        final Changer.ChangeMode[] modes = method.isAnnotationPresent(Expression.class)
            ? method.getAnnotation(Expression.class).allowedModes()
            : new Changer.ChangeMode[0];
        try {
            cls.getField("method").set(null, method);
            cls.getField("modes").set(null, modes);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SyntaxLoadException("Unable to set method targets.", e);
        }
        Skript.registerExpression(cls, (Class<Expr>) GeneratedExpression.ensureWrapper(method.getReturnType()), ExpressionType.PROPERTY,
            syntax);
    }
    
    protected void registerEffect(final Method method, final String... syntax) {
        final Class<GeneratedEffect> cls = this.generateEffectClass();
        assert cls != null;
        try {
            cls.getField("method").set(null, method);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SyntaxLoadException("Unable to set method targets.", e);
        }
        Skript.registerEffect(cls, syntax);
    }
    //endregion
    
    //region Class Generation
    protected synchronized Class<GeneratedEffect> generateEffectClass()
        throws SyntaxCreationException {
        final String namespace = "mx.kenzie.skriptlab.generated.$Effect" + this.hashCode() + "$" + (++index);
        final String internalName = namespace.replace(".", "/");
        final String superName = "mx/kenzie/skriptlab/template/GeneratedEffect";
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER,
            internalName, null, superName,
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "method", "Ljava/lang/reflect/Method;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "caller", "Ljava/lang/Object;", null, null).visitEnd();
        constructor: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
            method.visitInsn(RETURN);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }
        execute: {
            final MethodVisitor method = writer.visitMethod(ACC_PROTECTED, "execute", "(Lorg/bukkit/event/Event;)V", null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitFieldInsn(GETSTATIC, internalName, "method", "Ljava/lang/reflect/Method;");
            method.visitFieldInsn(GETSTATIC, internalName, "caller", "Ljava/lang/Object;");
            method.visitMethodInsn(INVOKESPECIAL, superName, "execute", "(Lorg/bukkit/event/Event;Ljava/lang/reflect/Method;Ljava/lang/Object;)V", false);
            method.visitInsn(RETURN);
            method.visitMaxs(4, 2);
            method.visitEnd();
        }
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedEffect>) cls;
        } catch (Throwable ex) {
            throw new SyntaxLoadException(ex);
        }
    }
    
    protected synchronized Class<GeneratedPropertyCondition<?>> generatePropertyConditionClass()
        throws SyntaxCreationException {
        final String namespace = "mx.kenzie.skriptlab.generated.$PropertyCondition" + this.hashCode() + "$" + (++index);
        final String internalName = namespace.replace(".", "/");
        final String superName = "mx/kenzie/skriptlab/template/GeneratedPropertyCondition";
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER,
            internalName, null, superName,
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "target", "Ljava/lang/Object;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "name", "Ljava/lang/String;", null, null).visitEnd();
        constructor: {
            final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(ALOAD, 0);
            constructor.visitMethodInsn(INVOKESPECIAL, "mx/kenzie/skriptlab/template/GeneratedPropertyCondition", "<init>", "()V", false);
            constructor.visitInsn(RETURN);
            constructor.visitMaxs(1, 1);
            constructor.visitEnd();
        }
        check: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "check", "(Ljava/lang/Object;)Z", "(TT;)Z", null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitFieldInsn(GETSTATIC, internalName, "target", "Ljava/lang/Object;");
            method.visitMethodInsn(INVOKESPECIAL, "mx/kenzie/skriptlab/template/GeneratedPropertyCondition", "check", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
            method.visitInsn(IRETURN);
            method.visitMaxs(3, 2);
            method.visitEnd();
        }
        name: {
            final MethodVisitor methodVisitor = writer.visitMethod(ACC_PROTECTED, "getPropertyName", "()Ljava/lang/String;", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitFieldInsn(GETSTATIC, internalName, "name", "Ljava/lang/String;");
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedPropertyCondition<?>>) cls;
        } catch (Throwable ex) {
            throw new SyntaxLoadException(ex);
        }
    }
    
    protected synchronized <Expr>
    Class<GeneratedPropertyExpression<Expr>> generatePropertyExpressionClass()
        throws SyntaxCreationException {
        final String namespace = "mx.kenzie.skriptlab.generated.$PropertyExpression" + this.hashCode() + "$" + (++index);
        final String internalName = namespace.replace(".", "/");
        final String superName = "mx/kenzie/skriptlab/template/GeneratedPropertyExpression";
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER,
            internalName, null, superName,
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "field", "Ljava/lang/reflect/Field;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "modes", "[Lch/njol/skript/classes/Changer$ChangeMode;", null, null).visitEnd();
        constructor: {
            final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(ALOAD, 0);
            constructor.visitMethodInsn(INVOKESPECIAL, "mx/kenzie/skriptlab/template/GeneratedPropertyExpression", "<init>", "()V", false);
            constructor.visitInsn(RETURN);
            constructor.visitMaxs(1, 1);
            constructor.visitEnd();
        }
        get: {
            final MethodVisitor method = writer.visitMethod(ACC_PROTECTED, "get", "(Lorg/bukkit/event/Event;)[Ljava/lang/Object;", "(Lorg/bukkit/event/Event;)[TT;", null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitFieldInsn(GETSTATIC, internalName, "field", "Ljava/lang/reflect/Field;");
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "get", "(Lorg/bukkit/event/Event;Ljava/lang/reflect/Field;)[Ljava/lang/Object;", false);
            method.visitInsn(ARETURN);
            method.visitMaxs(3, 2);
            method.visitEnd();
        }
        change: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "change", "(Lorg/bukkit/event/Event;[Ljava/lang/Object;Lch/njol/skript/classes/Changer$ChangeMode;)V", null, null);
            method.visitAnnotableParameterCount(3, false);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitVarInsn(ALOAD, 2);
            method.visitVarInsn(ALOAD, 3);
            method.visitFieldInsn(GETSTATIC, internalName, "field", "Ljava/lang/reflect/Field;");
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "change", "(Lorg/bukkit/event/Event;[Ljava/lang/Object;Lch/njol/skript/classes/Changer$ChangeMode;Ljava/lang/reflect/Field;)V", false);
            method.visitInsn(RETURN);
            method.visitMaxs(5, 4);
            method.visitEnd();
        }
        modes: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "acceptChange", "(Lch/njol/skript/classes/Changer$ChangeMode;)[Ljava/lang/Class;", "(Lch/njol/skript/classes/Changer$ChangeMode;)[Ljava/lang/Class<*>;", null);
            method.visitCode();
            method.visitFieldInsn(GETSTATIC, internalName, "modes", "[Lch/njol/skript/classes/Changer$ChangeMode;");
            method.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "sort", "([Ljava/lang/Object;)V", false);
            method.visitFieldInsn(GETSTATIC, internalName, "modes", "[Lch/njol/skript/classes/Changer$ChangeMode;");
            method.visitVarInsn(ALOAD, 1);
            method.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "binarySearch", "([Ljava/lang/Object;Ljava/lang/Object;)I", false);
            method.visitInsn(ICONST_M1);
            final Label block = new Label();
            method.visitJumpInsn(IF_ICMPLE, block);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETSTATIC, internalName, "field", "Ljava/lang/reflect/Field;");
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "getChangeType", "(Ljava/lang/reflect/Field;)[Ljava/lang/Class;", false);
            method.visitInsn(ARETURN);
            method.visitLabel(block);
            method.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            method.visitInsn(ACONST_NULL);
            method.visitInsn(ARETURN);
            method.visitMaxs(3, 2);
            method.visitEnd();
        }
        is_single: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "isSingle", "()Z", null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETSTATIC, internalName, "field", "Ljava/lang/reflect/Field;");
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "isSingle", "(Ljava/lang/reflect/Field;)Z", false);
            method.visitInsn(IRETURN);
            method.visitMaxs(2, 1);
            method.visitEnd();
        }
        return_type: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "getReturnType", "()Ljava/lang/Class;", "()Ljava/lang/Class<+TT;>;", null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETSTATIC, internalName, "field", "Ljava/lang/reflect/Field;");
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "getReturnType", "(Ljava/lang/reflect/Field;)Ljava/lang/Class;", false);
            method.visitInsn(ARETURN);
            method.visitMaxs(2, 1);
            method.visitEnd();
        }
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedPropertyExpression<Expr>>) cls;
        } catch (Throwable ex) {
            throw new SyntaxLoadException(ex);
        }
    }
    
    protected synchronized <Expr>
    Class<GeneratedSimpleExpression<Expr>> generateSimpleExpressionClass()
        throws SyntaxCreationException {
        final String namespace = "mx.kenzie.skriptlab.generated.$SimpleExpression" + this.hashCode() + "$" + (++index);
        final String internalName = namespace.replace(".", "/");
        final String superName = "mx/kenzie/skriptlab/template/GeneratedSimpleExpression";
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER,
            internalName, null, superName,
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "method", "Ljava/lang/reflect/Method;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "modes", "[Lch/njol/skript/classes/Changer$ChangeMode;", null, null).visitEnd();
        constructor: {
            final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(ALOAD, 0);
            constructor.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
            constructor.visitInsn(RETURN);
            constructor.visitMaxs(1, 1);
            constructor.visitEnd();
        }
        get: {
            final MethodVisitor method = writer.visitMethod(ACC_PROTECTED, "get", "(Lorg/bukkit/event/Event;)[Ljava/lang/Object;", "(Lorg/bukkit/event/Event;)[TT;", null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitFieldInsn(GETSTATIC, internalName, "method", "Ljava/lang/reflect/Method;");
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "get", "(Lorg/bukkit/event/Event;Ljava/lang/reflect/Method;)[Ljava/lang/Object;", false);
            method.visitInsn(ARETURN);
            method.visitMaxs(3, 2);
            method.visitEnd();
        }
        change: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "change", "(Lorg/bukkit/event/Event;[Ljava/lang/Object;Lch/njol/skript/classes/Changer$ChangeMode;)V", null, null);
            method.visitAnnotableParameterCount(3, false);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitVarInsn(ALOAD, 2);
            method.visitVarInsn(ALOAD, 3);
            method.visitFieldInsn(GETSTATIC, internalName, "method", "Ljava/lang/reflect/Method;");
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "change", "(Lorg/bukkit/event/Event;[Ljava/lang/Object;Lch/njol/skript/classes/Changer$ChangeMode;Ljava/lang/reflect/Method;)V", false);
            method.visitInsn(RETURN);
            method.visitMaxs(5, 4);
            method.visitEnd();
        }
        modes: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "acceptChange", "(Lch/njol/skript/classes/Changer$ChangeMode;)[Ljava/lang/Class;", "(Lch/njol/skript/classes/Changer$ChangeMode;)[Ljava/lang/Class<*>;", null);
            method.visitCode();
            method.visitFieldInsn(GETSTATIC, internalName, "modes", "[Lch/njol/skript/classes/Changer$ChangeMode;");
            method.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "sort", "([Ljava/lang/Object;)V", false);
            method.visitFieldInsn(GETSTATIC, internalName, "modes", "[Lch/njol/skript/classes/Changer$ChangeMode;");
            method.visitVarInsn(ALOAD, 1);
            method.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "binarySearch", "([Ljava/lang/Object;Ljava/lang/Object;)I", false);
            method.visitInsn(ICONST_M1);
            final Label block = new Label();
            method.visitJumpInsn(IF_ICMPLE, block);
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitFieldInsn(GETSTATIC, internalName, "method", "Ljava/lang/reflect/Method;");
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "getChangeType", "(Lch/njol/skript/classes/Changer$ChangeMode;Ljava/lang/reflect/Method;)[Ljava/lang/Class;", false);
            method.visitInsn(ARETURN);
            method.visitLabel(block);
            method.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            method.visitInsn(ACONST_NULL);
            method.visitInsn(ARETURN);
            method.visitMaxs(3, 2);
            method.visitEnd();
        }
        is_single: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "isSingle", "()Z", null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETSTATIC, internalName, "method", "Ljava/lang/reflect/Method;");
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "isSingle", "(Ljava/lang/reflect/Method;)Z", false);
            method.visitInsn(IRETURN);
            method.visitMaxs(2, 1);
            method.visitEnd();
        }
        return_type: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "getReturnType", "()Ljava/lang/Class;", "()Ljava/lang/Class<+TT;>;", null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETSTATIC, internalName, "method", "Ljava/lang/reflect/Method;");
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "getReturnType", "(Ljava/lang/reflect/Method;)Ljava/lang/Class;", false);
            method.visitInsn(ARETURN);
            method.visitMaxs(2, 1);
            method.visitEnd();
        }
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedSimpleExpression<Expr>>) cls;
        } catch (Throwable ex) {
            throw new SyntaxLoadException(ex);
        }
    }
    //endregion
    
    //region Guess Utility Methods
    protected Method getResetMethod(Class<?> cls) {
        try {
            return cls.getMethod("reset");
        } catch (Throwable throwable) {
            return null; // We don't want to know if there is no method
        }
    }
    
    protected Method getParseMethod(Class<?> cls) {
        try {
            return cls.getMethod("parse", String.class);
        } catch (Throwable throwable) {
            return null; // We don't want to know if there is no method
        }
    }
    //endregion
    
    //region Class Loader
    protected Class<?> loadClass(final String name, final byte[] bytes) {
        return loader.loadClass(name, bytes);
    }
    
    static class RuntimeClassLoader extends ClassLoader {
    
        protected RuntimeClassLoader(ClassLoader parent) {
            super(parent);
        }
    
        public Class<?> loadClass(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }
    //endregion

}
