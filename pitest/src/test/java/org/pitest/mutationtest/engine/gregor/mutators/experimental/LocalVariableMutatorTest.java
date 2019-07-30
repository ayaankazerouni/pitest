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
    public void shouldReplaceIntAssignment1With0() throws Exception {
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
    public void shouldReplaceBooleanAssignmentTrueWithFalse() throws Exception {
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
    public void shouldReplaceCharacterAssignmentWith0() throws Exception {
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
    public void shouldReplaceByteAssignmentWith0() throws Exception {
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
    public void shouldReplaceSecondIntAssignmentsWith0() throws Exception {
        final Mutant mutant = getNthMutant(HasLocalVariableWithSecondAssignment.class, 1);
        assertMutantCallableReturns(new HasLocalVariableWithSecondAssignment(), mutant, 1);
    }

    private static class HasLocalAssignmentToMethodReturnValue implements Callable<Object> {

        @Override
        public Integer call() throws Exception {
            int a = this.get10();
            return a;
        }

        private int get10() {
            return 10;
        }
    }

    @Test
    public void shouldReplaceAssignmentToMethodResultWith0() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalAssignmentToMethodReturnValue.class);
        assertMutantCallableReturns(new HasLocalAssignmentToMethodReturnValue(), mutant, 0);
    }

    private static class HasLocalDouble implements Callable<Object> {
        @Override
        public Double call() throws Exception {
            double a = 1D;
            return a;
        }
    }

    @Test
    public void shouldReplaceDoubleAssignmentWith0() throws Exception {
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
    public void shouldReplaceLongAssignmentWith0() throws Exception {
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
    public void shouldReplaceStringAssignmentWithNull() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalString.class);
        assertMutantCallableReturns(new HasLocalString(), mutant, null);
    }
}
