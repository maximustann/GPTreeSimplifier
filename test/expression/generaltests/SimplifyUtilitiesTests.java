package expression.generaltests;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyUtilities;
import basic.MathToolTestBase;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimplifyUtilitiesTests extends MathToolTestBase {

    Expression f, g, h;
    Expression exprWithMultipleVariables;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void defineExpressions() throws Exception {
        // f = 1*x*sin(x)
        f = new Constant(1).mult(Variable.create("x")).mult(Variable.create("x").sin());
        // g = x*1*sin(x)
        g = Variable.create("x").mult(ONE).mult(Variable.create("x").sin());
        // h = 1*1*1
        h = ONE.mult(ONE).mult(ONE);
        Variable x = Variable.create("x");
        Variable y = Variable.create("y");
        // expr = (1*x*sin(x)*1*y^2)/(5*y^3)
        exprWithMultipleVariables = ONE.mult(x).mult(x.sin()).mult(1).mult(y.pow(2)).div(new Constant(5).mult(y.pow(3)));
    }

    @Test
    public void getFactorsOfFTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactors(f);

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{2};

        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfGTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactors(g);

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{2};

        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfGIfApproximatingTest() {
        g = g.turnToApproximate();
        ExpressionCollection factors = SimplifyUtilities.getFactors(g);

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{2};

        assertTrue(factors.getBound() == 2);
        // Notwendig f??r weitere Tests!
        g = g.turnToPrecise();
    }

    @Test
    public void getFactorsOfHTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactors(h);

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{1};

        assertTrue(factors.getBound() == 1);
    }

    @Test
    public void getFactorsOfEnumeratorOfFTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{2};

        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfDenominatorOfFTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{0};

        assertTrue(factors.isEmpty());
    }

    @Test
    public void getFactorsOfEnumeratorOfGTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfNumeratorInExpression(g);

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{2};

        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfDenominatorOfGTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfDenominatorInExpression(g);

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{0};

        assertTrue(factors.isEmpty());
    }

    @Test
    public void getNonConstantFactorsOfEnumeratorOfExprTest() {
        ExpressionCollection factors = SimplifyUtilities.getNonConstantFactorsOfNumeratorInExpression(exprWithMultipleVariables, "x");

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{2};

        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getNonConstantFactorsOfDenominatorOfExprWithRespectToXTest() {
        ExpressionCollection factors = SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(exprWithMultipleVariables, "x");

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{0};

        assertTrue(factors.isEmpty());
    }

    @Test
    public void getNonConstantFactorsOfDenominatorOfExprWithRespectToYTest() {
        ExpressionCollection factors = SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(exprWithMultipleVariables, "y");

        results = new Object[]{factors.getBound()};
        expectedResults = new Object[]{1};

        assertTrue(factors.getBound() == 1);
    }

}
