package expression.generaltests;

import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.basic.ExpressionCollection;
import basic.MathToolTestBase;
import java.util.HashSet;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExpressionCollectionTests extends MathToolTestBase {

    Expression f, g, h;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void defineExpressions() {
        // f = 1*x*sin(x)
        f = new Constant(1).mult(Variable.create("x")).mult(Variable.create("x").sin());
        // g = x*1*sin(x)
        g = Variable.create("x").mult(1).mult(Variable.create("x").sin());
        // h = 1*1*1
        h = new Constant(1).mult(1).mult(1);
    }

    @Test
    public void getMaxIndexTest() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(f);
        terms.add(g);
        terms.add(h);
        terms.add(f);

        results = new Object[]{terms.getBound(), terms.getSize()};
        expectedResults = new Object[]{4, 4};

        assertTrue(terms.getBound() == 4);
        assertTrue(terms.getSize() == 4);
    }

    @Test
    public void removeTest1() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(f);
        terms.add(g);
        terms.add(h);
        terms.add(f);
        terms.remove(3);

        results = new Object[]{terms.getBound(), terms.getSize()};
        expectedResults = new Object[]{3, 3};

        assertTrue(terms.getBound() == 3);
        assertTrue(terms.getSize() == 3);
    }

    @Test
    public void removeTest2() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(f);
        terms.add(g);
        terms.add(h);
        terms.add(f);
        terms.remove(1);

        results = new Object[]{terms.getBound(), terms.getSize()};
        expectedResults = new Object[]{4, 3};

        assertTrue(terms.getBound() == 4);
        assertTrue(terms.getSize() == 3);
    }

    @Test
    public void addTest1() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(Variable.create("a"));
        terms.add(Variable.create("b"));
        terms.add(Variable.create("c"));
        terms.add(Variable.create("d"));
        terms.add(Variable.create("e"));
        terms.remove(4);
        terms.remove(3);
        terms.remove(2);
        terms.add(Variable.create("f"));
        assertTrue(terms.getBound() == 3);
        assertTrue(terms.get(0) != null);
        assertTrue(terms.get(1) != null);
        assertTrue(terms.get(2) != null);
        assertTrue(terms.get(3) == null);
        assertTrue(terms.get(4) == null);
        assertTrue(terms.get(5) == null);
    }

    @Test
    public void addTest2() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(Variable.create("a"));
        terms.add(Variable.create("b"));
        terms.add(Variable.create("c"));
        terms.add(Variable.create("d"));
        terms.add(Variable.create("e"));
        terms.remove(3);
        terms.add(Variable.create("f"));
        assertTrue(terms.getBound() == 6);
        assertTrue(terms.get(0) != null);
        assertTrue(terms.get(1) != null);
        assertTrue(terms.get(2) != null);
        assertTrue(terms.get(3) == null);
        assertTrue(terms.get(4) != null);
        assertTrue(terms.get(5) != null);
    }

    @Test
    public void insertTest1() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.put(0, Variable.create("a"));
        terms.put(1, Variable.create("b"));
        terms.put(4, Variable.create("c"));
        terms.put(5, Variable.create("d"));
        terms.put(8, Variable.create("e"));
        terms.insert(2, Variable.create("f"));
        assertTrue(terms.getBound() == 9);
        assertTrue(terms.get(0).equals(Variable.create("a")));
        assertTrue(terms.get(1).equals(Variable.create("b")));
        assertTrue(terms.get(2).equals(Variable.create("f")));
        assertTrue(terms.get(3) == null);
        assertTrue(terms.get(4).equals(Variable.create("c")));
        assertTrue(terms.get(5).equals(Variable.create("d")));
        assertTrue(terms.get(6) == null);
        assertTrue(terms.get(7) == null);
        assertTrue(terms.get(8).equals(Variable.create("e")));
    }

    @Test
    public void insertTest2() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.put(0, Variable.create("a"));
        terms.put(1, Variable.create("b"));
        terms.put(4, Variable.create("c"));
        terms.put(5, Variable.create("d"));
        terms.put(8, Variable.create("e"));
        terms.insert(1, Variable.create("f"));
        assertTrue(terms.getBound() == 10);
        assertTrue(terms.get(0).equals(Variable.create("a")));
        assertTrue(terms.get(1).equals(Variable.create("f")));
        assertTrue(terms.get(2).equals(Variable.create("b")));
        assertTrue(terms.get(3) == null);
        assertTrue(terms.get(4) == null);
        assertTrue(terms.get(5).equals(Variable.create("c")));
        assertTrue(terms.get(6).equals(Variable.create("d")));
        assertTrue(terms.get(7) == null);
        assertTrue(terms.get(8) == null);
        assertTrue(terms.get(9).equals(Variable.create("e")));
    }

    @Test
    public void insertTest3() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.insert(4, Variable.create("a"));
        assertTrue(terms.getBound() == 5);
        assertTrue(terms.get(0) == null);
        assertTrue(terms.get(1) == null);
        assertTrue(terms.get(2) == null);
        assertTrue(terms.get(3) == null);
        assertTrue(terms.get(4).equals(Variable.create("a")));
    }

    @Test
    public void insertTest4() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(Variable.create("a"));
        terms.add(Variable.create("b"));
        terms.insert(4, Variable.create("c"));
        assertTrue(terms.getBound() == 5);
        assertTrue(terms.get(0).equals(Variable.create("a")));
        assertTrue(terms.get(1).equals(Variable.create("b")));
        assertTrue(terms.get(2) == null);
        assertTrue(terms.get(3) == null);
        assertTrue(terms.get(4).equals(Variable.create("c")));
    }

    @Test
    public void removeMultipliTermsTest() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(Expression.ONE);
        terms.add(Expression.TWO);
        terms.add(Expression.TWO);
        terms.add(Expression.TWO);
        terms.add(Expression.THREE);
        terms.removeMultipleEquivalentTerms();
        assertTrue(terms.getBound() == 3);
        assertTrue(terms.get(0).equals(Expression.ONE));
        assertTrue(terms.get(1).equals(Expression.TWO));
        assertTrue(terms.get(2).equals(Expression.THREE));
    }

    @Test
    public void containsTest() {
        ExpressionCollection terms = new ExpressionCollection();
        try {
            terms.add(Expression.build("a+b"));
            terms.add(Expression.build("x*y"));
            terms.add(Expression.build("sin(z)"));
            terms.remove(1);
            Expression expr1 = Expression.build("a+b");
            Expression expr2 = Expression.build("x*y");
            Expression expr3 = Expression.build("sin(z)");
            assertTrue(terms.containsExpression(expr1));
            assertFalse(terms.containsExpression(expr2));
            assertTrue(terms.containsExpression(expr3));
            assertTrue(terms.containsExpression(null));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void containsEquivalentTest() {
        ExpressionCollection terms = new ExpressionCollection();
        try {
            terms.add(Expression.build("a+b+c"));
            terms.add(Expression.build("x*y*z"));
            terms.add(Expression.build("a*sin(u+v)"));
            Expression expr1 = Expression.build("c+a+b");
            Expression expr2 = Expression.build("z*y*x");
            Expression expr3 = Expression.build("sin(v+u)*a");
            assertTrue(terms.containsExquivalent(expr1));
            assertTrue(terms.containsExquivalent(expr2));
            assertTrue(terms.containsExquivalent(expr3));
            terms.remove(1);
            assertFalse(terms.containsExquivalent(expr2));
            assertTrue(terms.containsExquivalent(null));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void iteratorTest1() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.put(0, ONE);
        terms.put(2, TWO);
        terms.put(3, Variable.create("x"));
        terms.put(7, THREE);
        terms.put(18, ONE.div(TWO));

        int i = 0;
        for (Expression term : terms) {
            i++;
        }

        assertTrue(i == 5);
    }

    @Test
    public void iteratorTest2() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.put(7, ONE);
        terms.put(8, TWO);
        terms.put(15, Variable.create("x"));
        terms.put(27, THREE);

        int i = 0;
        for (Expression term : terms) {
            i++;
        }

        assertTrue(i == 4);
    }

    @Test
    public void iteratorEmptyTest() {
        ExpressionCollection terms = new ExpressionCollection();

        int i = 0;
        for (Expression term : terms) {
            i++;
        }

        assertTrue(i == 0);
    }

}
