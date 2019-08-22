package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

import java.util.HashSet;
import java.util.Set;

public class LocalVariableMutator implements MethodMutatorFactory {

    private final class LocalVariableVisitor extends MethodVisitor {
        private final MutationContext context;
        private Set<Integer> locals;

        LocalVariableVisitor(final MutationContext context, final MethodVisitor delegateVisitor) {
            super(ASMVersion.ASM_VERSION, delegateVisitor);
            this.context = context;
            this.locals = new HashSet<>();
        }

        @Override
        public void visitVarInsn(final int opcode, int var) {
            if (this.isStore(opcode)) {
                boolean isDeclaration = this.locals.add(var);
                if (this.shouldMutate(var, opcode)) {
                    if (isDeclaration) {
                        this.mutateDeclaration(opcode, var);
                    } else {
                        this.mutateAssignment(opcode);
                    }
                    return;
                }
            }

            super.visitVarInsn(opcode, var);
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature,
                                       Label start, Label end, int index) {
            this.locals.remove(index);
            super.visitLocalVariable(name, desc, signature, start, end, index);
        }

        @Override
        public void visitIincInsn(int var, int increment) {
            if (this.shouldMutate(var, Opcodes.IINC)) {
                // no op, just don't do the increment
            } else {
                super.visitIincInsn(var, increment);
            }
        }

        // For initialization statements, change the assigned value to
        // to 0, which is effectively the same as removing the assignment.
        private void mutateDeclaration(final int opcode, int var) {
            switch (opcode) {
                case Opcodes.ISTORE:
                    super.visitInsn(Opcodes.POP);
                    super.visitInsn(Opcodes.ICONST_0);
                    break;
                case Opcodes.FSTORE:
                    super.visitInsn(Opcodes.POP);
                    super.visitInsn(Opcodes.FCONST_0);
                    break;
                case Opcodes.ASTORE:
                    super.visitInsn(Opcodes.POP);
                    super.visitInsn(Opcodes.ACONST_NULL);
                    break;
                case Opcodes.DSTORE:
                    super.visitInsn(Opcodes.POP2);
                    super.visitInsn(Opcodes.DCONST_0);
                    break;
                case Opcodes.LSTORE:
                    super.visitInsn(Opcodes.POP2);
                    super.visitInsn(Opcodes.LCONST_0);
                    break;
            }

            // The value (or values) atop the stack have been replaced with 0.
            // Store them as usual.
            super.visitVarInsn(opcode, var);
        }

        // For existing local variables, simply remove the values from the stack
        // and the assignment statement.
        private void mutateAssignment(final int opcode) {
            switch (opcode) {
                case Opcodes.ISTORE:
                case Opcodes.FSTORE:
                case Opcodes.ASTORE:
                    super.visitInsn(Opcodes.POP);
                    break;
                default:
                    super.visitInsn(Opcodes.POP2);
            }
        }

        private boolean isStore(final int opcode) {
            switch (opcode) {
                case Opcodes.ISTORE:
                case Opcodes.FSTORE:
                case Opcodes.DSTORE:
                case Opcodes.LSTORE:
                case Opcodes.ASTORE:
                    return true;
                default:
                    return false;
            }
        }

        private String getInsnType(final int opcode) {
            switch (opcode) {
                case Opcodes.ISTORE:
                    return "Integer assignment";
                case Opcodes.DSTORE:
                    return "Double assignment";
                case Opcodes.FSTORE:
                    return "Float assignment";
                case Opcodes.LSTORE:
                    return "Long assignment";
                case Opcodes.ASTORE:
                    return "Reference assignment";
                case Opcodes.IINC:
                    return "Increment";
                default:
                    throw new IllegalArgumentException(opcode + " is not a valid mutatable opcode");
            }
        }

        private boolean shouldMutate(final int var, final int opcode) {
            String insnType = this.getInsnType(opcode);
            final MutationIdentifier mutationId = this.context.registerMutation(
                    LocalVariableMutator.this, "Removed " + insnType + " on local variable " + var);
            return this.context.shouldMutate(mutationId);
        }
    }

    @Override
    public MethodVisitor create(MutationContext context, MethodInfo methodInfo, MethodVisitor methodVisitor) {
        return new LocalVariableVisitor(context, methodVisitor);
    }

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    @Override
    public String toString() {
        return "EXPERIMENTAL_LOCAL_VARIABLE_MUTATOR";
    }

    @Override
    public String getName() {
        return toString();
    }

}
