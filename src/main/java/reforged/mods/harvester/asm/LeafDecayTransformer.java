package reforged.mods.harvester.asm;

import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import reforged.mods.harvester.HarvesterMod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeafDecayTransformer implements IClassTransformer {

    private final HashMap<String, String> obfStrings;

    public LeafDecayTransformer() {
        this.obfStrings = new HashMap<String, String>();
        this.obfStrings.put("blockClassName", "amq"); // net.minecraft.block.Block
        this.obfStrings.put("methodName", "a"); // onNeighborBlockChange
    }

    @Override
    public byte[] transform(String obfName, byte[] basicClass) {
        return obfName.equals("amq") ? transform(basicClass, this.obfStrings) : basicClass;
    }

    public byte[] transform(byte[] basicClass, Map<String, String> names) {
        HarvesterMod.LOGGER.info("[Leaf Decay - ASM] Transforming Classes!");
        HarvesterMod.LOGGER.info("[Leaf Decay - ASM] Class Transformation running on " + names.get("blockClassName") + "...");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);
        List<MethodNode> methods = classNode.methods;
        for (MethodNode methodNode : methods) {
            if (methodNode.name.equals(names.get("methodName")) && methodNode.desc.equals("(Lyc;IIII)V")) {
                HarvesterMod.LOGGER.info("[Leaf Decay - ASM] Found target method " + methodNode.name + methodNode.desc + "! Searching for landmarks...");
                HarvesterMod.LOGGER.info("[Leaf Decay - ASM] Patching method " + names.get("blockClassName") + "/" + methodNode.name + methodNode.desc + "...");
                LabelNode lmm1Node = new LabelNode(new Label());
                InsnList toInject = new InsnList();
                toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
                toInject.add(new VarInsnNode(Opcodes.ILOAD, 2));
                toInject.add(new VarInsnNode(Opcodes.ILOAD, 3));
                toInject.add(new VarInsnNode(Opcodes.ILOAD, 4));
                toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "reforged/mods/harvester/events/LeafDecayEvent", "onLeafDecay", "(Lyc;III)V"));
                toInject.add(lmm1Node);
                methodNode.instructions.insert(toInject);
                HarvesterMod.LOGGER.info("[Leaf Decay - ASM] Method " + names.get("blockClassName") + "/" + methodNode.name + methodNode.desc + " patched!");
                HarvesterMod.LOGGER.info("[Leaf Decay - ASM] Patching Complete!");
                break;
            }
        }
        ClassWriter writer = new ClassWriter(1);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
