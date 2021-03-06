package expression.computationtests;

import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.integration.RischAlgorithmUtils;
import basic.MathToolTestBase;
import org.junit.Assert;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RischAlgorithmUtilitiesTests extends MathToolTestBase {

    Expression f;

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
    public void isRationalOverFieldExtensionTest1() {
        // f = exp(x+2), var = "x", fieldGenerators = {}.
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection();
            f = Expression.build("exp(x+2)");
            boolean result = RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertFalse(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isRationalOverFieldExtensionTest2() {
        // f = exp(x)+3*ln(x)-7/4, var = "x", fieldGenerators = {exp(x), ln(x)}.
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection("exp(x)", "ln(x)");
            f = Expression.build("exp(x)+3*ln(x)-7/4");
            boolean result = RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isRationalOverFieldExtensionTest3() {
        // f = exp(x)+3*ln(x)-7/4, var = "x", fieldGenerators = {exp(x), ln(x)}.
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection("exp(x)", "ln(x)");
            f = Expression.build("exp(x)+3*ln(7*x)-7/4");
            boolean result = RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isRationalOverFieldExtensionTest4() {
        /* 
         F??r f = exp(5+3*x/8+a^2)-7/11, var = "x", fieldGenerators = {exp(x/8)} wird 
         true zur??ckgegeben, f??r f = exp(5+3^(1/5)*x+a^2)-7/11 dagegen false.
         */
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection("exp(x/8)", "ln(x)");
            f = Expression.build("exp(5+3*x/8+a^2)-7/11");
            boolean result = RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
            f = Expression.build("exp(5+3^(1/5)*x+a^2)-7/11");
            result = RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertFalse(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isRationalOverFieldExtensionTest5() {
        /* 
         F??r f = x^2/(1+x)+ln((3+2*x)^7)-x, var = "x", fieldGenerators = {exp(x), ln(2*x+3)} wird 
         true zur??ckgegeben, f??r f = x^3+7*ln(x)-5 dagegen false.
         */
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection("exp(x)", "ln(2*x+3)");
            f = Expression.build("x^2/(1+x)+ln((3+2*x)^7)-x");
            boolean result = RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
            f = Expression.build("x^3+7*ln(x)-5");
            result = RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertFalse(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isRationalOverFieldExtensionTest6() {
        /* 
         F??r f = x^2/(1+x)+x*ln(21+9*x)-x^5, var = "x", fieldGenerators = {exp(x), ln(14/5+(6*x)/5)} wird 
         true zur??ckgegeben.
         */
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection("exp(x)", "ln(14/5+(6*x)/5)");
            f = Expression.build("x^2/(1+x)+x*ln(21/8+9*x/8)-x^5");
            boolean result = RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void getFieldGeneratorsTest1() {
        /* 
         F??r f = x^7*exp(x+sinh(x/11))+ln(3+x)+exp(5*x+exp(x)-1), var = "x". Dann wird 
         fieldGenerators = {exp(x/11), exp(x), ln(3+x), exp(5*x+exp(x)-1)} zur??ckgegeben.
         */
        try {
            f = Expression.build("x^7*exp(x+sinh(x/11))+ln(3+x)+exp(5*x+exp(x)-1)");
            ExpressionCollection fieldGenerators = RischAlgorithmUtils.getOrderedTranscendentalGeneratorsForDifferentialField(f, "x");
            ExpressionCollection fieldGeneratorsForCompare = new ExpressionCollection("exp(x/11)",
                    "exp((x+exp(x/11)/2)-exp((-x)/11)/2)", "ln(3+x)", "exp((5*x+exp(x))-1)");
            Assert.assertTrue(fieldGenerators.equivalentInTerms(fieldGeneratorsForCompare));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getFieldGeneratorsTest2() {
        /* 
         F??r f = (x^7+ln(x)^3)*exp(x+exp(x+exp(7*x))), var = "x". Dann wird 
         fieldGenerators = {ln(x), exp(7*x), exp(x+exp(7*x)), exp(x+exp(x+exp(7*x)))} zur??ckgegeben.
         */
        try {
            f = Expression.build("(x^7+ln(x)^3)*exp(x+exp(x+exp(7*x)))");
            ExpressionCollection fieldGenerators = RischAlgorithmUtils.getOrderedTranscendentalGeneratorsForDifferentialField(f, "x");
            ExpressionCollection fieldGeneratorsForCompare = new ExpressionCollection("ln(x)", "exp(7*x)", "exp(x+exp(7*x))", "exp(x+exp(x+exp(7*x)))");
            Assert.assertTrue(fieldGenerators.equivalentInTerms(fieldGeneratorsForCompare));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getFieldGeneratorsTest3() {
        /* 
         F??r f = x*exp(8*x/15)-exp(6*x/35), var = "x". Dann wird 
         fieldGenerators = {exp(2*x/105)} zur??ckgegeben.
         */
        try {
            f = Expression.build("x*exp(8*x/15)-exp(6*x/35)");
            ExpressionCollection fieldGenerators = RischAlgorithmUtils.getOrderedTranscendentalGeneratorsForDifferentialField(f, "x");
            ExpressionCollection fieldGeneratorsForCompare = new ExpressionCollection("exp((2*x)/105)");
            Assert.assertTrue(fieldGenerators.equivalentInTerms(fieldGeneratorsForCompare));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getFieldGeneratorsTest4() {
        /* 
         F??r f = exp(8*x/15)-exp(10*x/9)+exp(4*x)/x, var = "x". Dann wird 
         fieldGenerators = {exp(2*x/45)} zur??ckgegeben.
         */
        try {
            f = Expression.build("exp(8*x/15)-exp(10*x/9)+exp(4*x)/x");
            ExpressionCollection fieldGenerators = RischAlgorithmUtils.getOrderedTranscendentalGeneratorsForDifferentialField(f, "x");
            ExpressionCollection fieldGeneratorsForCompare = new ExpressionCollection("exp((2*x)/45)");
            Assert.assertTrue(fieldGenerators.equivalentInTerms(fieldGeneratorsForCompare));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void getFieldGeneratorsTest5() {
        /* 
         F??r f = exp(x+exp(x))+exp(x), var = "x". Dann wird 
         fieldGenerators = {exp(x), exp(x+exp(x))} zur??ckgegeben.
         */
        try {
            f = Expression.build("exp(x+exp(x/2))+exp(x/3)");
            ExpressionCollection fieldGenerators = RischAlgorithmUtils.getOrderedTranscendentalGeneratorsForDifferentialField(f, "x");
            ExpressionCollection fieldGeneratorsForCompare = new ExpressionCollection("exp(x/6)", "exp(x+exp(x/2))");
            Assert.assertTrue(fieldGenerators.equivalentInTerms(fieldGeneratorsForCompare));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void functionIsGeneratedByObtainedFieldGeneratorsTest1() {
        /* 
         F??r f = x^2*exp(x+exp(x)*ln(x))+x^3/(8+x), var = "x". Dann wird 
         fieldGenerators = {exp(x), ln(x), exp(x+exp(x)*ln(x))} zur??ckgegeben.
         Ferner: f ist algebraisch ??ber R(x, fieldGenerators).
         */
        try {
            f = Expression.build("x^2*exp(x+exp(x)*ln(x))+x^3/(8+x)");
            ExpressionCollection fieldGenerators = RischAlgorithmUtils.getOrderedTranscendentalGeneratorsForDifferentialField(f, "x");
            ExpressionCollection fieldGeneratorsForCompare = new ExpressionCollection("exp(x)", "ln(x)", "exp(x+exp(x)*ln(x))");
            Assert.assertTrue(fieldGenerators.equivalentInTerms(fieldGeneratorsForCompare));
            Assert.assertTrue(RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void functionIsGeneratedByObtainedFieldGeneratorsTest2() {
        /* 
         F??r f = x^2*exp(x)+5^x, var = "x". Dann wird 
         fieldGenerators = {exp(x), exp(ln(5)*x)} zur??ckgegeben.
         Ferner: f ist algebraisch ??ber R(x, fieldGenerators).
         */
        try {
            f = Expression.build("x^2*exp(x)+5^x");
            ExpressionCollection fieldGenerators = RischAlgorithmUtils.getOrderedTranscendentalGeneratorsForDifferentialField(f, "x");
            ExpressionCollection fieldGeneratorsForCompare = new ExpressionCollection("exp(x)", "exp(ln(5)*x)");
            Assert.assertTrue(fieldGenerators.equivalentInTerms(fieldGeneratorsForCompare));
            Assert.assertTrue(RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void functionIsNotGeneratedByObtainedFieldGeneratorsTest() {
        /* 
         F??r f = x^2*exp(x+exp(1/x)*ln(x))+x^3/8+arctan(x), var = "x". Dann wird 
         fieldGenerators = {exp(x), ln(x), exp(x+exp(x)*ln(x))} zur??ckgegeben.
         Aber: f ist nicht algebraisch ??ber R(x, fieldGenerators).
         */
        try {
            f = Expression.build("x^2*exp(x+exp(1/x)*ln(x))+x^3/8+arctan(x)");
            ExpressionCollection fieldGenerators = RischAlgorithmUtils.getOrderedTranscendentalGeneratorsForDifferentialField(f, "x");
            ExpressionCollection fieldGeneratorsForCompare = new ExpressionCollection("exp(1/x)", "ln(x)", "exp(x+exp(1/x)*ln(x))");
            Assert.assertTrue(fieldGenerators.equivalentInTerms(fieldGeneratorsForCompare));
            Assert.assertFalse(RischAlgorithmUtils.isFunctionRationalOverDifferentialField(f, "x", fieldGenerators));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

}
