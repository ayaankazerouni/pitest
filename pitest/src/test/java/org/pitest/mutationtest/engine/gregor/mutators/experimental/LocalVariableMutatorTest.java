package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;

public class LocalVariableMutatorTest extends MutatorTestBase {
    @Before
    public void setupEngineToMutateOnlyLocalVariables() {
        createTesteeWith(new LocalVariableMutator());
    }

    private static class HasNoLocals implements Callable<Object> {
        @Override
        public Integer call() throws Exception {
            return 10;
        }
    }

    @Test
    public void shouldHaveNoMutants() throws Exception {
        assertNoMutants(HasNoLocals.class);
    }

    private static class HasLocalInteger implements Callable<Object> {
        @Override
        public Integer call() throws Exception {
            int a = 1;
            return a;
        }
    }

    @Test
    public void shouldReplaceIntInitialization1With0() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalInteger.class);
        assertMutantCallableReturns(new HasLocalInteger(), mutant, 0);
    }

    private static class HasLocalBoolean implements Callable<Object> {
        @Override
        public Boolean call() throws Exception {
            boolean a = true;
            return a;
        }
    }

    @Test
    public void shouldReplaceBooleanInitializationTrueWithFalse() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalBoolean.class);
        assertMutantCallableReturns(new HasLocalBoolean(), mutant, false);
    }

    private static class HasLocalChar implements Callable<Object> {
        @Override
        public Character call() throws Exception {
            char a = 'a';
            return a;
        }
    }

    @Test
    public void shouldReplaceCharacterInitializationWith0() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalChar.class);
        assertMutantCallableReturns(new HasLocalChar(), mutant, '\u0000');
    }


    private static class HasLocalByte implements Callable<Object> {
        @Override
        public Byte call() throws Exception {
            byte a = 1;
            return a;
        }
    }

    @Test
    public void shouldReplaceByteInitializationWith0() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalByte.class);
        assertMutantCallableReturns(new HasLocalByte(), mutant, (byte) 0);
    }

    private static class HasLocalVariableWithSecondAssignment implements Callable<Object> {
        @Override
        public Integer call() throws Exception {
            int a = 1;
            a = 10;
            return a;
        }
    }

    @Test
    public void shouldRemoveAssignmentToLocalVariable() throws Exception {
        final Mutant mutant = getNthMutant(HasLocalVariableWithSecondAssignment.class, 1);
        assertMutantCallableReturns(new HasLocalVariableWithSecondAssignment(), mutant, 1);
    }

    private static class HasLocalDouble implements Callable<Object> {
        @Override
        public Double call() throws Exception {
            double a = 1D;
            return a;
        }
    }

    @Test
    public void shouldReplaceDoubleInitializationWith0() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalDouble.class);
        assertMutantCallableReturns(new HasLocalDouble(), mutant, 0D);
    }

    private static class HasLocalLong implements Callable<Object> {
        @Override
        public Long call() throws Exception {
            long a = 1L;
            return a;
        }
    }

    @Test
    public void shouldReplaceLongInitializationWith0() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalLong.class);
        assertMutantCallableReturns(new HasLocalLong(), mutant, 0L);
    }

    private static class HasLocalString implements Callable<Object> {
        @Override
        public String call() throws Exception {
            String a = "a";
            return a;
        }
    }

    @Test
    public void shouldReplaceStringInitializationWithNull() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalString.class);
        assertMutantCallableReturns(new HasLocalString(), mutant, null);
    }

    private static class HasManyAssignments implements Callable<Object> {
        @Override
        public Integer call() {
            int a = 11;
            a++;
            return a;
        }
    }

    @Test
    public void shouldReplaceInitializationWith0() throws Exception {
        final Mutant mutant = getFirstMutant(HasManyAssignments.class);
        assertMutantCallableReturns(new HasManyAssignments(), mutant, 1);
    }

    @Test
    public void shouldRemoveIncrement() throws Exception {
        final Mutant mutant = getNthMutant(HasManyAssignments.class, 1);
        assertMutantCallableReturns(new HasManyAssignments(), mutant, 11);
    }

    private static class HasIfElse implements Callable<Object> {
        private boolean iff;

        public HasIfElse(boolean iff) {
            this.iff = iff;
        }

        @Override
        public Integer call() throws Exception {
            int a = 1; // var 1
            if (this.iff) {
                int i = 1; // var 2
                i = 2; // var 2
                return i;
            } else {
                a = 5; // var 1
                int j = 2; // var 2
            }

            a = a + 2; // var 1
            int k = 10; // var 2
            return k;
        }
    }

    @Test
    public void shouldRemoveInitializationInReusedSlotInElse() throws Exception {
        final Mutant mutant = getNthMutant(HasIfElse.class, 4);
        assertMutantCallableReturns(new HasIfElse(false), mutant, 10);
    }

    @Test
    public void shouldRemoveInitializationInReusedSlotAfterElse() throws Exception {
        final Mutant mutant = getNthMutant(HasIfElse.class, 6);
        assertMutantCallableReturns(new HasIfElse(false), mutant, 0);
    }

    private static class HasDeclarationInIfWithLoop implements Callable<Object> {
        private boolean iff;

        public HasDeclarationInIfWithLoop(boolean iff) {
            this.iff = iff;
        }

        public Integer call() throws Exception {
            if (this.iff) {
                int j = 1;
                for (int i = 5; i < 10; i++) {
                    j = j + i;
                }

                int k = 2;
                return j;
            }

            return 0;
        }
    }

    @Test
    public void shouldSetLoopInitializerTo0() throws Exception {
        final Mutant mutant = getNthMutant(HasDeclarationInIfWithLoop.class, 1);
        assertMutantCallableReturns(new HasDeclarationInIfWithLoop(true), mutant, 46);
    }

    @Test
    public void shouldRemoveAssignmentInLoopBody() throws Exception {
        // In bytecode instructions, the increment comes *after* an iteration of the loop, making
        // the assignment in the loop's body the 2nd mutation opportunity, not the 3rd
        final Mutant mutant = getNthMutant(HasDeclarationInIfWithLoop.class, 2);
        assertMutantCallableReturns(new HasDeclarationInIfWithLoop(true), mutant, 0);
    }
}
