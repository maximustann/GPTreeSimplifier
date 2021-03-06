package abstractexpressions.expression.classes;

import enums.TypeSimplify;
import enums.TypeExpansion;
import enums.TypeLanguage;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyUtilities;
import abstractexpressions.interfaces.IdentifierValidator;
import abstractexpressions.interfaces.IdentifierValidatorImpl;
import enums.TypeFractionSimplification;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import lang.translator.Translator;
import process.Canceller;

public abstract class Expression implements AbstractExpression {

    private static final String EB_Expression_EXPRESSION_EMPTY_OR_INCOMPLETE = "EB_Expression_EXPRESSION_EMPTY_OR_INCOMPLETE";
    private static final String EB_Expression_IS_NOT_VALID_COMMAND = "EB_Expression_IS_NOT_VALID_COMMAND";
    private static final String EB_Expression_MISSING_CLOSING_BRACKET = "EB_Expression_MISSING_CLOSING_BRACKET";
    private static final String EB_Expression_EMPTY_PARAMETER = "EB_Expression_EMPTY_PARAMETER";
    private static final String EB_Expression_WRONG_BRACKETS = "EB_Expression_WRONG_BRACKETS";
    private static final String EB_Expression_TWO_OPERATIONS = "EB_Expression_TWO_OPERATIONS";
    private static final String EB_Expression_WRONG_ABS_BRACKETS = "EB_Expression_WRONG_ABS_BRACKETS";
    private static final String EB_Expression_LEFT_SIDE_OF_BINARY_IS_EMPTY = "EB_Expression_LEFT_SIDE_OF_BINARY_IS_EMPTY";
    private static final String EB_Expression_RIGHT_SIDE_OF_BINARY_IS_EMPTY = "EB_Expression_RIGHT_SIDE_OF_BINARY_IS_EMPTY";
    private static final String EB_Expression_WRONG_NUMBER_OF_PARAMETERS_IN_SELF_DEFINED_FUNCTION = "EB_Expression_WRONG_NUMBER_OF_PARAMETERS_IN_SELF_DEFINED_FUNCTION";
    private static final String EB_Expression_FORMULA_CANNOT_BE_INTERPRETED = "EB_Expression_FORMULA_CANNOT_BE_INTERPRETED";
    private static final String EB_Expression_STACK_OVERFLOW = "EB_Expression_STACK_OVERFLOW";

    // Sprache f??r Fehlermeldungen.
    private static TypeLanguage language;

    public final static Variable PI = Variable.create("pi");
    public final static Constant ZERO = new Constant(0);
    public final static Constant ONE = new Constant(1);
    public final static Constant TWO = new Constant(2);
    public final static Constant THREE = new Constant(3);
    public final static Constant FOUR = new Constant(4);
    public final static Constant TEN = new Constant(10);
    public final static Constant MINUS_ONE = new Constant(-1);

    public final static IdentifierValidator VALIDATOR = new IdentifierValidatorImpl();
    
    public static TypeLanguage getLanguage() {
        return language;
    }

    public static void setLanguage(TypeLanguage typeLanguage) {
        language = typeLanguage;
    }

