package reforged.mods.harvester.asm;

import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import reforged.mods.harvester.HarvesterMod;

import java.util.HashMap;
import java.util.Iterator;

public class ItemInWorldManagerTransformer implements IClassTransformer {

    private final HashMap<String, String> obfStrings;

    public ItemInWorldManagerTransformer() {
        this.obfStrings = new HashMap<String, String>();
        this.obfStrings.put("className", "ir");
        this.obfStrings.put("javaClassName", "ir");
        this.obfStrings.put("targetMethodName", "d");
        this.obfStrings.put("worldFieldName", "a");
        this.obfStrings.put("entityPlayerFieldName", "b");
        this.obfStrings.put("worldJavaClassName", "yc");
        this.obfStrings.put("getBlockMetadataMethodName", "h");
        this.obfStrings.put("blockJavaClassName", "amq");
        this.obfStrings.put("blocksListFieldName", "p");
        this.obfStrings.put("entityPlayerJavaClassName", "qx");
        this.obfStrings.put("entityPlayerMPJavaClassName", "iq");
    }

    @Override
    public byte[] transform(String obfName, byte[] basicClass) {
        return "ir".equals(obfName) ? transformItemInWorldManager(basicClass, this.obfStrings) : basicClass;
    }

    private byte[] transformItemInWorldManager(byte[] bytes, HashMap<String, String> names) {
        HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Transforming Classes!");
        HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Class Transformation running on " + names.get("javaClassName") + "...");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (m.name.equals(names.get("targetMethodName")) && m.desc.equals("(III)Z")) {
                HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Found target method " + m.name + m.desc + "! Searching for landmarks...");
                int blockIndex = 4;
                int mdIndex = 5;
                for (int index = 0; index < m.instructions.size(); index++) {
                    if (m.instructions.get(index).getType() == 4) {
                        FieldInsnNode blocksListNode = (FieldInsnNode)m.instructions.get(index);
                        if (blocksListNode.owner.equals(names.get("blockJavaClassName")) && blocksListNode.name.equals(names.get("blocksListFieldName"))) {
                            int offset = 1;
                            while (m.instructions.get(index + offset).getOpcode() != Opcodes.ASTORE)
                                offset++;
                            HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Found Block object ASTORE Node at " + (index + offset));
                            VarInsnNode blockNode = (VarInsnNode)m.instructions.get(index + offset);
                            blockIndex = blockNode.var;
                            HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Block object is in local object " + blockIndex);
                        }
                    }
                    if (m.instructions.get(index).getType() == 5) {
                        MethodInsnNode mdNode = (MethodInsnNode)m.instructions.get(index);
                        if (mdNode.owner.equals(names.get("worldJavaClassName")) && mdNode.name.equals(names.get("getBlockMetadataMethodName"))) {
                            int offset = 1;
                            while (m.instructions.get(index + offset).getOpcode() != Opcodes.ISTORE)
                                offset++;
                            HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Found metadata local variable ISTORE Node at " + (index + offset));
                            VarInsnNode mdFieldNode = (VarInsnNode)m.instructions.get(index + offset);
                            mdIndex = mdFieldNode.var;
                            HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Metadata is in local variable " + mdIndex);
                        }
                    }
                    if (m.instructions.get(index).getOpcode() == Opcodes.IFNULL) {
                        HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Found IFNULL Node at " + index);
                        int offset = 1;
                        while (m.instructions.get(index + offset).getOpcode() != Opcodes.ALOAD)
                            offset++;
                        HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Found ALOAD Node at offset " + offset + " from IFNULL Node");
                        HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Patching method " + names.get("javaClassName") + "/" + m.name + m.desc + "...");
                        LabelNode lmm1Node = new LabelNode(new Label());
                        InsnList toInject = new InsnList();
                        toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "reforged/mods/harvester/TreeHarvestEvent", "instance", "Lreforged/mods/harvester/TreeHarvestEvent;"));
                        toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        toInject.add(new FieldInsnNode(Opcodes.GETFIELD, names.get("javaClassName"), names.get("worldFieldName"), "L" + names.get("worldJavaClassName") + ";"));
                        toInject.add(new VarInsnNode(Opcodes.ILOAD, 1));
                        toInject.add(new VarInsnNode(Opcodes.ILOAD, 2));
                        toInject.add(new VarInsnNode(Opcodes.ILOAD, 3));
                        toInject.add(new VarInsnNode(Opcodes.ALOAD, blockIndex));
                        toInject.add(new VarInsnNode(Opcodes.ILOAD, mdIndex));
                        toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        toInject.add(new FieldInsnNode(Opcodes.GETFIELD, names.get("javaClassName"), names.get("entityPlayerFieldName"), "L" + names.get("entityPlayerMPJavaClassName") + ";"));
                        toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "reforged/mods/harvester/TreeHarvestEvent", "onBlockHarvested", "(L" + names.get("worldJavaClassName") + ";IIIL" + names.get("blockJavaClassName") + ";IL" + names.get("entityPlayerJavaClassName") + ";)V"));
                        toInject.add(lmm1Node);
                        m.instructions.insertBefore(m.instructions.get(index + offset), toInject);
                        HarvesterMod.LOGGER.info("[Tree Harvester - ASM] Method " + names.get("javaClassName") + "/" + m.name + m.desc + " patched at index " + (index + offset - 1));
                        HarvesterMod.LOGGER.info("[Tree Harvester - ASM] TreeCapitator ASM Patching Complete!");
                        break;
                    }
                }
            }
        }
        ClassWriter writer = new ClassWriter(1);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
