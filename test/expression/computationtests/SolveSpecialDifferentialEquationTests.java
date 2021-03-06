package expression.computationtests;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.differentialequation.SolveGeneralDifferentialEquationUtils;
import abstractexpressions.expression.basic.ExpressionCollection;
import basic.MathToolTestBase;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SolveSpecialDifferentialEquationTests extends MathToolTestBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void defineExpressions() throws Exception {
    }

    @Test
    public void solveDiffEqWithOnlySecondDerivativeAndFunctionTest() {
        try {
            // DGL: y'' = y^2. Implizite Lösungen sind: int(1/((2*y^3)/3+C_1)^(1/2),y)+x+C_2 = 0, int(1/((2*y^3)/3+C_1)^(1/2),y)-(x+C_3) = 0.
            Expression leftSide = Expression.build("y''");
            Expression rightSide = Expression.build("y^2");
            ExpressionCollection solutions = SolveGeneralDifferentialEquationUtils.solveDifferentialEquation(leftSide, rightSide, "x", "y");
            Expression expectedSolutionOne = new Operator(TypeOperator.integral, new Object[]{ONE.div(TWO.mult(Variable.create("y").pow(3)).div(3).add(Variable.create("C_1")).pow(1, 2)), "y"}).add(Variable.create("x")).add(Variable.create("C_2"));
            Expression expectedSolutionTwo = new Operator(TypeOperator.integral, new Object[]{ONE.div(TWO.mult(Variable.create("y").pow(3)).div(3).add(Variable.create("C_1")).pow(1, 2)), "y"}).sub(Variable.create("x").add(Variable.create("C_3")));
            expectedResults = new Object[]{2, expectedSolutionOne, expectedSolutionTwo};
            results = new Object[]{solutions.getBound(), solutions.get(0), solutions.get(1)};
            assertTrue(solutions.getBound() == 2);
            assertTrue(solutions.containsExquivalent(expectedSolutionOne));
            assertTrue(solutions.containsExquivalent(expectedSolutionTwo));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveDiffEqWithOnlythirdAndFirstDerivativeTest() {
        try {
            // DGL: y''' = y'^2. Keine algebraischen Lösungen.
            Expression leftSide = Expression.build("y'''");
            Expression rightSide = Expression.build("y'^2");
            ExpressionCollection solutions = SolveGeneralDifferentialEquationUtils.solveDifferentialEquation(leftSide, rightSide, "x", "y");
            expectedResults = new Object[]{0};
            results = new Object[]{solutions.getBound()};
            assertTrue(solutions.isEmpty());
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveDiffEqWithOnlyVarOrdAndDerivativesTest() {
        try {
            /* 
            DGL: y'' + y'^2*y = 0. Imnplizite Lösungen: y_1 = C_1 und 
            y_2 ist gegeben durch int(exp(y^2/2-C_1),y)-(x+C_1) und  
            int(exp(y^2/2-C_1),y)+x-C_1. 
             */
            Expression leftSide = Expression.build("y'' + y'^2*y");
            ExpressionCollection solutions = SolveGeneralDifferentialEquationUtils.solveDifferentialEquation(leftSide, ZERO, "x", "y");
            Expression solutionOne = Variable.create("C_1");
            Expression implicitSolutionTwo = new Operator(TypeOperator.integral, new Object[]{Variable.create("y").pow(2).div(2).sub(Variable.create("C_1")).exp(), "y"}).sub(Variable.create("x").add(Variable.create("C_1")));
            Expression implicitSolutionThree = new Operator(TypeOperator.integral, new Object[]{Variable.create("y").pow(2).div(2).sub(Variable.create("C_1")).exp(), "y"}).add(Variable.create("x")).sub(Variable.create("C_1"));
            expectedResults = new Object[]{3, solutionOne, implicitSolutionTwo, implicitSolutionThree};
            results = new Object[]{solutions.getBound(), solutions.get(0), solutions.get(1), solutions.get(2)};
            assertTrue(solutions.getBound() == 3);
            assertTrue(solutionOne.equals(solutions.get(0)));
            assertTrue(implicitSolutionTwo.equals(solutions.get(1)));
            assertTrue(implicitSolutionThree.equals(solutions.get(2)));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveBernoulliDifferentialEquationTest() {
        try {
            // DGL: y' -4*x*y + 6*y^2 = 0. Lösungen sind exp(2*x^2)/(C_1+6*int(exp(2*x^2),x)).
            Expression leftSide = Expression.build("y' -4*x*y + 6*y^2");
            ExpressionCollection solutions = SolveGeneralDifferentialEquationUtils.solveDifferentialEquation(leftSide, ZERO, "x", "y");
            Expression expectedSolution = TWO.mult(Variable.create("x").pow(2)).exp().div(Variable.create("C_1").add(new Constant(6).mult(new Operator(TypeOperator.integral, new Object[]{TWO.mult(Variable.create("x").pow(2)).exp(), "x"}))));
            expectedResults = new Object[]{1, expectedSolution};
            results = new Object[]{solutions.getBound(), solutions.get(0)};
            assertTrue(solutions.getBound() == 1);
            assertTrue(solutions.containsExquivalent(expectedSolution));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

}
