package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public class LocalVariableMutator implements MethodMutatorFactory {

    private final class LocalVariableVisitor extends MethodVisitor {
        private final MutationContext context;

        public LocalVariableVisitor(final MutationContext context, final MethodVisitor delegateVisitor) { super(ASMVersion.ASM_VERSION, delegateVisitor);
            this.context = context;
        }

        @Override
        public void visitVarInsn(final int opcode, int var) {
            if (this.shouldMutate(opcode, var)) {
                switch (opcode) {
                    case Opcodes.ISTORE:
                        super.visitInsn(Opcodes.POP);
                        super.visitInsn(Opcodes.ICONST_0);
                        break;
                    case Opcodes.FSTORE:
                        super.visitInsn(Opcodes.POP);
                        super.visitInsn(Opcodes.FCONST_0);
                        break;
                    case Opcodes.DSTORE:
                        super.visitInsn(Opcodes.POP2);
                        super.visitInsn(Opcodes.DCONST_0);
                        break;
                    case Opcodes.LSTORE:
                        super.visitInsn(Opcodes.POP2);
                        super.visitInsn(Opcodes.LCONST_0);
                        break;
                    case Opcodes.ASTORE:
                        super.visitInsn(Opcodes.POP);
                        super.visitInsn(Opcodes.ACONST_NULL);
                        break;
                }
            }
            super.visitVarInsn(opcode, var);
        }

        @Override
        public void visitMethodInsn(final int opcode, final String owner,
                final String name, final String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        private boolean shouldMutate(final int opcode, final int var) {
            if (opcode == Opcodes.ISTORE || opcode == Opcodes.FSTORE ||
                opcode == Opcodes.DSTORE || opcode == Opcodes.LSTORE ||
                opcode == Opcodes.ASTORE) {
                final MutationIdentifier mutationId = this.context.registerMutation(
                        LocalVariableMutator.this, "Removed assignment to local variable " + var);
                return this.context.shouldMutate(mutationId);
            }

            return false;
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
