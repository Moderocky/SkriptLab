package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.template.DirectCondition;
import mx.kenzie.skriptlab.template.DirectEffect;
import mx.kenzie.skriptlab.template.DirectExpression;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.Closeable;

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
        constructor.visitMethodInsn(INVOKESPECIAL, internalName, "<init>",
            "()V", false);
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
        getPatterns.visitFieldInsn(GETSTATIC, internalName, "patterns",
            "[Ljava/lang/String;");
        getPatterns.visitInsn(ARETURN);
        getPatterns.visitMaxs(1, 1);
        getPatterns.visitEnd();
        //</editor-fold>
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
            writer.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null,
                "mx/kenzie/skriptlab/internal/GeneratedCondition", null);
            writer.visitField(ACC_PUBLIC | ACC_STATIC, "handle",
                "Lmx/kenzie/skriptlab/template/DirectCondition;", null, null).visitEnd();
            writer.visitField(ACC_PUBLIC | ACC_STATIC, "patterns", "[Ljava/lang/String;", null,
                null).visitEnd();
            //</editor-fold>
            //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
            this.addConstructor("mx/kenzie/skriptlab/internal/GeneratedCondition", writer);
            //</editor-fold>
            //<editor-fold desc="Generate getHandle method" defaultstate="collapsed">
            final MethodVisitor getHandle = writer.visitMethod(ACC_PUBLIC, "getHandle",
                "()Lmx/kenzie/skriptlab/template/DirectCondition;", null, null);
            getHandle.visitCode();
            getHandle.visitFieldInsn(GETSTATIC, internalName, "handle",
                "Lmx/kenzie/skriptlab/template/DirectCondition;");
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
            this.addConstructor("mx/kenzie/skriptlab/internal/GeneratedEffect", writer);
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
            writer.visitField(ACC_PUBLIC | ACC_STATIC, "handle",
                "Lmx/kenzie/skriptlab/template/DirectExpression;", null, null).visitEnd();
            writer.visitField(ACC_PUBLIC | ACC_STATIC, "patterns", "[Ljava/lang/String;", null,
                null).visitEnd();
            //</editor-fold>
            //<editor-fold desc="Create empty constructor" defaultstate="collapsed">
            this.addConstructor("mx/kenzie/skriptlab/internal/GeneratedExpression", writer);
            //</editor-fold>
            //<editor-fold desc="Generate getHandle method" defaultstate="collapsed">
            final MethodVisitor getHandle = writer.visitMethod(ACC_PUBLIC, "getHandle",
                "()Lmx/kenzie/skriptlab/template/DirectExpression;", null, null);
            getHandle.visitCode();
            getHandle.visitFieldInsn(GETSTATIC, internalName, "handle",
                "Lmx/kenzie/skriptlab/template/DirectExpression;");
            getHandle.visitInsn(ARETURN);
            getHandle.visitMaxs(1, 1);
            getHandle.visitEnd();
            //</editor-fold>
            //<editor-fold desc="Generate getPatterns method" defaultstate="collapsed">
            this.addPatternsMethod(internalName, writer);
            //</editor-fold>
            //<editor-fold desc="Generate getReturnType method" defaultstate="collapsed">
            final MethodVisitor getReturnType = writer.visitMethod(ACC_PUBLIC, "getReturnType", "()Ljava/lang/Class;",
                null,
                null);
            getReturnType.visitCode();
            getReturnType.visitVarInsn(ALOAD, 0);
            getReturnType.visitMethodInsn(INVOKEVIRTUAL, "mx/kenzie/skriptlab/generated/" + this.className(),
                "getHandle",
                "()Lmx/kenzie/skriptlab/template/DirectExpression;", false);
            getReturnType.visitMethodInsn(INVOKEINTERFACE, "mx/kenzie/skriptlab/template/DirectExpression",
                "getReturnType",
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
    
}
