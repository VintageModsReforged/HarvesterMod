package reforged.mods.harvester.asm;

import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import reforged.mods.harvester.HarvesterMod;

public class LeafDecayTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, byte[] basicClass) {
        return name.equals("amy") ? transform(basicClass) : basicClass;
    }

    private byte[] transform(byte[] basicClass) {
        HarvesterMod.LOGGER.info("Patching Leaves Block!");
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        // onNeighborBlockChange
        MethodVisitor methodVisitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "a", "(Lyc;IIII)V", null, null);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1); // Load 'world'
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 2); // Load 'x'
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 3); // Load 'y'
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 4); // Load 'z'
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "reforged/mods/harvester/asm/LeafDecayHandler", "handleLeafDecay", "(Lyc;III)V");
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(640, label0);
        methodVisitor.visitInsn(Opcodes.RETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLocalVariable("this", "Lreforged/mods/harvester/asm/LeafDecayTransformer;", null, label0, label1, 0);
        methodVisitor.visitLocalVariable("par1World", "Lnet/minecraft/world/World;", null, label0, label1, 1);
        methodVisitor.visitLocalVariable("par2", "I", null, label0, label1, 2);
        methodVisitor.visitLocalVariable("par3", "I", null, label0, label1, 3);
        methodVisitor.visitLocalVariable("par4", "I", null, label0, label1, 4);
        methodVisitor.visitLocalVariable("par5", "I", null, label0, label1, 5);
        methodVisitor.visitMaxs(0, 6);
        methodVisitor.visitEnd();
        return writer.toByteArray();
    }
}