    /**
     * Der Befehl f??r die jeweilige math. Operation und die Parameter in der
     * Befehlsklammer werden ausgelesen und zur??ckgegeben.<br>
     * BEISPIEL: commandLine = f(x, y, z). Zur??ckgegeben wird ein array der
     * L??nge zwei: im 0. Eintrag steht der String "f", im 1. der String "x, y,
     * z".
     *
     * @throws ExpressionException
     */
    public static String[] getOperatorAndArguments(String input) throws ExpressionException {

        // Leerzeichen beseitigen
        input = input.replaceAll(" ", "");

        String[] result = new String[2];
        int i = input.indexOf("(");
        if (i == -1) {
            // Um zu verhindern, dass es eine IndexOutOfBoundsException gibt.
            i = 0;
        }
        result[0] = input.substring(0, i);

        //Wenn der Befehl leer ist -> Fehler.
        if (result[0].length() == 0) {
            throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_EXPRESSION_EMPTY_OR_INCOMPLETE));
        }

        //Wenn length(result[0]) > l - 2 -> Fehler (der Befehl besitzt NICHT die Form command(...)).
        if (result[0].length() > input.length() - 2) {
            throw new ExpressionException(input + Translator.translateOutputMessage(EB_Expression_IS_NOT_VALID_COMMAND));
        }

        //Wenn am Ende nicht ")" steht.
        if (!input.substring(input.length() - 1, input.length()).equals(")")) {
            throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_MISSING_CLOSING_BRACKET, input));
        }

        result[1] = input.substring(result[0].length() + 1, input.length() - 1);

        return result;

    }

    /**
     * Input: String input, in der NUR die Parameter (getrennt durch ein Komma)
     * stehen. Beispiel input = "x,y,f(w,z),u,v". Paremeter sind dann {x, y,
     * f(w, z), u, v}. Nach einem eingelesenen Komma, welches NICHT von runden
     * Klammern umgeben ist, werden die Parameter getrennt.
     *
     * @throws ExpressionException
     */
    public static String[] getArguments(String input) throws ExpressionException {

        //Leerzeichen beseitigen
        input = input.replaceAll(" ", "");

        //Falls Parameterstring leer ist -> Fertig
        if (input.isEmpty()) {
            return new String[0];
        }

        ArrayList<String> resultParameters = new ArrayList<>();
        int startPositionOfCurrentParameter = 0;

        /*
         Differenz zwischen der Anzahl der ??ffnenden und der der schlie??enden
         Klammern (bracketCounter == 0 am Ende -> alles ok).
         */
        int bracketCounter = 0;
        int squareBracketCounter = 0;
        String currentChar;
        //Jetzt werden die einzelnen Parameter ausgelesen
        for (int i = 0; i < input.length(); i++) {

            currentChar = input.substring(i, i + 1);
            if (currentChar.equals("(")) {
                bracketCounter++;
            }
            if (currentChar.equals(")")) {
                bracketCounter--;
            }
            if (currentChar.equals("[")) {
                squareBracketCounter++;
            }
            if (currentChar.equals("]")) {
                squareBracketCounter--;
            }
            if (bracketCounter == 0 && squareBracketCounter == 0 && currentChar.equals(",")) {
                if (input.substring(startPositionOfCurrentParameter, i).isEmpty()) {
                    throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_EMPTY_PARAMETER));
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, i));
                startPositionOfCurrentParameter = i + 1;
            }
            if (i == input.length() - 1) {
                if (startPositionOfCurrentParameter == input.length()) {
                    throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_EMPTY_PARAMETER));
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, input.length()));
            }

        }

        if (bracketCounter != 0 || squareBracketCounter != 0) {
            throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_WRONG_BRACKETS));
        }

        String[] resultParametersAsArray = new String[resultParameters.size()];
        for (int i = 0; i < resultParameters.size(); i++) {
            resultParametersAsArray[i] = resultParameters.get(i);
        }

        return resultParametersAsArray;

    }

    /**
     * Pr??ft, ob es sich bei var um einen zul??ssigen Variablennamen handelt.
     * True wird genau dann zur??ckgegeben, wenn var ein Kleinbuchstabe ist,
     * eventuell gefolgt von '_' und einer nat??rlichen Zahl (als Index).
     * Beispielsweise wird bei y, x_2, z_4, true zur??ckgegeben, bei t_3_5
     * dagegen wird false zur??ckgegeben.
     */
    public static boolean isValidVariable(String var) {

        if (var.length() == 0) {
            return false;
        }

        //Falls der Ausdruck eine (einfache) Variable ist
        if (var.length() == 1 && (int) var.charAt(0) >= 97 && (int) var.charAt(0) <= 122) {
            return true;
        }

        //Falls der Ausdruck eine Variable mit Index ist (Form: Buchstabe_Index)
        if (var.length() >= 3 && (int) var.charAt(0) >= 97 && (int) var.charAt(0) <= 122
                && (int) var.charAt(1) == 95) {

            for (int i = 2; i < var.length(); i++) {
                if ((int) var.charAt(i) < 48 || (int) var.charAt(i) > 57) {
                    return false;
                }
            }
            return true;

        }

        return false;

    }

    /**
     * Pr??ft, ob es sich bei var um einen zul??ssigen Variablennamen handelt, und
     * ob zus??tzlich der entsprechenden Variable kein fester Wert zugewiesen
     * wurde.
     */
    public static boolean isValidIndeterminate(String var) {
        return isValidVariable(var) && Variable.create(var).getPreciseExpression() == null;
    }

    /**
     * Pr??ft, ob es sich bei var um einen zul??ssigen Variablennamen oder um die
     * formale Ableitung einer zul??ssigen Variable handelt, und ob zus??tzlich
     * der entsprechenden Variable kein fester Wert zugewiesen wurde.
     */
    public static boolean isValidDerivativeOfIndeterminate(String var) {
        return VALIDATOR.isValidIdentifier(var) && Variable.create(var).getPreciseExpression() == null;
    }

    /**
     * Pr??ft, ob es sich bei formula um die Konstante pi handelt.
     */
    public static boolean isPI(String formula) {
        return formula.equals("pi");
    }

    /**
     * Pr??ft, ob es sich bei formula um eine Bin??roperation handelt.
     */
    private static boolean isOperation(String formula) {
        return formula.equals("+") || formula.equals("-") || formula.equals("*") || formula.equals("/") || formula.equals("^");
    }

    /**
     * Hauptmethode zum Erstellen einer Expression aus einem String.
     *
     * @throws ExpressionException
     */
    public static Expression build(String formula) throws ExpressionException {
        return build(formula, null, null);
    }

    /**
     * Hauptmethode zum Erstellen einer Expression aus einem String.
     *
     * @throws ExpressionException
     */
    public static Expression build(String formula, HashSet<String> vars) throws ExpressionException {
        return build(formula, vars, VALIDATOR);
    }
    
    /**
     * Hauptmethode zum Erstellen einer Expression aus einem String.
     *
     * @throws ExpressionException
     */
    public static Expression build(String formula, IdentifierValidator validator) throws ExpressionException {
        return build(formula, null, VALIDATOR);
    }
    
    /**
     * Hauptmethode zum Erstellen einer Expression aus einem String.
     *
     * @throws ExpressionException
     */
    public static Expression build(String formula, HashSet<String> vars, IdentifierValidator validator) throws ExpressionException {

        // Leerzeichen beseitigen und alles zu Kleinbuchstaben machen
        formula = formula.replaceAll(" ", "").toLowerCase();

        // Priorit??ten: + = 0, - = 1, * = 2, / = 3, ^ = 4, Zahl, Var, Funktion, Operator = 5.
        int priority = 5;
        int breakpoint = -1;
        int bracketCounter = 0;
        int absBracketCounter = 0;
        int formulaLength = formula.length();
        String currentChar;

        if (formula.isEmpty()) {
            throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_EXPRESSION_EMPTY_OR_INCOMPLETE));
        }

        // Pr??fen, ob nicht zwei Operatoren nacheinander auftreten.
        for (int i = 0; i < formulaLength - 1; i++) {
            if (isOperation(formula.substring(i, i + 1)) && isOperation(formula.substring(i + 1, i + 2))) {
                throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_TWO_OPERATIONS));
            }
        }

        for (int i = 1; i <= formulaLength; i++) {
            currentChar = formula.substring(formulaLength - i, formulaLength - i + 1);

            // ??ffnende und schlie??ende Klammern z??hlen.
            if (currentChar.equals("(") && bracketCounter == 0) {
                throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_WRONG_BRACKETS));
            }

            if (currentChar.equals(")")) {
                bracketCounter++;
            }
            if (currentChar.equals("(")) {
                bracketCounter--;
            }

            // ??ffnende und schlie??ende Betragsklammern z??hlen.
            char charWithinAbsBrackets;
            if (currentChar.equals("|") && formulaLength - i - 1 >= 0) {

                int k = 1;
                //Aufeinanderfolgende Betragsstriche werden gez??hlt
                while (formulaLength - i - k >= 0 && (int) formula.charAt(formulaLength - i - k) == 124) {
                    k++;
                }

                if (formulaLength - i - k >= 0) {
                    charWithinAbsBrackets = formula.charAt(formulaLength - i - k);
                } else {
                    absBracketCounter = absBracketCounter - k;
                    if (absBracketCounter != 0) {
                        throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_WRONG_ABS_BRACKETS));
                    }
                    break;
                }

                if (formulaLength - i - k == 0) {
                    absBracketCounter = absBracketCounter - k;
                    i = i + k - 1;
                } else if ((int) charWithinAbsBrackets >= 97 && (int) charWithinAbsBrackets <= 122
                        || (int) charWithinAbsBrackets >= 48 && (int) charWithinAbsBrackets <= 57
                        || (int) charWithinAbsBrackets == 95 || (int) charWithinAbsBrackets == 39
                        || (int) charWithinAbsBrackets == 41) {
                    /*
                     Dann steht links von einer |-Kette eine Zahl oder ein
                     Buchstabe oder ein '_' oder ein ''' oder ")" 
                     -> Es ist eine Ketten von schlie??enden Betragsklammern.
                     */
                    absBracketCounter = absBracketCounter + k;
                    i = i + k - 1;
                } else {
                    // Andernfalls ist eine Ketten von ??ffnenden Betragsklammern.
                    absBracketCounter = absBracketCounter - k;
                    i = i + k - 1;
                }

            } else if (currentChar.equals("|") && i == formulaLength) {
                absBracketCounter--;
            }

            if (absBracketCounter < 0) {
                throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_WRONG_ABS_BRACKETS));
            }

            if (bracketCounter != 0 || absBracketCounter != 0) {
                continue;
            }
            // Aufteilungspunkt finden; zun??chst wird nach -, +, *, /, ^ gesucht 
            // breakpoint gibt den Index in formula an, wo die Formel aufgespalten werden soll.
            if (currentChar.equals("+") && priority > 0) {
                priority = 0;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("-") && priority > 1) {
                priority = 1;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("*") && priority > 2) {
                priority = 2;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("/") && priority > 3) {
                priority = 3;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("^") && priority > 4) {
                priority = 4;
                breakpoint = formulaLength - i;
            }
        }

        if (bracketCounter > 0) {
            throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_WRONG_BRACKETS));
        }
        if (absBracketCounter > 0) {
            throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_WRONG_ABS_BRACKETS));
        }

        // Aufteilung, falls eine Elementaroperation (-, +, /, *, ^) vorliegt
        if (priority <= 4) {
            String formulaLeft = formula.substring(0, breakpoint);
            String formulaRight = formula.substring(breakpoint + 1, formulaLength);

            if (formulaLeft.isEmpty() && priority > 1) {
                throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_LEFT_SIDE_OF_BINARY_IS_EMPTY));
            }
            if (formulaRight.isEmpty()) {
                throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_RIGHT_SIDE_OF_BINARY_IS_EMPTY));
            }

            //Falls der Ausdruck die Form "+abc..." besitzt -> daraus "abc..." machen
            if (formulaLeft.isEmpty() && priority == 0) {
                return build(formulaRight, vars);
            }
            //Falls der Ausdruck die Form "-abc..." besitzt -> daraus "(-1)*abc..." machen
            if (formulaLeft.isEmpty() && priority == 1) {
                Expression right = build(formulaRight, vars);
                /* 
                 Konstanten und Verh??ltnisse von Konstanten bilden Ausnahmen: Dann wird das 
                 Minuszeichen direkt in den Z??hler gezogen.
                 */
                if (right instanceof Constant && ((Constant) right).getValue().compareTo(BigDecimal.ZERO) >= 0) {
                    return new Constant(((Constant) right).getValue().negate());
                } else if (right.isRationalConstant() && ((BinaryOperation) right).getLeft().isNonNegative()) {
                    return new Constant(((Constant) ((BinaryOperation) right).getLeft()).getValue().negate()).div(((BinaryOperation) right).getRight());
                }
                return MINUS_ONE.mult(build(formulaRight, vars));
            }
            switch (priority) {
                case 0:
                    return new BinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeBinary.PLUS);
                case 1:
                    return new BinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeBinary.MINUS);
                case 2:
                    return new BinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeBinary.TIMES);
                case 3:
                    return new BinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeBinary.DIV);
                default:
                    return new BinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeBinary.POW);
            }
        }

        // Falls kein bin??rer Operator und die Formel die Form (...) hat -> Klammern beseitigen.
        if ((priority == 5) && (formula.substring(0, 1).equals("(")) && (formula.substring(formulaLength - 1, formulaLength).equals(")"))) {
            return build(formula.substring(1, formulaLength - 1), vars);
        }

        //Falls der Ausdruck eine Zahl ist.
        if (priority == 5) {
            try {
                return new Constant(new BigDecimal(formula));
            } catch (NumberFormatException e) {
            }
        }

        //Falls der Ausdruck eine Variable ist.
        if (priority == 5) {
            if (VALIDATOR.isValidIdentifier(formula)) {
                if (vars != null) {
                    vars.add(formula);
                }
                return Variable.create(formula);
            }
            if (isPI(formula)) {
                return Variable.create(formula, Math.PI);
            }
        }

        //AUSNAHME: |...| = abs(...), falls es klappt!
        if (formula.substring(0, 1).equals("|") && formula.substring(formula.length() - 1, formula.length()).equals("|")) {
            Expression formulaInAbsBrackets = Expression.build(formula.substring(1, formula.length() - 1), vars);
            return new Function(formulaInAbsBrackets, TypeFunction.abs);
        }

        //Falls der Ausdruck eine Funktion ist.
        if (priority == 5) {
            int functionNameLength;
            for (TypeFunction type : TypeFunction.values()) {
                functionNameLength = type.toString().length();
                //Falls der Ausdruck die Form function(...) hat -> Funktion und Argument auslesen
                if (formula.length() >= functionNameLength + 2) {
                    if ((formula.substring(0, functionNameLength).equals(type.toString()))
                            && (formula.substring(functionNameLength, functionNameLength + 1).equals("("))
                            && (formula.substring(formulaLength - 1, formulaLength).equals(")"))) {

                        String functionArgument = formula.substring(functionNameLength + 1, formulaLength - 1);
                        if (type.equals(TypeFunction.sqrt)) {
                            // Die Wurzel wird intern sofort als (...)^(1/2) aufgefasst.
                            return build(functionArgument, vars).pow(ONE.div(TWO));
                        }
                        return new Function(build(functionArgument, vars), type);

                    }
                }
            }
        }

        //AUSNAHME: Operator Fakult??t (== !).
        if (priority == 5) {
            if (formula.substring(formula.length() - 1, formula.length()).equals("!")) {
                Expression[] params = new Expression[1];
                params[0] = Expression.build(formula.substring(0, formula.length() - 1), vars);
                return new Operator(TypeOperator.fac, params);
            }
        }

        //Falls der Ausdruck ein Operator ist.
        if (priority == 5) {
            String[] operatorNameAndParams = getOperatorAndArguments(formula);
            String[] params = getArguments(operatorNameAndParams[1]);
            String operatorName;
            for (TypeOperator type : TypeOperator.values()) {
                operatorName = Operator.getNameFromType(type);
                if (operatorNameAndParams[0].equals(operatorName)) {
                    return Operator.getOperator(operatorName, params, vars);
                }
            }
        }

        //Falls der Ausdruck eine vom Benutzer selbstdefinierte Funktion ist.
        if (priority == 5) {
            String function = getOperatorAndArguments(formula)[0];
            String[] functionArguments = getArguments(getOperatorAndArguments(formula)[1]);

            if (SelfDefinedFunction.getInnerExpressionsForSelfDefinedFunctions().containsKey(function)) {
                if (SelfDefinedFunction.getArgumentsForSelfDefinedFunctions().get(function).length == functionArguments.length) {
                    Expression[] exprsInArguments = new Expression[functionArguments.length];
                    for (int i = 0; i < functionArguments.length; i++) {
                        exprsInArguments[i] = Expression.build(functionArguments[i], vars);
                    }
                    return new SelfDefinedFunction(function, SelfDefinedFunction.getArgumentsForSelfDefinedFunctions().get(function),
                            SelfDefinedFunction.getAbstractExpressionsForSelfDefinedFunctions().get(function), exprsInArguments);
                } else {
                    throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_WRONG_NUMBER_OF_PARAMETERS_IN_SELF_DEFINED_FUNCTION, function, String.valueOf(SelfDefinedFunction.getArgumentsForSelfDefinedFunctions().get(function).length)));
                }
            }

        }

        throw new ExpressionException(Translator.translateOutputMessage(EB_Expression_FORMULA_CANNOT_BE_INTERPRETED, formula));

    }

    /**
     * Gibt ein HashSet mit den Namen der Variablen zur??ck, die formal von der
     * Variablen mit dem Namen varName abh??ngen und die im gegebenen Ausdruck
     * vorkommen.<br>
     * BEISPIEL: Ist varName = "y''" und der gegebene Ausdruck =
     * "x+y'+y''''+sin(y'')", so wird ein HashSet mit den Elementen "y''" und
     * "y''''" zur??ckgegeben.
     */
    public HashSet<String> getContainedVariablesDependingOnGivenVariable(String varName) {
        HashSet<String> vars = new HashSet<>();
        HashSet<String> allVarsDependingOnGivenVariable = Variable.getVariablesDependingOnGivenVariable(varName);
        for (String var : allVarsDependingOnGivenVariable) {
            if (Variable.create(var).getDependingVariable().equals(varName)) {
                vars.add(var);
            }
        }
        return vars;

    }

    /**
     * Gibt eine neue Kopie vom gegebenen Ausdruck zur??ck.
     */
    public abstract Expression copy();

    /**
     * Liefert den Wert des gegebenen Ausdrucks unter Einsetzung aller
     * Variablenwerte.
     *
     * @throws EvaluationException
     */
    public abstract double evaluate() throws EvaluationException;

    /**
     * F??gt alle Variablen, die in dem gegebenen Ausdruck vorkommen, zum HashSet
     * vars hinzu.
     */
    @Override
    public abstract void addContainedVars(HashSet<String> vars);

    /**
     * Gibt ein HashSet mit allen Variablen, die in dem gegebenen Ausdruck
     * vorkommen, zur??ck.
     */
    @Override
    public HashSet<String> getContainedVars() {
        HashSet<String> vars = new HashSet<>();
        addContainedVars(vars);
        return vars;
    }

    /**
     * F??gt alle Variablen, denen kein Wert zugewiesen wurde und die in dem
     * gegebenen Ausdruck vorkommen, zum HashSet vars hinzu.
     */
    @Override
    public abstract void addContainedIndeterminates(HashSet<String> vars);

    /**
     * Gibt ein HashSet mit allen Variablen, denen kein Wert zugewiesen wurde
     * und die in dem gegebenen Ausdruck vorkommen, zur??ck.
     */
    @Override
    public HashSet<String> getContainedIndeterminates() {
        HashSet<String> vars = new HashSet<>();
        addContainedIndeterminates(vars);
        return vars;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck die Variable var enth??lt.
     */
    @Override
    public abstract boolean contains(String var);

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck mindestens eine der Variable aus
     * vars enth??lt.
     */
    public boolean containsAtLeastOne(HashSet<String> vars) {
        for (String var : vars) {
            if (this.contains(var)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck nichtexakte Konstanten enth??lt.
     */
    public abstract boolean containsApproximates();

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck Funktionen enth??lt.
     */
    public abstract boolean containsFunction();

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck Exponentialfunktionen enth??lt.
     */
    public abstract boolean containsExponentialFunction();

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck trigonometrische Funktionen
     * enth??lt.
     */
    public abstract boolean containsTrigonometricalFunction();

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck unbestimmte Integrale enth??lt.
     */
    public abstract boolean containsIndefiniteIntegral();

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck Operatoren enth??lt.
     */
    public abstract boolean containsOperator();

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck einen Operator vom Type type
     * enth??lt.
     */
    public abstract boolean containsOperator(TypeOperator type);

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine algebraische Operation enth??lt
     * (d.h. Exponenten von der Form p/q mit ganzen p und q und |q| &#8805; 2).
     */
    public abstract boolean containsAlgebraicOperation();

    /**
     * Setzt alle im gegebenen Ausdruck vorkommenden Konstanten auf
     * 'approximativ' (precise = false).
     */
    public abstract Expression turnToApproximate();

    /**
     * Setzt alle im gegebenen Ausdruck vorkommenden Konstanten auf 'exakt'
     * (precise = true).
     */
    public abstract Expression turnToPrecise();

    /**
     * Ersetzt im gegebenen Ausdruck die Variable var durch den Ausdruck expr.
     */
    public abstract Expression replaceVariable(String var, Expression expr);

    /**
     * Schreibt eine vom Benutzer definierte Funktion in den ??blichen
     * vordefinierten Termen aus.<br>
     * BEISPIEL: Der Benutzer definiert def(f(x) = exp(x)+x^2). Dann liefert
     * diese Methode f??r den Ausdruck expr = f(u) den Ausdruck exp(u)+u^2
     * zur??ck. Technische Umsetzung: Alle Instanzen von Klassen, au??er
     * SelfDefinedFunction, werden gleich gelassen. In Instanzen von
     * SelfDefinedFunction wird im abstrakten Ausdruck jeder Parameter durch den
     * entsprechenden konkreten Eintrag ersetzt.
     */
    public abstract Expression replaceSelfDefinedFunctionsByPredefinedFunctions();

    /**
     * Differenziert den gegebenen Ausdruck nach der Variablen var
     *
     * @throws EvaluationException
     */
    public abstract Expression diff(String var) throws EvaluationException;

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck konstant ist
     */
    public abstract boolean isConstant();

    /**
     * Liefert true, falls der gegebene Ausdruck konstant ist und mit Sicherheit
     * mindestens 0 ist. Im ungewissen Fall wird false ausgegeben.
     */
    public abstract boolean isNonNegative();

    /**
     * Liefert true, falls der gegebene Ausdruck konstant ist und mit Sicherheit
     * h??chstens 0 ist. Im ungewissen Fall wird false ausgegeben.
     */
    public abstract boolean isNonPositive();

    /**
     * Liefert true, falls der gegebene Ausdruck konstant ist und mit Sicherheit
     * &#62; 0 ist. Im ungewissen Fall wird false ausgegeben.
     */
    public boolean isPositive() {
        return this.isNonNegative() && !this.equals(ZERO);
    }

    /**
     * Liefert true, falls der gegebene Ausdruck konstant ist und mit Sicherheit
     * &#60; 0 ist. Im ungewissen Fall wird false ausgegeben.
     */
    public boolean isNegative() {
        return this.isNonPositive() && !this.equals(ZERO);
    }

    /**
     * Liefert true, falls der gegebene Ausdruck definiv immer nichtnegativ ist
     * (z.B. x^2+y^4 etc.)
     */
    public abstract boolean isAlwaysNonNegative();

    /**
     * Liefert true, falls der gegebene Ausdruck definiv immer positiv ist (z.B.
     * 1+x^2+y^4 etc.)
     */
    public abstract boolean isAlwaysPositive();

    /**
     * Liefert true, falls der gegebene Ausdruck definiv immer nichtpositiv ist
     * (z.B. -x^2-y^4 etc.)
     */
    public abstract boolean isAlwaysNonPositive();

    /**
     * Liefert true, falls der gegebene Ausdruck definiv immer positiv ist (z.B.
     * -1-x^2-y^4 etc.)
     */
    public abstract boolean isAlwaysNegative();

    /**
     * Pr??ft, ob der gegebene Ausdruck ein eine rationale Konstante ist.
     */
    public boolean isRationalConstant() {
        return this.isQuotient()
                && ((BinaryOperation) this).getLeft().isIntegerConstant()
                && ((BinaryOperation) this).getRight().isIntegerConstant();
    }

    /**
     * Pr??ft, ob der gegebene Ausdruck eine ganzzahlige oder eine rationale
     * Konstante ist.
     */
    public boolean isIntegerConstantOrRationalConstant() {
        return this.isIntegerConstant() || this.isRationalConstant();
    }

    /**
     * Pr??ft, ob der gegebene Ausdruck eine negative Konstante oder ein
     * negativer Bruch ist.
     */
    public boolean isIntegerConstantOrRationalConstantNegative() {
        if (this instanceof Constant) {
            return (((Constant) this).getValue().compareTo(BigDecimal.ZERO) < 0);
        }
        if (this.isRationalConstant()) {
            return (((Constant) ((BinaryOperation) this).getLeft()).getValue().multiply(
                    ((Constant) ((BinaryOperation) this).getRight()).getValue())).compareTo(BigDecimal.ZERO) < 0;
        }
        return false;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine ganzzahlige Konstante ist.
     */
    public boolean isIntegerConstant() {
        return this instanceof Constant && ((Constant) this).getValue().compareTo(((Constant) this).getValue().setScale(0, BigDecimal.ROUND_HALF_UP)) == 0;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine nichtnegative ganzzahlige
     * Konstante ist.
     */
    public boolean isNonNegativeIntegerConstant() {
        return isIntegerConstant() && ((Constant) this).getValue().compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine nichtpositive ganzzahlige
     * Konstante ist.
     */
    public boolean isNonPositiveIntegerConstant() {
        return isIntegerConstant() && ((Constant) this).getValue().compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine positive ganzzahlige Konstante
     * ist.
     */
    public boolean isPositiveIntegerConstant() {
        return isIntegerConstant() && ((Constant) this).getValue().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine negative ganzzahlige Konstante
     * ist.
     */
    public boolean isNegativeIntegerConstant() {
        return isIntegerConstant() && ((Constant) this).getValue().compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine ungerade Konstante ist.
     */
    public boolean isOddIntegerConstant() {
        if (this.isIntegerConstant()) {
            BigInteger value = ((Constant) this).getBigIntValue();
            if (value.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ONE) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine positive ungerade Konstante
     * ist.
     */
    public boolean isPositiveOddIntegerConstant() {
        if (this.isIntegerConstant()) {
            BigInteger value = ((Constant) this).getBigIntValue();
            if (value.compareTo(BigInteger.ZERO) > 0 && value.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ONE) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine gerade Konstante ist.
     */
    public boolean isEvenIntegerConstant() {
        if (this.isIntegerConstant()) {
            BigInteger value = ((Constant) this).getBigIntValue();
            if (value.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine positive gerade Konstante ist.
     */
    public boolean isPositiveEvenIntegerConstant() {
        if (this.isIntegerConstant()) {
            BigInteger value = ((Constant) this).getBigIntValue();
            if (value.compareTo(BigInteger.ZERO) > 0 && value.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Hilfsmethode f??r eine toString()-Implementierung und f??r das Zeichnen von
     * Ausdr??cken. Gibt zur??ck, ob der Ausdruck mit einem negativen Vorzeichen
     * anf??ngt.<br>
     * BEISPIEL: ist der gegebene Ausdruck = (-2)*3 wird true zur??ckgegeben, bei
     * x*(-7)*5 wird false zur??ckgegeben.
     */
    public boolean doesExpressionStartWithAMinusSign() {

        if (this instanceof Constant) {
            return ((Constant) this).getValue().compareTo(BigDecimal.ZERO) < 0;
        }
        if (this.isProduct() || this.isQuotient()) {
            return ((BinaryOperation) this).getLeft().doesExpressionStartWithAMinusSign();
        }
        return false;

    }

    /**
     * Negiert den gegebenen Ausdruck.
     */
    public Expression negate() {
        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(this);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(this);
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i) instanceof Constant) {
                factorsEnumerator.put(i, new Constant(BigDecimal.valueOf(-1).multiply(((Constant) factorsEnumerator.get(i)).getValue()),
                        ((Constant) factorsEnumerator.get(i)).getPrecise()));
                return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);
            }
        }
        return MINUS_ONE.mult(this);
    }

    /**
     * Generierung eines Latex-Codes aus dem gegebenen Ausdruck.
     */
    public abstract String expressionToLatex();

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine identische Kopie von expr
     * darstellt.
     */
    public abstract boolean equals(Expression expr);

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck ??quivalent zu dem von expr ist.
     */
    public abstract boolean equivalent(Expression expr);

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck bis auf das Vorzeichen ??quivalent
     * zu dem von expr ist.
     */
    public abstract boolean antiEquivalent(Expression expr);

    /**
     * Liefert true, wenn der Ausdruck this einen nichtnegativen Koeffizienten
     * besitzt, falls man this als Produkt auffasst.<br>
     * BEISPIELE: (1) F??r expr =2*x*(-3)*y wird false zur??ckgegeben, da expr,
     * welches gleich (-6)*x*y ist, einen negativen Koeffizienten besitzt.<br>
     * (2) F??r expr = x + 3*y wird true zur??ckgegeben, da der Koeffizient 1 ist,
     * wenn man expr als Produkt auffasst.<br>
     * (3) F??r expr = (-5)*x*(-3)*y wird true zur??ckgegeben.
     */
    public abstract boolean hasPositiveSign();

    /**
     * Ermittelt ein Ma?? f??r die "L??nge" des gegebenen Ausdrucks.
     */
    public abstract int getLength();

    /**
     * Ermittelt die maximale Anzahl von Summanden, die im gegebenen Ausdruck
     * vorkommen, wenn man simplifyExpand() anwendet und BEVOR man wieder
     * zusammenfasst. Wenn die gesch??tzte Anzahl der Summanden gr????er ist als
     * Integer.MAX_VALUE, wird Integer.MAX_VALUE zur??ckgegeben.
     */
    public abstract int getMaximalNumberOfSummandsInExpansion();

    /**
     * Addiert den gegebenen Ausdruck zu expr.
     */
    public Expression add(Expression expr) {
        if (this.equals(ZERO)) {
            return expr;
        }
        if (expr.equals(ZERO)) {
            return this;
        }
        return new BinaryOperation(this, expr, TypeBinary.PLUS);
    }

    /**
     * Addiert den gegebenen Ausdruck zu a.
     */
    public Expression add(BigDecimal a) {
        if (this.equals(ZERO)) {
            return new Constant(a);
        }
        if (a.equals(BigDecimal.ZERO)) {
            return this;
        }
        return this.add(new Constant(a));
    }

    /**
     * Addiert den gegebenen Ausdruck zu a.
     */
    public Expression add(BigInteger a) {
        if (this.equals(ZERO)) {
            return new Constant(a);
        }
        if (a.equals(BigInteger.ZERO)) {
            return this;
        }
        return this.add(new Constant(a));
    }

    /**
     * Addiert den gegebenen Ausdruck zu a.
     */
    public Expression add(int a) {
        if (this.equals(ZERO)) {
            return new Constant(a);
        }
        if (a == 0) {
            return this;
        }
        return this.add(new Constant(a));
    }

    /**
     * Addiert die Variable mit dem Namen var zum gegebenen Ausdruck.
     */
    public Expression add(String var) {
        return this.add(Variable.create(var));
    }

    /**
     * Subtrahiert expr vom gegebenen Ausdruck.
     */
    public Expression sub(Expression expr) {
        if (this.equals(ZERO) && expr.equals(ZERO)) {
            return ZERO;
        }
        if (this.equals(ZERO)) {
            return MINUS_ONE.mult(expr);
        }
        if (expr.equals(ZERO)) {
            return this;
        }
        return new BinaryOperation(this, expr, TypeBinary.MINUS);
    }

    /**
     * Subtrahiert a vom gegebenen Ausdruck.
     */
    public Expression sub(BigDecimal a) {
        if (this.equals(ZERO) && a.equals(BigDecimal.ZERO)) {
            return ZERO;
        }
        if (this.equals(ZERO)) {
            return new Constant(a.negate());
        }
        if (a.equals(BigDecimal.ZERO)) {
            return this;
        }
        return this.sub(new Constant(a));
    }

    /**
     * Subtrahiert a vom gegebenen Ausdruck.
     */
    public Expression sub(BigInteger a) {
        if (this.equals(ZERO) && a.equals(BigInteger.ZERO)) {
            return ZERO;
        }
        if (this.equals(ZERO)) {
            return new Constant(a.negate());
        }
        if (a.equals(BigInteger.ZERO)) {
            return this;
        }
        return this.sub(new Constant(a));
    }

    /**
     * Subtrahiert a vom gegebenen Ausdruck.
     */
    public Expression sub(int a) {
        if (this.equals(ZERO) && a == 0) {
            return ZERO;
        }
        if (this.equals(ZERO)) {
            return new Constant(-a);
        }
        if (a == 0) {
            return this;
        }
        return this.sub(new Constant(a));
    }

    /**
     * Subtrahiert vom gegebenen Ausdruck die Variable mit dem Namen var.
     */
    public Expression sub(String var) {
        return this.sub(Variable.create(var));
    }

    /**
     * Multipliziert den gegebenen Ausdruck mit expr.
     */
    public Expression mult(Expression expr) {
        if (this.equals(ZERO) || expr.equals(ZERO)) {
            return ZERO;
        }
        if (this.equals(Expression.ONE)) {
            return expr;
        }
        if (expr.equals(Expression.ONE)) {
            return this;
        }
        return new BinaryOperation(this, expr, TypeBinary.TIMES);
    }

    /**
     * Multipliziert den gegebenen Ausdruck mit a.
     */
    public Expression mult(BigDecimal a) {
        if (this.equals(ZERO) || a.equals(BigDecimal.ZERO)) {
            return ZERO;
        }
        if (this.equals(Expression.ONE)) {
            return new Constant(a);
        }
        if (a.equals(BigDecimal.ONE)) {
            return this;
        }
        return this.mult(new Constant(a));
    }

    /**
     * Multipliziert den gegebenen Ausdruck mit a.
     */
    public Expression mult(BigInteger a) {
        if (this.equals(ZERO) || a.equals(BigInteger.ZERO)) {
            return ZERO;
        }
        if (this.equals(Expression.ONE)) {
            return new Constant(a);
        }
        if (a.equals(BigInteger.ONE)) {
            return this;
        }
        return this.mult(new Constant(a));
    }

    /**
     * Multipliziert den gegebenen Ausdruck mit a.
     */
    public Expression mult(int a) {
        if (this.equals(ZERO) || a == 0) {
            return ZERO;
        }
        if (this.equals(Expression.ONE)) {
            return new Constant(a);
        }
        if (a == 1) {
            return this;
        }
        return this.mult(new Constant(a));
    }

    /**
     * Multipliziert den gegebenen Ausdruck mit der Variablen mit dem Namen var.
     */
    public Expression mult(String var) {
        return this.mult(Variable.create(var));
    }

    /**
     * Dividiert den gegebenen Ausdruck durch expr.
     */
    public Expression div(Expression expr) {
        if (expr.equals(Expression.ONE)) {
            return this;
        }
        return new BinaryOperation(this, expr, TypeBinary.DIV);
    }

    /**
     * Dividiert den gegebenen Ausdruck durch a.
     */
    public Expression div(BigDecimal a) {
        if (this.equals(ZERO) && !a.equals(BigDecimal.ZERO)) {
            return ZERO;
        }
        if (a.equals(BigDecimal.ONE)) {
            return this;
        }
        return this.div(new Constant(a));
    }

    /**
     * Dividiert den gegebenen Ausdruck durch a.
     */
    public Expression div(BigInteger a) {
        if (this.equals(ZERO) && !a.equals(BigInteger.ZERO)) {
            return ZERO;
        }
        if (a.equals(BigInteger.ONE)) {
            return this;
        }
        return this.div(new Constant(a));
    }

    /**
     * Dividiert den gegebenen Ausdruck durch a.
     */
    public Expression div(int a) {
        if (this.equals(ZERO) && a != 0) {
            return ZERO;
        }
        if (a == 1) {
            return this;
        }
        return this.div(new Constant(a));
    }

    /**
     * Dividiert den gegebenen Ausdruck durch die Variablen mit dem Namen var.
     */
    public Expression div(String var) {
        return this.div(Variable.create(var));
    }

    /**
     * Potenziert den gegebenen Ausdruck mit expr.
     */
    public Expression pow(Expression expr) {
        if (expr.equals(ZERO)) {
            return ONE;
        }
        if (expr.equals(ONE)) {
            return this;
        }
        if (this.equals(ONE)) {
            return ONE;
        }
        return new BinaryOperation(this, expr, TypeBinary.POW);
    }

    /**
     * Potenziert den gegebenen Ausdruck mit a.
     */
    public Expression pow(BigDecimal a) {
        if (a.equals(BigDecimal.ZERO)) {
            return ONE;
        }
        if (a.equals(BigDecimal.ONE)) {
            return this;
        }
        return this.pow(new Constant(a));
    }

    /**
     * Potenziert den gegebenen Ausdruck mit a.
     */
    public Expression pow(BigInteger a) {
        if (a.equals(BigInteger.ZERO)) {
            return ONE;
        }
        if (a.equals(BigInteger.ONE)) {
            return this;
        }
        return this.pow(new Constant(a));
    }

    /**
     * Potenziert den gegebenen Ausdruck mit a.
     */
    public Expression pow(int a) {
        if (a == 0) {
            return ONE;
        }
        if (a == 1) {
            return this;
        }
        return this.pow(new Constant(a));
    }

    /**
     * Potenziert den gegebenen Ausdruck mit der Variablen mit dem Namen var.
     */
    public Expression pow(String var) {
        return this.pow(Variable.create(var));
    }

    /**
     * Potenziert den gegebenen Ausdruck mit m/n.
     */
    public Expression pow(Expression m, Expression n) {
        return this.pow(m.div(n));
    }

    /**
     * Potenziert den gegebenen Ausdruck mit m/n.
     */
    public Expression pow(BigDecimal m, BigDecimal n) {
        return this.pow((new Constant(m).div(new Constant(n))));
    }

    /**
     * Potenziert den gegebenen Ausdruck mit m/n.
     */
    public Expression pow(BigInteger m, BigInteger n) {
        return this.pow((new Constant(m).div(new Constant(n))));
    }

    /**
     * Potenziert den gegebenen Ausdruck mit m/n.
     */
    public Expression pow(int m, int n) {
        return this.pow((new Constant(m).div(new Constant(n))));
    }

    /**
     * Gibt den Betrag des Ausdrucks zur??ck.
     */
    public Function abs() {
        return new Function(this, TypeFunction.abs);
    }

    /**
     * Gibt den Arkuskosinus des Ausdrucks zur??ck.
     */
    public Function arccos() {
        return new Function(this, TypeFunction.arccos);
    }

    /**
     * Gibt den Arkuskosekans des Ausdrucks zur??ck.
     */
    public Function arccosec() {
        return new Function(this, TypeFunction.arccosec);
    }

    /**
     * Gibt den Arkuskotangens des Ausdrucks zur??ck.
     */
    public Function arccot() {
        return new Function(this, TypeFunction.arccot);
    }

    /**
     * Gibt den hyperbolichen Areakosekans des Ausdrucks zur??ck.
     */
    public Function arcosech() {
        return new Function(this, TypeFunction.arcosech);
    }

    /**
     * Gibt den hyperbolichen Areakosinus des Ausdrucks zur??ck.
     */
    public Function arcosh() {
        return new Function(this, TypeFunction.arcosh);
    }

    /**
     * Gibt den hyperbolichen Areakotangens des Ausdrucks zur??ck.
     */
    public Function arcoth() {
        return new Function(this, TypeFunction.arcoth);
    }

    /**
     * Gibt den Arkussekans des Ausdrucks zur??ck.
     */
    public Function arcsec() {
        return new Function(this, TypeFunction.arcsec);
    }

    /**
     * Gibt den Arkussinus des Ausdrucks zur??ck.
     */
    public Function arcsin() {
        return new Function(this, TypeFunction.arcsin);
    }

    /**
     * Gibt den Arkustangens des Ausdrucks zur??ck.
     */
    public Function arctan() {
        return new Function(this, TypeFunction.arctan);
    }

    /**
     * Gibt den hyperbolischen Areasekans des Ausdrucks zur??ck.
     */
    public Function arsech() {
        return new Function(this, TypeFunction.arsech);
    }

    /**
     * Gibt den hyperbolischen Areasinus des Ausdrucks zur??ck.
     */
    public Function arsinh() {
        return new Function(this, TypeFunction.arsinh);
    }

    /**
     * Gibt den hyperbolischen Areatangens des Ausdrucks zur??ck.
     */
    public Function artanh() {
        return new Function(this, TypeFunction.artanh);
    }

    /**
     * Gibt den Kosinus des Ausdrucks zur??ck.
     */
    public Function cos() {
        return new Function(this, TypeFunction.cos);
    }

    /**
     * Gibt den Kosekans des Ausdrucks zur??ck.
     */
    public Function cosec() {
        return new Function(this, TypeFunction.cosec);
    }

    /**
     * Gibt den hyperbolischen Kosekans des Ausdrucks zur??ck.
     */
    public Function cosech() {
        return new Function(this, TypeFunction.cosech);
    }

    /**
     * Gibt den hyperbolischen Kosinus des Ausdrucks zur??ck.
     */
    public Function cosh() {
        return new Function(this, TypeFunction.cosh);
    }

    /**
     * Gibt den Kotangens des Ausdrucks zur??ck.
     */
    public Function cot() {
        return new Function(this, TypeFunction.cot);
    }

    /**
     * Gibt den hyperbolischen Kotangens des Ausdrucks zur??ck.
     */
    public Function coth() {
        return new Function(this, TypeFunction.coth);
    }

    /**
     * Gibt die Exponentialfunktion des Ausdrucks zur??ck.
     */
    public Function exp() {
        return new Function(this, TypeFunction.exp);
    }

    /**
     * Gibt die Identit??tsfunktion des Ausdrucks zur??ck.
     */
    public Function id() {
        return new Function(this, TypeFunction.id);
    }

    /**
     * Gibt den dekadischen Logarithmus des Ausdrucks zur??ck.
     */
    public Function lg() {
        return new Function(this, TypeFunction.lg);
    }

    /**
     * Gibt den nat??rlichen Logarithmus des Ausdrucks zur??ck.
     */
    public Function ln() {
        return new Function(this, TypeFunction.ln);
    }

    /**
     * Gibt den Sekans des Ausdrucks zur??ck.
     */
    public Function sec() {
        return new Function(this, TypeFunction.sec);
    }

    /**
     * Gibt den hyperbolischen Sekans des Ausdrucks zur??ck.
     */
    public Function sech() {
        return new Function(this, TypeFunction.sech);
    }

    /**
     * Gibt das Signum des Ausdrucks zur??ck.
     */
    public Function sgn() {
        return new Function(this, TypeFunction.sgn);
    }

    /**
     * Gibt den Sinus des Ausdrucks zur??ck.
     */
    public Function sin() {
        return new Function(this, TypeFunction.sin);
    }

    /**
     * Gibt den hyperbolischen Sinus des Ausdrucks zur??ck.
     */
    public Function sinh() {
        return new Function(this, TypeFunction.sinh);
    }

    /**
     * Gibt die Quadratwurzel des Ausdrucks zur??ck.
     */
    public Function sqrt() {
        return new Function(this, TypeFunction.sqrt);
    }

    /**
     * Gibt den Tangens des Ausdrucks zur??ck.
     */
    public Function tan() {
        return new Function(this, TypeFunction.tan);
    }

    /**
     * Gibt den hyperbolischen Tangens des Ausdrucks zur??ck.
     */
    public Function tanh() {
        return new Function(this, TypeFunction.tanh);
    }

    /*
     * Es folgen Methoden zur Ermittlung, ob der zugrundeliegende Ausdruck eine
     * Instanz einer speziellen Unterklasse von Expression mit speziellem Typ
     * ist.
     */
    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine Summe ist.
     */
    public boolean isSum() {
        return this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.PLUS);
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine Differenz ist.
     */
    public boolean isDifference() {
        return this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.MINUS);
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck ein Produkt ist.
     */
    public boolean isProduct() {
        return this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.TIMES);
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck ein Quotient ist.
     */
    public boolean isQuotient() {
        return this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.DIV);
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine Potenz ist.
     */
    public boolean isPower() {
        return this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.POW);
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine Potenz mit einem ganzzahligen
     * Exponenten ist.
     */
    public boolean isIntegerPower() {
        return this.isPower() && ((BinaryOperation) this).getRight().isIntegerConstant();
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine Potenz mit einem positiven
     * ganzzahligen Exponenten ist.
     */
    public boolean isPositiveIntegerPower() {
        return this.isPower() && ((BinaryOperation) this).getRight().isIntegerConstant()
                && ((Constant) ((BinaryOperation) this).getRight()).getValue().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine Potenz mit einem rationalen
     * Exponenten ist.
     */
    public boolean isRationalPower() {
        return this.isPower() && ((BinaryOperation) this).getRight().isRationalConstant();
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine Potenz mit einem ganzzahligen
     * oder rationalen Exponenten ist.
     */
    public boolean isIntegerPowerOrRationalPower() {
        return this.isPower() && ((BinaryOperation) this).getRight().isIntegerConstantOrRationalConstant();
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine Funktion ist.
     */
    public boolean isFunction() {
        return this instanceof Function;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck eine Funktion vom Typ type ist.
     */
    public boolean isFunction(TypeFunction type) {
        return this instanceof Function && ((Function) this).getType().equals(type);
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck ein Operator ist.
     */
    public boolean isOperator() {
        return this instanceof Operator;
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck ein Operator vom Typ type ist.
     */
    public boolean isOperator(TypeOperator type) {
        return this instanceof Operator && ((Operator) this).getType().equals(type);
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck keine Summe ist.
     */
    public boolean isNotSum() {
        return !(this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.PLUS));
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck keine Differenz ist.
     */
    public boolean isNotDifference() {
        return !(this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.MINUS));
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck kein Produkt ist.
     */
    public boolean isNotProduct() {
        return !(this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.TIMES));
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck kein Quotient ist.
     */
    public boolean isNotQuotient() {
        return !(this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.DIV));
    }

    /**
     * Gibt zur??ck, ob der gegebene Ausdruck keine Potenz ist.
     */
    public boolean isNotPower() {
        return !(this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.POW));
    }

    /**
     * F??hrt triviale Vereinfachungen am gegebenen Ausdruck durch und gibt den
     * vereinfachten Ausdruck zur??ck.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyBasic() throws EvaluationException;

    /**
     * Liefert einen Ausdruck, bei dem f??r alle Variablen ihre zugeh??rigen Werte
     * eingesetzt werden, falls diesen Werte zugeordnet wurden. Die restlichen
     * Variablen werden als Unbestimmte gelassen.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyByInsertingDefinedVars() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei die Vereinfachung gem???? dem
     * Distributivgesetz a*(b + c) = a*b + a*c f??r konstante und rationale
     * Faktoren a erfolgt.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyExpandRationalFactors() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei die Vereinfachung gem???? dem
     * Distributivgesetz und den binomischen Formeln erfolgt. Falls der Exponent
     * bei auftretenden Potenzen zu gro?? ist, wird der zugrundeliegende
     * Teilausdruck nicht weiter vereinfacht.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyExpand(TypeExpansion type) throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei die Vereinfachung gem???? dem
     * Distributivgesetz und den binomischen Formeln erfolgt und falls die
     * Anzahl der resultierenden Summanden klein ist.
     *
     * @throws EvaluationException
     */
    public Expression simplifyExpandShort() throws EvaluationException {
        return this.simplifyExpand(TypeExpansion.SHORT);
    }

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei die Vereinfachung gem???? dem
     * Distributivgesetz und den binomischen Formeln erfolgt und falls die
     * Anzahl der resultierenden Summanden mittelgro?? ist.
     *
     * @throws EvaluationException
     */
    public Expression simplifyExpandModerate() throws EvaluationException {
        return this.simplifyExpand(TypeExpansion.MODERATE);
    }

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei die Vereinfachung gem???? dem
     * Distributivgesetz und den binomischen Formeln erfolgt, selbst wenn die
     * Anzahl der resultierenden Summanden recht gro?? ist.
     *
     * @throws EvaluationException
     */
    public Expression simplifyExpandPowerful() throws EvaluationException {
        return this.simplifyExpand(TypeExpansion.POWERFUL);
    }

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung alle
     * Br??che auf einen Nenner gebracht werden. Der Typ type entscheidet
     * dar??ber, ob die Br??che immer (ALWAYS) oder nur im Falle eines Auftretens
     * von Doppelbr??chen (IF_MULTIPLE_FRACTION_OCCURS) erfolgt.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyBringExpressionToCommonDenominator(TypeFractionSimplification type) throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * relativ 'trickreiche' Methoden zum K??rzen von Differenzen und Quotienten
     * angewendet werden.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyReduceDifferencesAndQuotientsAdvanced() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * Ketten von + und von * nach rechts geordnet werden.
     *
     * @throws EvaluationException
     */
    public abstract Expression orderSumsAndProducts() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * verschachtelte Differenzen bzw. Quotienten zu einer Differenz bzw. einem
     * Quotienten umgewandelt werden.
     *
     * @throws EvaluationException
     */
    public abstract Expression orderDifferencesAndQuotients() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung in
     * Produkten gleiche Faktoren (oder Potenzen mit gleicher Basis) zu einem
     * einzigen Faktor (bzw. Potenz) zusammengefasst werden.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyCollectProducts() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung in
     * Summen oder Differenzen faktorisiert wird.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyFactorize() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung in
     * Summen wie folgt faktorisiert wird: gleiche nichtkonstante Summanden mit
     * verschiedenen rationalen Koeffizienten werden zu einem einzigen Summanden
     * zusammengefasst.<br>
     * BEISPIEL: (1) F??r 3*x*y/7 + 3.8*x*y wird (3/7 + 3.8)*x*y zur??ckgegeben.
     * (2) F??r 2*x*y + 5*x*z wird 2*x*y + 5*x*z zur??ckgegeben (es wird also
     * nichts vereinfacht).
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyFactorizeAllButRationalsInSums() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung in
     * Differenzen wie folgt faktorisiert wird: gleiche nichtkonstante Summanden
     * mit verschiedenen rationalen Koeffizienten werden zu einem einzigen
     * Summanden zusammengefasst.<br>
     * BEISPIEL: (1) F??r 3*x*y/7 - 3.8*x*y wird (3/7 - 3.8)*x*y zur??ckgegeben.
     * (2) F??r 2*x*y - 5*x*z wird 2*x*y - 5*x*z zur??ckgegeben (es wird also
     * nichts vereinfacht).
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyFactorizeAllButRationalsInDifferences() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung in
     * Summen oder Differenzen wie folgt faktorisiert wird: gleiche
     * nichtkonstante Summanden mit verschiedenen rationalen Koeffizienten
     * werden zu einem einzigen Summanden zusammengefasst.<br>
     * BEISPIEL: (1) F??r 3*x*y/7 + 3.8*x*y - 2*x*y wird (3/7 + 3.8 - 2)*x*y
     * zur??ckgegeben. (2) F??r 2*x*y + 5*x*z wird 2*x*y + 5*x*z zur??ckgegeben (es
     * wird also nichts vereinfacht).
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyFactorizeAllButRationals() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung in
     * Quotienten gek??rzt wird.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyReduceQuotients() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung in
     * Produkten oder Quotienten zul??ssige Potenzen auseinandergezogen werden.
     * Eine Potenz ist <i>zul??ssig</i>, wenn sie entweder ganz oder rational mit
     * ungeradem Nenner ist.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyPullApartPowers() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung bei
     * verschachtelten Potenzen stur versucht wird, Exponenten
     * auszumultiplizieren.<br>
     * BEISPIEL: (1) Der Ausdruck (x^3)^5 wird zu x^15 vereinfacht.<br>
     * (2) Der Ausdruck (x^4)^(1/2) wird zu x^2 vereinfacht.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyMultiplyExponents() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung eine
     * Reihe vorgegebener Funktionalgleichungen verwendet wird.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyFunctionalRelations() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * Logarithmen nach M??glichkeit zu einem einzigen Logarithmus
     * zusammengefasst werden.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyCollectLogarithms() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * Logarithmen von Produkten oder Potenzen in Summen oder Vielfache von
     * Logarithmen auseinandergezogen werden.<br>
     * BEISPIEL: Der Ausdruck ln(x*y^3) wird zu ln(x) + 3*ln(y) vereinfacht.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyExpandLogarithms() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * allgemeine Exponentialfunktionen durch die eigentliche Definition ersetzt
     * werden.<br>
     * BEISPIEL: Der Ausdruck 2^x wird zu exp(x*ln(2)) vereinfacht.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyReplaceExponentialFunctionsByDefinitions() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * allgemeine Exponentialfunktionen, deren Basis bzgl var konstant ist,
     * durch die eigentliche Definition ersetzt werden.<br>
     * BEISPIELE: (1) Der Ausdruck 2^x wird bei var = "x" zu exp(x*ln(2))
     * vereinfacht.<br>
     * (2) Der Ausdruck x^x wird bei var = "x" zu x^x vereinfacht (Vereinfachung
     * findet also nicht statt).<br>
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(String var) throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * trigonometrische Funktionen durch die eigentliche Definition ersetzt
     * werden.<br>
     * BEISPIEL: Der Ausdruck tan(x) wird zu sin(x)/cos(x) vereinfacht.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * trigonometrische Funktionen, deren Argumente die Ver??nderliche var
     * enthalten, durch die eigentliche Definition ersetzt werden.<br>
     * BEISPIELE: (1) Der Ausdruck tan(x) wird bei var = "x" zu sin(x)/cos(x)
     * vereinfacht.<br>
     * (2) Der Ausdruck tan(a) wird bei var = "x" zu tan(a) vereinfacht
     * (Vereinfachung findet also nicht statt).<br>
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(String var) throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * Sinus- und Kosinusfunktionen, deren Argumente ganzzahlige Vielfache eines
     * anderen Ausdrucks A sind, durch Polynome in Sinus- und Kosinusfunktionen
     * mit Argument A ersetzt werden. Au??erdem werden Produkte (und Potenzen,
     * wenn der Exponent nicht zu gro?? ist) von Sinus- und Kosinusfunktionen
     * unter Verwendung entsprechender Relationen zu Summen / Differenzen
     * vereinfacht.<br>
     * BEISPIELE: (1) F??r den Ausdruck sin(2*x) wird 2*sin(x)*cos(x)
     * zur??ckgegeben.<br>
     * (2) F??r den Ausdruck sin(x)*cos(2*x) wird zur??ckgegeben sin(3*x)/2 -
     * sin(x)/2.<br>
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyExpandProductsOfComplexExponentialFunctions(String var) throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung eine
     * Reihe algebraischer Relationen verwendet wird.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyAlgebraicExpressions() throws EvaluationException;

    /**
     * Gibt den vereinfachten Ausdruck zur??ck, wobei bei der Vereinfachung
     * Folgendes probiert wird:<br>
     * (1) Ausmultiplizieren aller Klammern und Aufsammeln ??quivalenter
     * Ausdr??cke.<br>
     * (2) Br??che werden auf einen Nenner gebracht und der Z??hler wird
     * weitestgehend vereinfacht.<br>
     * Falls eines dieser Schritte ein 'k??rzeres' Ergebnis produziert (gemessen
     * werden die Ausdr??cke mittels der Methode getLength()), so wird dieses
     * Ergebnis zur??ckgegeben.<br>
     * BEISPIELE: (1) F??r den Ausdruck (1+x)^2-x^2 wird 1+2*x zur??ckgegeben.<br>
     * (2) F??r den Ausdruck 1-1/(1+x) wird x/(1+x) zur??ckgegeben.<br>
     * (3) F??r den Ausdruck (1+x)^2 wird (1+x)^2 zur??ckgegeben.<br>
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyExpandAndCollectEquivalentsIfShorter() throws EvaluationException;

    /**
     * Gibt den Ausdruck zur??ck, welcher durch 'Standardvereinfachung' des
     * gegebenen Ausdrucks entsteht.
     *
     * @throws EvaluationException
     */
    public Expression simplify() throws EvaluationException {

        try {
            Expression expr, exprSimplified = this;
            do {
                expr = exprSimplified.copy();
                exprSimplified = exprSimplified.orderDifferencesAndQuotients();
                Canceller.interruptComputationIfNeeded();
                exprSimplified = exprSimplified.orderSumsAndProducts();
//                System.out.println(exprSimplified.writeExpression());
                Canceller.interruptComputationIfNeeded();
                exprSimplified = exprSimplified.simplifyBasic();
                Canceller.interruptComputationIfNeeded();
                exprSimplified = exprSimplified.simplifyByInsertingDefinedVars();
                Canceller.interruptComputationIfNeeded();
                exprSimplified = exprSimplified.simplifyPullApartPowers();
                Canceller.interruptComputationIfNeeded();
                exprSimplified = exprSimplified.simplifyCollectProducts();
                Canceller.interruptComputationIfNeeded();
                exprSimplified = exprSimplified.simplifyExpandRationalFactors();
                Canceller.interruptComputationIfNeeded();
                exprSimplified = exprSimplified.simplifyFactorize();
                Canceller.interruptComputationIfNeeded();
                exprSimplified = exprSimplified.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
                Canceller.interruptComputationIfNeeded();
                exprSimplified = exprSimplified.simplifyReduceQuotients();
                Canceller.interruptComputationIfNeeded();
                exprSimplified = exprSimplified.simplifyReduceDifferencesAndQuotientsAdvanced();
                Canceller.interruptComputationIfNeeded();
                if (exprSimplified.containsAlgebraicOperation()) {
                    exprSimplified = exprSimplified.simplifyAlgebraicExpressions();
                    Canceller.interruptComputationIfNeeded();
                }
                exprSimplified = exprSimplified.simplifyExpandAndCollectEquivalentsIfShorter();
                Canceller.interruptComputationIfNeeded();
                if (exprSimplified.containsFunction() || exprSimplified.containsOperator(TypeOperator.fac)) {
                    exprSimplified = exprSimplified.simplifyFunctionalRelations();
                    Canceller.interruptComputationIfNeeded();
                    exprSimplified = exprSimplified.simplifyCollectLogarithms();
                    Canceller.interruptComputationIfNeeded();
                }
            } while (!expr.equals(exprSimplified));
            return exprSimplified;
        } catch (java.lang.StackOverflowError e) {
            throw new EvaluationException(Translator.translateOutputMessage(EB_Expression_STACK_OVERFLOW));
        }

    }

    /**
     * Gibt den Ausdruck zur??ck, welcher durch die mittels simplifyTypes
     * definierten Vereinfachung des gegebenen Ausdrucks entsteht.
     *
     * @throws EvaluationException
     */
    public Expression simplify(TypeSimplify... simplifyTypes) throws EvaluationException {

        try {
            Expression expr, exprSimplified = this;
            do {

                expr = exprSimplified.copy();
                for (TypeSimplify simplifyType : simplifyTypes) {
                    if (simplifyType.equals(TypeSimplify.order_difference_and_division)) {
                        exprSimplified = exprSimplified.orderDifferencesAndQuotients();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.order_sums_and_products)) {
                        exprSimplified = exprSimplified.orderSumsAndProducts();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_basic)) {
                        exprSimplified = exprSimplified.simplifyBasic();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_by_inserting_defined_vars)) {
                        exprSimplified = exprSimplified.simplifyByInsertingDefinedVars();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_expand_short)) {
                        exprSimplified = exprSimplified.simplifyExpandShort();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_expand_moderate)) {
                        exprSimplified = exprSimplified.simplifyExpandModerate();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_expand_powerful)) {
                        exprSimplified = exprSimplified.simplifyExpandPowerful();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_expand_rational_factors)) {
                        exprSimplified = exprSimplified.simplifyExpandRationalFactors();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_pull_apart_powers)) {
                        exprSimplified = exprSimplified.simplifyPullApartPowers();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_multiply_exponents)) {
                        exprSimplified = exprSimplified.simplifyMultiplyExponents();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_collect_products)) {
                        exprSimplified = exprSimplified.simplifyCollectProducts();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_factorize_all_but_rationals)) {
                        exprSimplified = exprSimplified.simplifyFactorizeAllButRationals();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_factorize)) {
                        exprSimplified = exprSimplified.simplifyFactorize();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_bring_expression_to_common_denominator)) {
                        exprSimplified = exprSimplified.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_reduce_quotients)) {
                        exprSimplified = exprSimplified.simplifyReduceQuotients();
                        Canceller.interruptComputationIfNeeded();
                    } else if (simplifyType.equals(TypeSimplify.simplify_reduce_differences_and_quotients_advanced)) {
                        exprSimplified = exprSimplified.simplifyReduceDifferencesAndQuotientsAdvanced();
                        Canceller.interruptComputationIfNeeded();
                    } else if (exprSimplified.containsAlgebraicOperation()) {
                        if (simplifyType.equals(TypeSimplify.simplify_algebraic_expressions)) {
                            exprSimplified = exprSimplified.simplifyAlgebraicExpressions();
                            Canceller.interruptComputationIfNeeded();
                        }
                    } else if (simplifyType.equals(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter)) {
                        exprSimplified = exprSimplified.simplifyExpandAndCollectEquivalentsIfShorter();
                        Canceller.interruptComputationIfNeeded();
                    } else if (exprSimplified.containsFunction() || exprSimplified.containsOperator(TypeOperator.fac)) {
                        if (simplifyType.equals(TypeSimplify.simplify_functional_relations)) {
                            exprSimplified = exprSimplified.simplifyFunctionalRelations();
                            Canceller.interruptComputationIfNeeded();
                        } else if (simplifyType.equals(TypeSimplify.simplify_replace_exponential_functions_by_definitions)) {
                            exprSimplified = exprSimplified.simplifyReplaceExponentialFunctionsByDefinitions();
                            Canceller.interruptComputationIfNeeded();
                        } else if (simplifyType.equals(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions)) {
                            exprSimplified = exprSimplified.simplifyReplaceTrigonometricalFunctionsByDefinitions();
                            Canceller.interruptComputationIfNeeded();
                        } else if (simplifyType.equals(TypeSimplify.simplify_collect_logarithms)) {
                            exprSimplified = exprSimplified.simplifyCollectLogarithms();
                            Canceller.interruptComputationIfNeeded();
                        } else if (simplifyType.equals(TypeSimplify.simplify_expand_logarithms)) {
                            exprSimplified = exprSimplified.simplifyExpandLogarithms();
                            Canceller.interruptComputationIfNeeded();
                        }
                    }
                }

            } while (!expr.equals(exprSimplified));
            return exprSimplified;
        } catch (java.lang.StackOverflowError e) {
            throw new EvaluationException(Translator.translateOutputMessage(EB_Expression_STACK_OVERFLOW));
        }

    }

    /**
     * Gibt den Ausdruck zur??ck, welcher durch die mittels simplifyTypes
     * definierten Vereinfachung des gegebenen Ausdrucks entsteht. Das Argument
     * var wird hier nur f??r die Methoden
     * simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions()
     * und
     * simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions()
     * verwendet, da diese var als Eingabe ben??tigen.
     *
     * @throws EvaluationException
     */
    public Expression simplify(HashSet<TypeSimplify> simplifyTypes, String var) throws EvaluationException {

        try {
            Expression expr, exprSimplified = this;
            do {
                expr = exprSimplified.copy();
                if (simplifyTypes.contains(TypeSimplify.order_difference_and_division)) {
                    exprSimplified = exprSimplified.orderDifferencesAndQuotients();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.order_sums_and_products)) {
                    exprSimplified = exprSimplified.orderSumsAndProducts();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_basic)) {
                    exprSimplified = exprSimplified.simplifyBasic();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_by_inserting_defined_vars)) {
                    exprSimplified = exprSimplified.simplifyByInsertingDefinedVars();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_short)) {
                    exprSimplified = exprSimplified.simplifyExpandShort();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_moderate)) {
                    exprSimplified = exprSimplified.simplifyExpandModerate();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_powerful)) {
                    exprSimplified = exprSimplified.simplifyExpandPowerful();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_rational_factors)) {
                    exprSimplified = exprSimplified.simplifyExpandRationalFactors();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_pull_apart_powers)) {
                    exprSimplified = exprSimplified.simplifyPullApartPowers();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_multiply_exponents)) {
                    exprSimplified = exprSimplified.simplifyMultiplyExponents();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_collect_products)) {
                    exprSimplified = exprSimplified.simplifyCollectProducts();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_factorize_all_but_rationals)) {
                    exprSimplified = exprSimplified.simplifyFactorizeAllButRationals();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_factorize)) {
                    exprSimplified = exprSimplified.simplifyFactorize();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_bring_expression_to_common_denominator)) {
                    exprSimplified = exprSimplified.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_reduce_quotients)) {
                    exprSimplified = exprSimplified.simplifyReduceQuotients();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_reduce_differences_and_quotients_advanced)) {
                    exprSimplified = exprSimplified.simplifyReduceDifferencesAndQuotientsAdvanced();
                    Canceller.interruptComputationIfNeeded();
                }
                if (exprSimplified.containsAlgebraicOperation()) {
                    if (simplifyTypes.contains(TypeSimplify.simplify_algebraic_expressions)) {
                        exprSimplified = exprSimplified.simplifyAlgebraicExpressions();
                        Canceller.interruptComputationIfNeeded();
                    }
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter)) {
                    exprSimplified = exprSimplified.simplifyExpandAndCollectEquivalentsIfShorter();
                    Canceller.interruptComputationIfNeeded();
                }
                if (exprSimplified.containsFunction() || exprSimplified.containsOperator(TypeOperator.fac)) {
                    if (simplifyTypes.contains(TypeSimplify.simplify_functional_relations)) {
                        exprSimplified = exprSimplified.simplifyFunctionalRelations();
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_replace_exponential_functions_by_definitions)) {
                        exprSimplified = exprSimplified.simplifyReplaceExponentialFunctionsByDefinitions();
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_replace_exponential_functions_with_respect_to_variable_by_definitions)) {
                        exprSimplified = exprSimplified.simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var);
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions)) {
                        exprSimplified = exprSimplified.simplifyReplaceTrigonometricalFunctionsByDefinitions();
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_replace_trigonometrical_functions_with_respect_to_variable_by_definitions)) {
                        exprSimplified = exprSimplified.simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var);
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_collect_logarithms)) {
                        exprSimplified = exprSimplified.simplifyCollectLogarithms();
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_expand_logarithms)) {
                        exprSimplified = exprSimplified.simplifyExpandLogarithms();
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_expand_products_of_complex_exponential_functions)) {
                        exprSimplified = exprSimplified.simplifyExpandProductsOfComplexExponentialFunctions(var);
                        Canceller.interruptComputationIfNeeded();
                    }
                }
            } while (!expr.equals(exprSimplified));
            return exprSimplified;
        } catch (java.lang.StackOverflowError e) {
            throw new EvaluationException(Translator.translateOutputMessage(EB_Expression_STACK_OVERFLOW));
        }

    }

    /**
     * Gibt den Ausdruck zur??ck, welcher durch die mittels simplifyTypes
     * definierten Vereinfachung des gegebenen Ausdrucks entsteht.
     *
     * @throws EvaluationException
     */
    public Expression simplify(HashSet<TypeSimplify> simplifyTypes) throws EvaluationException {

        try {
            Expression expr, exprSimplified = this;
            do {
                expr = exprSimplified.copy();
                if (simplifyTypes.contains(TypeSimplify.order_difference_and_division)) {
                    exprSimplified = exprSimplified.orderDifferencesAndQuotients();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.order_sums_and_products)) {
                    exprSimplified = exprSimplified.orderSumsAndProducts();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_basic)) {
                    exprSimplified = exprSimplified.simplifyBasic();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_by_inserting_defined_vars)) {
                    exprSimplified = exprSimplified.simplifyByInsertingDefinedVars();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_short)) {
                    exprSimplified = exprSimplified.simplifyExpandShort();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_moderate)) {
                    exprSimplified = exprSimplified.simplifyExpandModerate();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_powerful)) {
                    exprSimplified = exprSimplified.simplifyExpandPowerful();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_rational_factors)) {
                    exprSimplified = exprSimplified.simplifyExpandRationalFactors();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_pull_apart_powers)) {
                    exprSimplified = exprSimplified.simplifyPullApartPowers();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_multiply_exponents)) {
                    exprSimplified = exprSimplified.simplifyMultiplyExponents();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_collect_products)) {
                    exprSimplified = exprSimplified.simplifyCollectProducts();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_factorize_all_but_rationals)) {
                    exprSimplified = exprSimplified.simplifyFactorizeAllButRationals();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_factorize)) {
                    exprSimplified = exprSimplified.simplifyFactorize();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_bring_expression_to_common_denominator)) {
                    exprSimplified = exprSimplified.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_reduce_quotients)) {
                    exprSimplified = exprSimplified.simplifyReduceQuotients();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_reduce_differences_and_quotients_advanced)) {
                    exprSimplified = exprSimplified.simplifyReduceDifferencesAndQuotientsAdvanced();
                    Canceller.interruptComputationIfNeeded();
                }
                if (exprSimplified.containsAlgebraicOperation()) {
                    if (simplifyTypes.contains(TypeSimplify.simplify_algebraic_expressions)) {
                        exprSimplified = exprSimplified.simplifyAlgebraicExpressions();
                        Canceller.interruptComputationIfNeeded();
                    }
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter)) {
                    exprSimplified = exprSimplified.simplifyExpandAndCollectEquivalentsIfShorter();
                    Canceller.interruptComputationIfNeeded();
                }
                if (exprSimplified.containsFunction() || exprSimplified.containsOperator(TypeOperator.fac)) {
                    if (simplifyTypes.contains(TypeSimplify.simplify_functional_relations)) {
                        exprSimplified = exprSimplified.simplifyFunctionalRelations();
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_replace_exponential_functions_by_definitions)) {
                        exprSimplified = exprSimplified.simplifyReplaceExponentialFunctionsByDefinitions();
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions)) {
                        exprSimplified = exprSimplified.simplifyReplaceTrigonometricalFunctionsByDefinitions();
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_collect_logarithms)) {
                        exprSimplified = exprSimplified.simplifyCollectLogarithms();
                        Canceller.interruptComputationIfNeeded();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_expand_logarithms)) {
                        exprSimplified = exprSimplified.simplifyExpandLogarithms();
                        Canceller.interruptComputationIfNeeded();
                    }
                }
            } while (!expr.equals(exprSimplified));
            return exprSimplified;
        } catch (java.lang.StackOverflowError e) {
            throw new EvaluationException(Translator.translateOutputMessage(EB_Expression_STACK_OVERFLOW));
        }

    }

}
