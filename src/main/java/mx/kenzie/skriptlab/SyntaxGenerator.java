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
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.base.CaseFormat;
import mx.kenzie.skriptlab.annotation.*;
import mx.kenzie.skriptlab.error.SyntaxCreationException;
import mx.kenzie.skriptlab.error.SyntaxLoadException;
import mx.kenzie.skriptlab.template.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.Type;
import org.objectweb.asm.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * A syntax generator.
 * @author Moderocky
 */
@SuppressWarnings({"DuplicatedCode", "SpellCheckingInspection", "unchecked", "UnusedLabel", "unused"})
public class SyntaxGenerator {

    protected final JavaPlugin plugin;
    protected final SkriptAddon addon;
    @SuppressWarnings("CanBeFinal")
    protected RuntimeClassLoader loader = new RuntimeClassLoader(this.getClass().getClassLoader());
    protected volatile int index = 0;
    
    //region Test Only
    /**
     * For testing class generation in a non-minecraft environment.
     */
    @Deprecated
    protected SyntaxGenerator() {
        plugin = null;
        addon = null;
    }
    //endregion
    
    /**
     * Creates a syntax generator with which your plugin can create load Skript syntax.
     * Usage can be found at {@link #registerSyntaxFrom(Class[])}
     *
     * Once this instance is garbage collected it will destroy all created classes.
     *
     * @param plugin Your plugin instance
     * @throws IllegalStateException If Skript is not present or otherwise non-functional
     */
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
    /**
     * Generates and registers syntax from all annotated members of the provided classes.
     * Classes and members with no viable annotations will be ignored.
     *
     * For annotation description and use see the README.
     *
     * @param classes The classes to scour
     */
    public void registerSyntaxFrom(final Class<?>... classes) {
        for (final Class<?> cls : classes) {
            registerSyntax(cls);
        }
    }
    
    /**
     * Generate, compile and register a class for a Skript type with property-referent objects.
     * @param typeName The unique type name, e.g. `player` or `shoe`
     * @param propertyTypes The classes for properties
     * @param propertyNames The names for properties, corresponds to the order of classes
     * @param <CompiledType> The resultant type
     * @return The type class, post-registration
     */
    public <CompiledType extends GeneratedType>
    Class<GeneratedType> createType(final String typeName, final Class<?>[] propertyTypes, final String[] propertyNames) {
        assert propertyTypes.length == propertyNames.length;
        final Class<GeneratedType> type = this.generateTypeClass(typeName, propertyTypes, propertyNames);
        this.registerSyntax(type);
        return type;
    }
    
