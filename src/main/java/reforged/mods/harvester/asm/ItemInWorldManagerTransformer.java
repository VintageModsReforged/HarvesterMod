package reforged.mods.harvester.asm;

import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import reforged.mods.harvester.HarvesterMod;

import java.util.HashMap;
import java.util.Iterator;

public class ItemInWorldManagerTransformer implements IClassTransformer {

    private final HashMap<String, String> obfStrings;

    public ItemInWorldManagerTransformer() {
        this.obfStrings = new HashMap<String, String>();
        this.obfStrings.put("className", "jd");
        this.obfStrings.put("javaClassName", "jd");
        this.obfStrings.put("targetMethodName", "d");
        this.obfStrings.put("worldFieldName", "a");
        this.obfStrings.put("entityPlayerFieldName", "b");
        this.obfStrings.put("worldJavaClassName", "aab");
        this.obfStrings.put("getBlockMetadataMethodName", "h");
        this.obfStrings.put("blockJavaClassName", "apa");
        this.obfStrings.put("blocksListFieldName", "r");
        this.obfStrings.put("entityPlayerJavaClassName", "sq");
        this.obfStrings.put("entityPlayerMPJavaClassName", "jc");
    }

    @Override
    public byte[] transform(String obfName, String transformedName, byte[] basicClass) {
        return "jd".equals(obfName) ? transformItemInWorldManager(basicClass, this.obfStrings) : basicClass;
    }

    private byte[] transformItemInWorldManager(byte[] bytes, HashMap<String, String> hm) {
        HarvesterMod.LOGGER.info("Harvester ASM Magic Time!");
        HarvesterMod.LOGGER.info("Class Transformation running on " + hm.get("javaClassName") + "...");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        Iterator<MethodNode> methods = classNode.methods.iterator();
        while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (m.name.equals(hm.get("targetMethodName")) && m.desc.equals("(III)Z")) {
                HarvesterMod.LOGGER.info("Found target method " + m.name + m.desc + "! Searching for landmarks...");
                int blockIndex = 4;
                int mdIndex = 5;
                for (int index = 0; index < m.instructions.size(); index++) {
                    if (m.instructions.get(index).getType() == 4) {
                        FieldInsnNode blocksListNode = (FieldInsnNode)m.instructions.get(index);
                        if (blocksListNode.owner.equals(hm.get("blockJavaClassName")) && blocksListNode.name.equals(hm.get("blocksListFieldName"))) {
                            int offset = 1;
                            while (m.instructions.get(index + offset).getOpcode() != 58)
                                offset++;
                            HarvesterMod.LOGGER.info("Found Block object ASTORE Node at " + (index + offset));
                            VarInsnNode blockNode = (VarInsnNode)m.instructions.get(index + offset);
                            blockIndex = blockNode.var;
                            HarvesterMod.LOGGER.info("Block object is in local object " + blockIndex);
                        }
                    }
                    if (m.instructions.get(index).getType() == 5) {
                        MethodInsnNode mdNode = (MethodInsnNode)m.instructions.get(index);
                        if (mdNode.owner.equals(hm.get("worldJavaClassName")) && mdNode.name.equals(hm.get("getBlockMetadataMethodName"))) {
                            int offset = 1;
                            while (m.instructions.get(index + offset).getOpcode() != 54)
                                offset++;
                            HarvesterMod.LOGGER.info("Found metadata local variable ISTORE Node at " + (index + offset));
                            VarInsnNode mdFieldNode = (VarInsnNode)m.instructions.get(index + offset);
                            mdIndex = mdFieldNode.var;
                            HarvesterMod.LOGGER.info("Metadata is in local variable " + mdIndex);
                        }
                    }
                    if (m.instructions.get(index).getOpcode() == 198) {
                        HarvesterMod.LOGGER.info("Found IFNULL Node at " + index);
                        int offset = 1;
                        while (m.instructions.get(index + offset).getOpcode() != 25)
                            offset++;
                        HarvesterMod.LOGGER.info("Found ALOAD Node at offset " + offset + " from IFNULL Node");
                        HarvesterMod.LOGGER.info("Patching method " + hm.get("javaClassName") + "/" + m.name + m.desc + "...");
                        LabelNode lmm1Node = new LabelNode(new Label());
                        InsnList toInject = new InsnList();
                        toInject.add(new FieldInsnNode(178, "reforged/mods/harvester/TreeHarvestEvent", "instance", "Lreforged/mods/harvester/TreeHarvestEvent;"));
                        toInject.add(new VarInsnNode(25, 0));
                        toInject.add(new FieldInsnNode(180, hm.get("javaClassName"), hm.get("worldFieldName"), "L" + hm.get("worldJavaClassName") + ";"));
                        toInject.add(new VarInsnNode(21, 1));
                        toInject.add(new VarInsnNode(21, 2));
                        toInject.add(new VarInsnNode(21, 3));
                        toInject.add(new VarInsnNode(25, blockIndex));
                        toInject.add(new VarInsnNode(21, mdIndex));
                        toInject.add(new VarInsnNode(25, 0));
                        toInject.add(new FieldInsnNode(180, hm.get("javaClassName"), hm.get("entityPlayerFieldName"), "L" + hm.get("entityPlayerMPJavaClassName") + ";"));
                        toInject.add(new MethodInsnNode(182, "reforged/mods/harvester/TreeHarvestEvent", "onBlockHarvested", "(L" + hm.get("worldJavaClassName") + ";IIIL" + hm.get("blockJavaClassName") + ";IL" + hm.get("entityPlayerJavaClassName") + ";)V"));
                        toInject.add(lmm1Node);
                        m.instructions.insertBefore(m.instructions.get(index + offset), toInject);
                        HarvesterMod.LOGGER.info("Method " + hm.get("javaClassName") + "/" + m.name + m.desc + " patched at index " + (index + offset - 1));
                        HarvesterMod.LOGGER.info("Harvester ASM Patching Complete!");
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
