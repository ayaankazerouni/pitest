package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class LocalVariableMutator implements MethodMutatorFactory {

    private final class LocalVariableVisitor extends MethodVisitor {
        private static final String INITIALIZATION = "initialization";
        private static final String ASSIGNMENT = "assignment";
        private static final String INCREMENT = "increment";

        private final MutationContext context;
        private final Stack<Set<Integer>> localsInScope;

        LocalVariableVisitor(final MutationContext context, final MethodInfo methodInfo,
                             final MethodVisitor delegateVisitor) {
            super(ASMVersion.ASM_VERSION, delegateVisitor);
            this.context = context;
            this.localsInScope = new Stack<>();
            this.localsInScope.push(new HashSet<>());
        }

        @Override
        public void visitCode() {
            System.out.println("\nStart");
        }

        @Override
        public void visitVarInsn(final int opcode, int var) {
            if (this.getStoreType(opcode).length() != 0) {
               boolean isInitialization = this.localsInScope.isEmpty() || this.localsInScope.peek().add(var);
               System.out.println("Store " + isInitialization + " " + this.localsInScope);
                if (this.shouldMutate(var, opcode, ASSIGNMENT)) {
                    if (isInitialization) {
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
        public void visitIincInsn(int var, int increment) {
            System.out.println("Reached increment");
            if (this.shouldMutate(var, Opcodes.IINC, INCREMENT)) {
                // no op, just don't do the increment
                System.out.println("\tMutating increment");
            } else {
                super.visitIincInsn(var, increment);
            }
        }

        @Override
        public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
            super.visitFrame(type, nLocal, local, nStack, stack);

            if (!this.localsInScope.isEmpty()) {
                this.localsInScope.pop();

                for (int i = 0; i < local.length; i++) {
                    if (this.localsInScope.isEmpty()) {
                        this.localsInScope.push(new HashSet<>());
                    }
                }
            }

            System.out.println("Visit frame: " + this.localsInScope);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            super.visitJumpInsn(opcode, label);
            if (opcode != Opcodes.GOTO) {
                Set<Integer> newScope = new HashSet<>();
                if (!this.localsInScope.isEmpty()) {
                    newScope.addAll(this.localsInScope.peek());
                }
                this.localsInScope.push(newScope);
            }

            System.out.println("Visit jump: " + this.localsInScope + " to " + label);
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
        // and don't do the assignment instruction.
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

        private String getStoreType(final int opcode) {
            switch (opcode) {
                case Opcodes.ISTORE:
                    return "Integer";
                case Opcodes.FSTORE:
                    return "Float";
                case Opcodes.DSTORE:
                    return "Double";
                case Opcodes.LSTORE:
                    return "Long";
                case Opcodes.ASTORE:
                    return "Reference";
                default:
                    return "";
            }
        }

        private boolean shouldMutate(final int var, final int opcode, String type) {
            String insnType = this.getStoreType(opcode);
            String description = "Removed " + type + " on local " + insnType + " variable " + var;
            final MutationIdentifier mutationId = this.context.registerMutation(
                    LocalVariableMutator.this, description);
            return this.context.shouldMutate(mutationId);
        }
    }

    @Override
    public MethodVisitor create(MutationContext context, MethodInfo methodInfo, MethodVisitor methodVisitor) {
        return new LocalVariableVisitor(context, methodInfo, methodVisitor);
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