    protected void registerSyntax(final Class<?> cls) {
        type: if (cls.isAnnotationPresent(SkriptType.class)) {
            registerType(cls);
        }
        event: if (cls.isAnnotationPresent(mx.kenzie.skriptlab.annotation.Event.class)) {
            assert Event.class.isAssignableFrom(cls);
            final mx.kenzie.skriptlab.annotation.Event event = cls.getAnnotation(mx.kenzie.skriptlab.annotation.Event.class);
            final String[] syntax = (event.value().length < 1)
                ? new String[]{CaseFormat.UPPER_CAMEL
                .to(CaseFormat.LOWER_UNDERSCORE, event.annotationType().getSimpleName())
                .replace("_", " ").replace(" event", "")}
                : event.value();
            final List<Method> values = new ArrayList<>();
            for (final Method method : cls.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(EventValue.class)) continue;
                values.add(method);
            }
            registerEvent((Class<? extends Event>) cls, values.toArray(new Method[0]), syntax);
        }
        effect: for (final Method method : cls.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Effect.class)) continue;
            method.setAccessible(true);
            final Effect effect = method.getAnnotation(Effect.class);
            final String[] syntax = (effect.value().length < 1)
                ? new String[]{CaseFormat.LOWER_CAMEL
                .to(CaseFormat.LOWER_UNDERSCORE, method.getName()).replace("_", " ")}
                : effect.value();
            registerEffect(method, syntax);
        }
        condition_method: for (final Method method : cls.getDeclaredMethods()) {
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
        condition_field: for (final Field field : cls.getDeclaredFields()) {
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
        property_expression: for (final Field field : cls.getDeclaredFields()) {
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
        simple_expression: for (final Method method : cls.getDeclaredMethods()) {
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
    
    @SuppressWarnings("NullableProblems")
    protected <ObjectType>
    void registerType(Class<ObjectType> cls) {
        if (Classes.getExactClassInfo(cls) != null) return;
        if (!cls.isAnnotationPresent(SkriptType.class)) return;
        SkriptType type = cls.getDeclaredAnnotation(SkriptType.class);
        final Documentation documentation = cls.isAnnotationPresent(Documentation.class)
            ? cls.getDeclaredAnnotation(Documentation.class)
            : null;
        final String codename = type.value().isEmpty()
            ? cls.getName().toLowerCase()
            : type.value();
        final String[] user = documentation != null
            ? documentation.user()
            : new String[]{codename};
        final String[] description = documentation != null
            ? documentation.description()
            : new String[]{"Description missing."};
        final String since = documentation != null
            ? documentation.since()
            : "Unknown";
        final String name = documentation != null
            ? documentation.name()
            : codename.toUpperCase().charAt(0) + codename.toLowerCase().substring(1);
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
                                    throwable.printStackTrace();
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
                    return codename;
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
        final Class<GeneratedPropertyCondition<?>> cls;
        if (((object instanceof Field field && field.getType() == boolean.class)
            || (object instanceof Method method && method.getReturnType() == boolean.class))
            && !Modifier.isStatic(object.getModifiers())
            && Modifier.isPublic(object.getModifiers())
        ) cls = this.generatePropertyConditionClass(object); // non-reflective version for dynamics with primitive types
        else cls = this.generatePropertyConditionClass(); // reflective version for cooky and strange versions
        assert cls != null;
        try {
            cls.getField("target").set(null, object);
            cls.getField("name").set(null, name);
            cls.getField("syntax").set(null, name);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SyntaxLoadException("Unable to set method targets.", e);
        }
        final ClassInfo<?> info = Classes.getExactClassInfo(object.getDeclaringClass());
        assert info != null;
        PropertyCondition.register(cls, type, name, info.getCodeName());
    }
    
    @SuppressWarnings("unchecked")
    protected <Expr>
    void registerPropertyExpression(final Field field, final String name) {
        final Class<GeneratedPropertyExpression<Expr>> cls = this.generatePropertyExpressionClass(field);
        assert cls != null;
        final Changer.ChangeMode[] modes = field.isAnnotationPresent(Property.class)
            ? field.getAnnotation(Property.class).allowedModes()
            : new Changer.ChangeMode[0];
        try {
            cls.getField("field").set(null, field);
            cls.getField("modes").set(null, modes);
            cls.getField("syntax").set(null, name);
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
        final Class<GeneratedSimpleExpression<Expr>> cls;
        if (Modifier.isPublic(method.getModifiers())) {
            cls = this.generateSimpleExpressionClass(method); // Inline version for public cases
        } else {
            cls = this.generateSimpleExpressionClass(); // Reflective version
        }
        assert cls != null;
        final Changer.ChangeMode[] modes = method.isAnnotationPresent(Expression.class)
            ? method.getAnnotation(Expression.class).allowedModes()
            : new Changer.ChangeMode[0];
        try {
            cls.getField("method").set(null, method);
            cls.getField("modes").set(null, modes);
            cls.getField("syntax").set(null, syntax[0]);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SyntaxLoadException("Unable to set method targets.", e);
        }
        Skript.registerExpression(cls, (Class<Expr>) GeneratedExpression.ensureWrapper(method.getReturnType()), ExpressionType.PROPERTY,
            syntax);
    }
    
    protected void registerEffect(final Method method, final String... syntax) {
        final Class<GeneratedEffect> cls = this.generateEffectClass(method);
        assert cls != null;
        try {
            cls.getField("method").set(null, method);
            cls.getField("syntax").set(null, syntax[0]);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SyntaxLoadException("Unable to set method targets.", e);
        }
        Skript.registerEffect(cls, syntax);
    }
    
    protected <EventType extends Event, Value>
    void registerEvent(final Class<EventType> event, final Method[] values, final String... syntax) {
        final Class<GeneratedEvent> cls = this.generateEventClass();
        assert cls != null;
        try {
            cls.getField("event").set(null, event);
            cls.getField("syntax").set(null, syntax[0]);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SyntaxLoadException("Unable to set event class targets.", e);
        }
        Skript.registerEvent(event.getSimpleName(), cls, event, syntax);
        for (Method value : values) {
            try {
                final Class<?> get = this.generateGetterClass(value);
                final Getter<Value, EventType> getter = (Getter<Value, EventType>) get.newInstance();
                EventValues.registerEventValue(event, (Class<Value>) value.getReturnType(), getter, 0);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new SyntaxCreationException(e);
            }
        }
    }
    //endregion
    
    //region Class Generation
    protected synchronized <CompiledType extends GeneratedType>
    Class<CompiledType> generateTypeClass(final String typeName, final Class<?>[] propertyTypes, final String[] propertyNames)
        throws SyntaxCreationException {
        final String namespace = "mx.kenzie.skriptlab.generated.$Type" + this.hashCode() + "$" + typeName;
        final String internalName = namespace.replace(".", "/");
        final String superName = this.internalName(GeneratedType.class);
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER, internalName, null, superName,null);
        final AnnotationVisitor annotation = writer.visitAnnotation(SkriptType.class.descriptorString(), true);
        annotation.visit("value", typeName);
        annotation.visitEnd();
        fields: {
            for (int i = 0; i < propertyTypes.length; i++) {
                final Class<?> type = propertyTypes[i];
                final String name = propertyNames[i];
                final FieldVisitor field = writer.visitField(ACC_PUBLIC, name, type.descriptorString(), null, null);
                field.visitAnnotation(Property.class.descriptorString(), true).visitEnd();
                field.visitEnd();
            }
        }
        constructor: addEmptyLoadConstructor(writer, superName);
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<CompiledType>) cls;
        } catch (Throwable ex) {
            throw new SyntaxCreationException(ex);
        }
    }
    
    protected synchronized <EventType extends Event, Value>
    Class<? extends Getter<Value, EventType>> generateGetterClass(final Method binder)
        throws SyntaxCreationException {
        final Class<EventType> event = (Class<EventType>) binder.getDeclaringClass();
        final Class<Value> value = (Class<Value>) binder.getReturnType();
        final String namespace = "mx.kenzie.skriptlab.generated.$Getter" + this.hashCode() + "$" + (++index);
        final String internalName = namespace.replace(".", "/");
        final String superName = "ch/njol/skript/util/Getter";
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER,
            internalName, "Lch/njol/skript/util/Getter<" + value.descriptorString() + event
                .descriptorString() + ">;", superName,
            null); // this handles illegal primitive overloading
        constructor: addEmptyLoadConstructor(writer, superName);
        get_virtual: {
            final MethodVisitor method = writer
                .visitMethod(ACC_PUBLIC, "get", "(" + event.descriptorString() + ")" + value
                    .descriptorString(), null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 1);
            method.visitMethodInsn(INVOKEVIRTUAL, this.internalName(event), binder.getName(), "()" + value
                .descriptorString(), false);
            method.visitInsn(ARETURN);
            method.visitMaxs(1, 2);
            method.visitEnd();
        }
        get_synthetic: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitTypeInsn(CHECKCAST, this.internalName(event));
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "get", "(" + event.descriptorString() + ")" + value
                .descriptorString(), false);
            method.visitInsn(ARETURN);
            method.visitMaxs(2, 2);
            method.visitEnd();
        }
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<? extends Getter<Value, EventType>>) cls;
        } catch (Throwable ex) {
            throw new SyntaxCreationException(ex);
        }
    }
    
    protected synchronized Class<GeneratedEvent> generateEventClass()
        throws SyntaxCreationException {
        final String namespace = "mx.kenzie.skriptlab.generated.$Event" + this.hashCode() + "$" + (++index);
        final String internalName = namespace.replace(".", "/");
        final String superName = "mx/kenzie/skriptlab/template/GeneratedEvent";
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER,
            internalName, null, superName,
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "syntax", "Ljava/lang/String;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "event", "Ljava/lang/Class;", "Ljava/lang/Class<+Lorg/bukkit/event/Event;>;", null).visitEnd();
        constructor: addEmptyLoadConstructor(writer, superName);
        check: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "check", "(Lorg/bukkit/event/Event;)Z", null, null);
            method.visitCode();
            method.visitFieldInsn(GETSTATIC, internalName, "event", "Ljava/lang/Class;");
            method.visitVarInsn(ALOAD, 1);
            method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isInstance", "(Ljava/lang/Object;)Z", false);
            method.visitInsn(IRETURN);
            method.visitMaxs(2, 2);
            method.visitEnd();
        }
        event_class: {
            final MethodVisitor method = writer.visitMethod(ACC_PROTECTED, "getEventClass", "()Ljava/lang/Class;", "()Ljava/lang/Class<+Lorg/bukkit/event/Event;>;", null);
            method.visitCode();
            method.visitFieldInsn(GETSTATIC, internalName, "event", "Ljava/lang/Class;");
            method.visitInsn(ARETURN);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }
        syntax: addSyntaxGetter(writer, internalName);
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedEvent>) cls;
        } catch (Throwable ex) {
            throw new SyntaxCreationException(ex);
        }
    }
    
    protected synchronized Class<GeneratedEffect> generateEffectClass(final Method binder)
        throws SyntaxCreationException {
        final String namespace = "mx.kenzie.skriptlab.generated.$Effect" + this.hashCode() + "$" + (++index);
        final String internalName = namespace.replace(".", "/");
        final String superName = "mx/kenzie/skriptlab/template/GeneratedEffect";
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER,
            internalName, null, superName,
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "syntax", "Ljava/lang/String;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "method", "Ljava/lang/reflect/Method;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "caller", "Ljava/lang/Object;", null, null).visitEnd();
        constructor: addEmptyLoadConstructor(writer, superName);
        execute: {
            final MethodVisitor method = writer
                .visitMethod(ACC_PROTECTED, "execute", "(Lorg/bukkit/event/Event;)V", null, null);
            method.visitCode();
            if (this.isSimple(binder)) { // public static no-parameters
                method.visitMethodInsn(INVOKESTATIC, this.internalName(binder.getDeclaringClass()), binder
                    .getName(), "()" + binder.getReturnType().descriptorString(), false);
                method.visitInsn(RETURN);
                method.visitMaxs(1, 1);
            } else if (isSimpleDynamic(binder)) { // public dynamic no-parameters
                method.visitVarInsn(ALOAD, 0);
                method.visitVarInsn(ALOAD, 1);
                method
                    .visitMethodInsn(INVOKEVIRTUAL, internalName, "getConvertedExpressions", "(Lorg/bukkit/event/Event;)Ljava/util/List;", false);
                method.visitInsn(ICONST_0);
                method.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "remove", "(I)Ljava/lang/Object;", true);
                method.visitTypeInsn(CHECKCAST, this.internalName(binder.getDeclaringClass())); // get object target
                method.visitMethodInsn(INVOKEVIRTUAL, this.internalName(binder.getDeclaringClass()), binder
                    .getName(), "()" + binder.getReturnType().descriptorString(), false);
                method.visitInsn(RETURN);
                method.visitMaxs(2, 2);
            } else if (binder.getParameterTypes().length > 0
                && Modifier.isPublic(binder.getModifiers())) { // public and parameters
                final boolean dynamic = !Modifier.isStatic(binder.getModifiers());
                method.visitVarInsn(ALOAD, 0);
                method.visitVarInsn(ALOAD, 1);
                method.visitMethodInsn(INVOKEVIRTUAL, internalName, "getConvertedExpressions", "(Lorg/bukkit/event/Event;)Ljava/util/List;", false);
                method.visitVarInsn(ASTORE, 2);
                if (dynamic) { // Trough the first input as the object
                    method.visitVarInsn(ALOAD, 2);
                    method.visitInsn(ICONST_0);
                    method.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "remove", "(I)Ljava/lang/Object;", true);
                    method.visitTypeInsn(CHECKCAST, this.internalName(binder.getDeclaringClass()));
                }
                final StringBuilder builder = new StringBuilder("(");
                for (Class<?> type : binder.getParameterTypes()) {
                    method.visitVarInsn(ALOAD, 2);
                    method.visitInsn(ICONST_0);
                    method.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "remove", "(I)Ljava/lang/Object;", true);
                    method.visitTypeInsn(CHECKCAST, this.internalName(type));
                    builder.append(type.descriptorString());
                }
                builder.append(")").append(binder.getReturnType().descriptorString());
                if (dynamic) {
                    method.visitMethodInsn(INVOKEVIRTUAL, this.internalName(binder.getDeclaringClass()), binder
                        .getName(), builder.toString(), false);
                } else {
                    method.visitMethodInsn(INVOKESTATIC, this.internalName(binder.getDeclaringClass()), binder
                        .getName(), builder.toString(), false);
                }
                method.visitInsn(RETURN);
                method.visitMaxs(3 + binder.getParameterTypes().length, 3);
            } else { // Can't deal with unknown parameter types, cede to reflective case
                method.visitVarInsn(ALOAD, 0);
                method.visitVarInsn(ALOAD, 1);
                method.visitFieldInsn(GETSTATIC, internalName, "method", "Ljava/lang/reflect/Method;");
                method.visitFieldInsn(GETSTATIC, internalName, "caller", "Ljava/lang/Object;");
                method
                    .visitMethodInsn(INVOKESPECIAL, superName, "execute", "(Lorg/bukkit/event/Event;Ljava/lang/reflect/Method;Ljava/lang/Object;)V", false);
                method.visitInsn(RETURN);
                method.visitMaxs(4, 2);
            }
            method.visitEnd();
        }
        syntax: addSyntaxGetter(writer, internalName);
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedEffect>) cls;
        } catch (Throwable ex) {
            throw new SyntaxCreationException(ex);
        }
    }
    
    protected synchronized Class<GeneratedPropertyCondition<?>> generatePropertyConditionClass(Object target)
        throws SyntaxCreationException {
        final String namespace = "mx.kenzie.skriptlab.generated.$PropertyCondition" + this.hashCode() + "$" + (++index);
        final String internalName = namespace.replace(".", "/");
        final String superName = "mx/kenzie/skriptlab/template/GeneratedPropertyCondition";
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER,
            internalName, null, superName,
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "syntax", "Ljava/lang/String;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "target", "Ljava/lang/Object;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "name", "Ljava/lang/String;", null, null).visitEnd();
        constructor: addEmptyLoadConstructor(writer, superName);
        check_field: {
            if (!(target instanceof Field binder)) break check_field;
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "check", "(Ljava/lang/Object;)Z", "(TT;)Z", null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 1);
            method.visitTypeInsn(CHECKCAST, this.internalName(binder.getDeclaringClass()));
            method.visitFieldInsn(GETFIELD, this.internalName(binder.getDeclaringClass()), binder.getName(), "Z");
            method.visitInsn(IRETURN);
            method.visitMaxs(1, 2);
            method.visitEnd();
        }
        check_method: {
            if (!(target instanceof Method binder)) break check_method;
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "check", "(Ljava/lang/Object;)Z", "(TT;)Z", null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 1);
            method.visitTypeInsn(CHECKCAST, this.internalName(binder.getDeclaringClass()));
            method.visitMethodInsn(INVOKEVIRTUAL, this.internalName(binder.getDeclaringClass()), binder.getName(), "()Z", false);
            method.visitInsn(IRETURN);
            method.visitMaxs(1, 2);
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
        syntax: addSyntaxGetter(writer, internalName);
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedPropertyCondition<?>>) cls;
        } catch (Throwable ex) {
            throw new SyntaxCreationException(ex);
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
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "syntax", "Ljava/lang/String;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "target", "Ljava/lang/Object;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "name", "Ljava/lang/String;", null, null).visitEnd();
        constructor: addEmptyLoadConstructor(writer, superName);
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
        syntax: addSyntaxGetter(writer, internalName);
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedPropertyCondition<?>>) cls;
        } catch (Throwable ex) {
            throw new SyntaxCreationException(ex);
        }
    }
    
    protected synchronized <Expr>
    Class<GeneratedPropertyExpression<Expr>> generatePropertyExpressionClass(Field binder)
        throws SyntaxCreationException {
        final String namespace = "mx.kenzie.skriptlab.generated.$PropertyExpression" + this.hashCode() + "$" + (++index);
        final String internalName = namespace.replace(".", "/");
        final String superName = "mx/kenzie/skriptlab/template/GeneratedPropertyExpression";
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER,
            internalName, null, superName,
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "syntax", "Ljava/lang/String;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "field", "Ljava/lang/reflect/Field;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "modes", "[Lch/njol/skript/classes/Changer$ChangeMode;", null, null).visitEnd();
        constructor: addEmptyLoadConstructor(writer, superName);
        if (isSimple(binder)) directPropertyAccess(writer, internalName, superName, binder);
        else reflectivePropertyAccess(writer, internalName, superName);
        syntax: addSyntaxGetter(writer, internalName);
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedPropertyExpression<Expr>>) cls;
        } catch (Throwable ex) {
            throw new SyntaxCreationException(ex);
        }
    }
    
    /**
     * Used when the property field is public and:
     *  - a primitive number type
     *  - a singular object
     */
    private void directPropertyAccess(ClassWriter writer, String internalName, String superName, Field binder) {
        get: {
            final MethodVisitor method = writer.visitMethod(ACC_PROTECTED, "get", "(Lorg/bukkit/event/Event;)[Ljava/lang/Object;", "(Lorg/bukkit/event/Event;)[TT;", null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "getConvertedExpressions", "(Lorg/bukkit/event/Event;)Ljava/util/List;", false);
            method.visitInsn(ICONST_0);
            method.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "remove", "(I)Ljava/lang/Object;", true);
            method.visitTypeInsn(CHECKCAST, this.internalName(binder.getDeclaringClass()));
            method.visitVarInsn(ALOAD, 0);
            method.visitInsn(SWAP);
            method.visitFieldInsn(GETFIELD, this.internalName(binder.getDeclaringClass()), binder.getName(), binder.getType().descriptorString());
            method.visitMethodInsn(INVOKEVIRTUAL, internalName, "wrapArray", "(" + binder.getType().descriptorString() + ")[Ljava/lang/Object;", false);
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
            if (Modifier.isFinal(binder.getModifiers())) finalChangeTypes(method, internalName, binder);
            else if (isNumber(binder.getType())) numberChangeTypes(method, internalName, binder);
            else simpleChangeTypes(method, internalName, binder);
            method.visitEnd();
        }
        is_single: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "isSingle", "()Z", null, null);
            method.visitCode();
            method.visitInsn(ICONST_1);
            method.visitInsn(IRETURN);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }
        return_type: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "getReturnType", "()Ljava/lang/Class;", "()Ljava/lang/Class<+TT;>;", null);
            method.visitCode();
            method.visitLdcInsn(Type.getType(this.ensureWrapperClass(binder.getType()).descriptorString()));
            method.visitInsn(ARETURN);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }
    }
    
    private void finalChangeTypes(MethodVisitor method, String internalName, Field binder) {
        method.visitInsn(ACONST_NULL); // If the field is final we can't have changers
        method.visitInsn(ARETURN);
        method.visitMaxs(1, 1);
    }
    
    private void simpleChangeTypes(MethodVisitor method, String internalName, Field binder) {
        final Label exit = new Label();
        method.visitVarInsn(ALOAD, 1);
        method.visitFieldInsn(GETSTATIC, "ch/njol/skript/classes/Changer$ChangeMode", "SET", "Lch/njol/skript/classes/Changer$ChangeMode;");
        method.visitJumpInsn(IF_ACMPNE, exit); // not set ? exit null
        method.visitInsn(ICONST_1); // array length 1
        method.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        method.visitInsn(DUP);
        method.visitInsn(ICONST_0); // array index to store
        method.visitVarInsn(ALOAD, 0); // assume return type is the change type (please lol)
        method.visitMethodInsn(INVOKEVIRTUAL, internalName, "getReturnType", "()Ljava/lang/Class;", false);
        method.visitInsn(AASTORE);
        method.visitInsn(ARETURN);
        method.visitLabel(exit);
        method.visitFrame(F_SAME, 0, null, 0, null);
        method.visitInsn(ACONST_NULL);
        method.visitInsn(ARETURN);
        method.visitMaxs(4, 2);
    }
    
    private void numberChangeTypes(MethodVisitor method, String internalName, Field binder) {
        final Label allowed = new Label(), exit = new Label();
        method.visitVarInsn(ALOAD, 1); // could DUP instead of repeat load but don't want frame conflict with jump
        method.visitFieldInsn(GETSTATIC, "ch/njol/skript/classes/Changer$ChangeMode", "SET", "Lch/njol/skript/classes/Changer$ChangeMode;");
        method.visitJumpInsn(IF_ACMPEQ, allowed);
        method.visitVarInsn(ALOAD, 1);
        method.visitFieldInsn(GETSTATIC, "ch/njol/skript/classes/Changer$ChangeMode", "ADD", "Lch/njol/skript/classes/Changer$ChangeMode;");
        method.visitJumpInsn(IF_ACMPEQ, allowed);
        method.visitVarInsn(ALOAD, 1);
        method.visitFieldInsn(GETSTATIC, "ch/njol/skript/classes/Changer$ChangeMode", "REMOVE", "Lch/njol/skript/classes/Changer$ChangeMode;");
        method.visitJumpInsn(IF_ACMPNE, exit); // not remove ? exit otherwise run into allowed types
        method.visitLabel(allowed); // allowed changer types
        method.visitFrame(F_SAME, 0, null, 0, null);
        method.visitInsn(ICONST_1); // array length
        method.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        method.visitInsn(DUP);
        method.visitInsn(ICONST_0); // array index for store
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKEVIRTUAL, internalName, "getReturnType", "()Ljava/lang/Class;", false);
        method.visitInsn(AASTORE);
        method.visitInsn(ARETURN);
        method.visitLabel(exit); // not matched type
        method.visitFrame(F_SAME, 0, null, 0, null);
        method.visitInsn(ACONST_NULL);
        method.visitInsn(ARETURN);
        method.visitMaxs(4, 2);
    }
    
    /**
     * Used when the property field is complex or private.
     */
    private void reflectivePropertyAccess(ClassWriter writer, String internalName, String superName) {
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
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "syntax", "Ljava/lang/String;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "method", "Ljava/lang/reflect/Method;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "modes", "[Lch/njol/skript/classes/Changer$ChangeMode;", null, null).visitEnd();
        constructor: addEmptyLoadConstructor(writer, superName);
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
        syntax: addSyntaxGetter(writer, internalName);
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedSimpleExpression<Expr>>) cls;
        } catch (Throwable ex) {
            throw new SyntaxCreationException(ex);
        }
    }
    
    /**
     * An alternative generator that takes an explicit method hook to avoid reflection.
     */
    protected synchronized <Expr>
    Class<GeneratedSimpleExpression<Expr>> generateSimpleExpressionClass(Method binder)
        throws SyntaxCreationException {
        final String namespace = "mx.kenzie.skriptlab.generated.$SimpleExpression" + this.hashCode() + "$" + (++index);
        final String internalName = namespace.replace(".", "/");
        final String superName = "mx/kenzie/skriptlab/template/GeneratedSimpleExpression";
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC | ACC_SUPER,
            internalName, null, superName,
            null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "syntax", "Ljava/lang/String;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "method", "Ljava/lang/reflect/Method;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "modes", "[Lch/njol/skript/classes/Changer$ChangeMode;", null, null)
            .visitEnd();
        constructor: addEmptyLoadConstructor(writer, superName);
        get: {
            final MethodVisitor method = writer
                .visitMethod(ACC_PROTECTED, "get", "(Lorg/bukkit/event/Event;)[Ljava/lang/Object;", "(Lorg/bukkit/event/Event;)[TT;", null);
            method.visitCode();
            if (isSimple(binder)) {
                method.visitMethodInsn(INVOKESTATIC, this.internalName(binder.getDeclaringClass()), binder
                    .getName(), "()" + binder.getReturnType().descriptorString(), false);
                method.visitVarInsn(ASTORE, 2);
                method.visitVarInsn(ALOAD, 0);
                method.visitVarInsn(ALOAD, 2);
                method
                    .visitMethodInsn(INVOKEVIRTUAL, internalName, "wrapArray", "(Ljava/lang/Object;)[Ljava/lang/Object;", false);
                method.visitInsn(ARETURN);
                method.visitMaxs(3, 2);
            } else {
                final boolean dynamic = !Modifier.isStatic(binder.getModifiers());
                method.visitVarInsn(ALOAD, 0); // Trough this
                method.visitVarInsn(ALOAD, 1); // Trough event
                method
                    .visitMethodInsn(INVOKEVIRTUAL, internalName, "getConvertedExpressions", "(Lorg/bukkit/event/Event;)Ljava/util/List;", false);
                method.visitVarInsn(ASTORE, 2); // this, event -> Object list goes in slot 2
                if (dynamic) { // Trough the first input as the object
                    method.visitVarInsn(ALOAD, 2);
                    method.visitInsn(ICONST_0);
                    method.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "remove", "(I)Ljava/lang/Object;", true);
                    method.visitTypeInsn(CHECKCAST, this.internalName(binder.getDeclaringClass()));
                }
                final StringBuilder builder = new StringBuilder("(");
                for (Class<?> type : binder.getParameterTypes()) { // If empty then ()
                    method.visitVarInsn(ALOAD, 2);
                    method.visitInsn(ICONST_0);
                    method.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "remove", "(I)Ljava/lang/Object;", true);
                    method.visitTypeInsn(CHECKCAST, this.internalName(type));
                    builder.append(type.descriptorString());
                }
                builder.append(")").append(binder.getReturnType().descriptorString());
                if (dynamic) {
                    method.visitMethodInsn(INVOKEVIRTUAL, this.internalName(binder.getDeclaringClass()), binder
                        .getName(), builder.toString(), false);
                } else {
                    method.visitMethodInsn(INVOKESTATIC, this.internalName(binder.getDeclaringClass()), binder
                        .getName(), builder.toString(), false);
                }
                method.visitTypeInsn(CHECKCAST, this.internalName(Object.class));
                method.visitVarInsn(ALOAD, 0); // Trough this
                method.visitInsn(SWAP); // This, object
                method
                    .visitMethodInsn(INVOKEVIRTUAL, internalName, "wrapArray", "(Ljava/lang/Object;)[Ljava/lang/Object;", false);
                method.visitInsn(ARETURN); // Result is troughed and returned
                method.visitMaxs(3 + binder.getParameterTypes().length, 3);
            }
            method.visitEnd();
        }
        change: {
            final MethodVisitor method = writer
                .visitMethod(ACC_PUBLIC, "change", "(Lorg/bukkit/event/Event;[Ljava/lang/Object;Lch/njol/skript/classes/Changer$ChangeMode;)V", null, null);
            method.visitCode();
            method.visitInsn(RETURN);
            method.visitMaxs(0, 0);
            method.visitEnd();
        }
        accept_change: {
            final MethodVisitor method = writer
                .visitMethod(ACC_PUBLIC, "acceptChange", "(Lch/njol/skript/classes/Changer$ChangeMode;)[Ljava/lang/Class;", "(Lch/njol/skript/classes/Changer$ChangeMode;)[Ljava/lang/Class<*>;", null);
            method.visitCode();
            method.visitInsn(ACONST_NULL);
            method.visitInsn(ARETURN);
            method.visitMaxs(1, 0);
            method.visitEnd();
        }
        is_single: {
            final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "isSingle", "()Z", null, null);
            method.visitCode();
            method.visitInsn(binder.getReturnType().isArray()
                ? ICONST_0
                : Collection.class.isAssignableFrom(binder.getReturnType())
                ? ICONST_0
                : ICONST_1
            );
            method.visitInsn(IRETURN);
            method.visitMaxs(1, 0);
            method.visitEnd();
        }
        return_type: {
            final MethodVisitor method = writer
                .visitMethod(ACC_PUBLIC, "getReturnType", "()Ljava/lang/Class;", "()Ljava/lang/Class<+TT;>;", null);
            method.visitCode();
            method.visitLdcInsn(Type.getType(binder.getReturnType().descriptorString()));
            method.visitInsn(ARETURN);
            method.visitMaxs(1, 0);
            method.visitEnd();
        }
        syntax: addSyntaxGetter(writer, internalName);
        writer.visitEnd();
        final byte[] bytecode = writer.toByteArray();
        try {
            final Class<?> cls = loadClass(namespace, bytecode);
            return (Class<GeneratedSimpleExpression<Expr>>) cls;
        } catch (Throwable ex) {
            throw new SyntaxCreationException(ex);
        }
    }
    
    private void addEmptyLoadConstructor(ClassWriter writer, String superName) {
        final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
    }
    
    private void addSyntaxGetter(ClassWriter writer, String internalName) {
        final MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "getSyntax", "()Ljava/lang/String;", null, null);
        method.visitCode();
        method.visitFieldInsn(GETSTATIC, internalName, "syntax", "Ljava/lang/String;");
        method.visitInsn(ARETURN);
        method.visitMaxs(1, 1);
        method.visitEnd();
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
    
    protected String internalName(Class<?> cls) {
        return cls.getName().replace(".", "/");
    }
    
    protected boolean isSimple(Field field) {
        return !Modifier.isStatic(field.getModifiers())
            && Modifier.isPublic(field.getModifiers())
            && !Collection.class.isAssignableFrom(field.getType());
    }
    
    protected boolean isSimple(Method method) {
        return method.getParameterTypes().length < 1
            && Modifier.isStatic(method.getModifiers())
            && Modifier.isPublic(method.getModifiers());
    }
    
    protected boolean isSimpleDynamic(Method method) {
        return method.getParameterTypes().length < 1
            && Modifier.isPublic(method.getModifiers());
    }
    
    protected Class<?> ensureWrapperClass(Class<?> type) {
        if (type == int.class
            || type == long.class
            || type == double.class
            || type == short.class
            || type == byte.class
            || type == float.class
        ) return Number.class;
        if (type == boolean.class
        ) return Boolean.class;
        return type;
    }
    
    protected boolean isNumber(Class<?> type) {
        return (Number.class.isAssignableFrom(type)
            || type == byte.class
            || type == short.class
            || type == int.class
            || type == long.class
            || type == float.class
            || type == double.class
        );
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
