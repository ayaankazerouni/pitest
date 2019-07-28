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
    
    private static class HasLocalVariable implements Callable<Object> {
        @Override
        public Integer call() throws Exception {
            int a = 1;
            return a;
        }
    }
    
    @Test
    public void shouldReplaceIntAssignment1With0() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalVariable.class);
        assertMutantCallableReturns(new HasLocalVariable(), mutant, 0);
    }
    
    private static class HasLocalVariableWithTwoAssignments implements Callable<Object> {
        @Override
        public Integer call() throws Exception {
            int a = 1;
            a = 10;
            return a;
        }
    }
    
    @Test
    public void shouldReplaceTwoIntAssignmentsWith0() throws Exception {
        final Mutant mutant = getFirstMutant(HasLocalVariableWithTwoAssignments.class);
        assertMutantCallableReturns(new HasLocalVariableWithTwoAssignments(), mutant, 0);
    }
}
