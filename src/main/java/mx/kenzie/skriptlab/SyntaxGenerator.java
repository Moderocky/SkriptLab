package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.internal.GeneratedEffect;
import mx.kenzie.skriptlab.template.DirectEffect;
import org.bukkit.Bukkit;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

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
    
}

public class SyntaxGenerator extends ClassLoader {
    
    private final AtomicInteger generatedNumber;
    
    public SyntaxGenerator() {
        super(SyntaxGenerator.class.getClassLoader());
        this.generatedNumber = new AtomicInteger(0);
    }
    
    @SuppressWarnings("unchecked")
    public Syntax createEffect(DirectEffect handler, String... patterns) {
        final String name = "GeneratedEffect" + generatedNumber.incrementAndGet();
        try (final EffectMaker maker = new EffectMaker(name, handler, patterns)) {
            final Class<? extends GeneratedEffect> type = (Class<? extends GeneratedEffect>) maker.make(this);
            final Field handleField = type.getDeclaredField("handle");
            handleField.set(null, handler);
            final Field patternsField = type.getDeclaredField("patterns");
            patternsField.set(null, patterns);
            return new Syntax(type, patterns);
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }
    
    protected Class<?> loadClass(String name, byte[] bytecode) {
        return super.defineClass("mx.kenzie.skriptlab.generated." + name, bytecode, 0, bytecode.length);
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
        writer.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null,
            "mx/kenzie/skriptlab/internal/GeneratedEffect", null);
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "handle",
            "Lmx/kenzie/skriptlab/template/DirectEffect;", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "patterns", "[Ljava/lang/String;", null,
            null).visitEnd();
        //</editor-fold>
        //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
        final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "mx/kenzie/skriptlab/internal/GeneratedEffect", "<init>",
            "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        //</editor-fold>
        //<editor-fold desc="Generate getHandle method" defaultstate="collapsed">
        final MethodVisitor getHandle = writer.visitMethod(ACC_PUBLIC, "getHandle",
            "()Lmx/kenzie/skriptlab/template/DirectEffect;", null, null);
        getHandle.visitCode();
        getHandle.visitFieldInsn(GETSTATIC, internalName, "handle",
            "Lmx/kenzie/skriptlab/template/DirectEffect;");
        getHandle.visitInsn(ARETURN);
        getHandle.visitMaxs(1, 1);
        getHandle.visitEnd();
        //</editor-fold>
        //<editor-fold desc="Generate getPatterns method" defaultstate="collapsed">
        final MethodVisitor getPatterns = writer.visitMethod(ACC_PUBLIC, "getPatterns", "()[Ljava/lang/String;", null,
            null);
        getPatterns.visitCode();
        getPatterns.visitFieldInsn(GETSTATIC, internalName, "patterns",
            "[Ljava/lang/String;");
        getPatterns.visitInsn(ARETURN);
        getPatterns.visitMaxs(1, 1);
        getPatterns.visitEnd();
        //</editor-fold>
        writer.visitEnd();
        return writer.toByteArray();
    }
    
}
