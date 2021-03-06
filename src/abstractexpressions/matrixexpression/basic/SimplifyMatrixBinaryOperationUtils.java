package abstractexpressions.matrixexpression.basic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import java.awt.Dimension;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixBinaryOperation;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixPower;
import abstractexpressions.matrixexpression.computation.EigenvaluesEigenvectorsUtils;

public abstract class SimplifyMatrixBinaryOperationUtils {

    public static void removeZeroMatrixInSum(MatrixExpressionCollection summands) throws EvaluationException {
        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) == null) {
                continue;
            }
            if (summands.get(i).isZeroMatrix() && !summands.isEmpty()) {
                summands.remove(i);
            }
        }
    }

    public static MatrixExpression trivialOperationsInDifferenceWithZeroIdMatrices(MatrixExpression matExpr) {

        if (matExpr.isDifference()) {
            if (((MatrixBinaryOperation) matExpr).getRight().isZeroMatrix()) {
                return ((MatrixBinaryOperation) matExpr).getLeft();
            }
            if (((MatrixBinaryOperation) matExpr).getLeft().isZeroMatrix()) {
                return new Matrix(Expression.MINUS_ONE).mult(((MatrixBinaryOperation) matExpr).getRight());
            }
        }

        return matExpr;

    }

    public static MatrixExpression factorizeMultiplesOfId(MatrixExpression matExpr) {

        if (matExpr.isNotProduct()) {
            return matExpr;
        }

        MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(matExpr);
        MatrixExpressionCollection resultFactors = new MatrixExpressionCollection();

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isMultipleOfId()) {
                resultFactors.add(new Matrix(((Matrix) factors.get(i)).getEntry(0, 0)));
                factors.remove(i);
            }
        }

        if (resultFactors.isEmpty()) {
            return matExpr;
        }

        for (int i = 0; i < factors.getBound(); i++) {
            resultFactors.add(factors.get(i));
        }
        return SimplifyMatrixUtilities.produceProduct(resultFactors);

    }

    public static void removeIdInProduct(MatrixExpressionCollection factors) throws EvaluationException {
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isId() && !factors.isEmpty()) {
                factors.remove(i);
            }
        }
    }

    public static void reduceZeroProductToZero(MatrixExpressionCollection factors) throws EvaluationException {
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isZeroMatrix()) {
                Dimension dim = SimplifyMatrixUtilities.produceProduct(factors).getDimension();
                factors.clear();
                factors.add(MatrixExpression.getZeroMatrix(dim.height, dim.width));
            }
        }
    }

    public static void factorizeScalarsInSum(MatrixExpressionCollection summands) throws EvaluationException {

        MatrixExpressionCollection factorsOfLeftSummand, factorsOfRightSummand, commonScalarFactors;
        MatrixExpression commonFactor, restSummandLeft, restSummandRight;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            factorsOfLeftSummand = SimplifyMatrixUtilities.getFactors(summands.get(i));

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }

                factorsOfRightSummand = SimplifyMatrixUtilities.getFactors(summands.get(j));

                commonScalarFactors = SimplifyMatrixUtilities.intersection(factorsOfLeftSummand, factorsOfRightSummand);

                // Nun m??ssen unter den gemeinsamen Faktoren diejenigen ausgew??hlt werden, welche 1x1-Matrizen darstellen.
                for (int k = 0; k < commonScalarFactors.getBound(); k++) {
                    if (!(commonScalarFactors.get(k).convertOneTimesOneMatrixToExpression() instanceof Expression)) {
                        commonScalarFactors.remove(k);
                    }
                }

                // Summanden faktorisieren, wenn gemeinsame Skalarfaktoren vorhanden sind.
                if (!commonScalarFactors.isEmpty()) {
                    commonFactor = SimplifyMatrixUtilities.produceProduct(commonScalarFactors);
                    restSummandLeft = SimplifyMatrixUtilities.produceProduct(SimplifyMatrixUtilities.difference(factorsOfLeftSummand, commonScalarFactors));
                    restSummandRight = SimplifyMatrixUtilities.produceProduct(SimplifyMatrixUtilities.difference(factorsOfRightSummand, commonScalarFactors));
                    summands.put(i, commonFactor.mult(restSummandLeft.add(restSummandRight)));
                    summands.remove(j);
                    break;
                }

            }

        }

    }

    public static void factorizeScalarsInDifference(MatrixExpressionCollection summandsLeft, MatrixExpressionCollection summandsRight) throws EvaluationException {

        MatrixExpressionCollection factorsOfLeftSummand, factorsOfRightSummand, commonScalarFactors;
        MatrixExpression commonFactor, restSummandLeft, restSummandRight;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }

            factorsOfLeftSummand = SimplifyMatrixUtilities.getFactors(summandsLeft.get(i));

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }

                factorsOfRightSummand = SimplifyMatrixUtilities.getFactors(summandsRight.get(j));

                commonScalarFactors = SimplifyMatrixUtilities.intersection(factorsOfLeftSummand, factorsOfRightSummand);

                // Nun m??ssen unter den gemeinsamen Faktoren diejenigen ausgew??hlt werden, welche 1x1-Matrizen darstellen.
                for (int k = 0; k < commonScalarFactors.getBound(); k++) {
                    if (!(commonScalarFactors.get(k).convertOneTimesOneMatrixToExpression() instanceof Expression)) {
                        commonScalarFactors.remove(k);
                    }
                }

                // Summanden faktorisieren, wenn gemeinsame Skalarfaktoren vorhanden sind.
                if (!commonScalarFactors.isEmpty()) {
                    commonFactor = SimplifyMatrixUtilities.produceProduct(commonScalarFactors);
                    restSummandLeft = SimplifyMatrixUtilities.produceProduct(SimplifyMatrixUtilities.difference(factorsOfLeftSummand, commonScalarFactors));
                    restSummandRight = SimplifyMatrixUtilities.produceProduct(SimplifyMatrixUtilities.difference(factorsOfRightSummand, commonScalarFactors));
                    summandsLeft.put(i, commonFactor.mult(restSummandLeft.sub(restSummandRight)));
                    summandsRight.remove(j);
                    break;
                }

            }

        }

    }

    public static void factorizeInSum(MatrixExpressionCollection summands) throws EvaluationException {

        MatrixExpressionCollection factorsOfLeftSummand, factorsOfRightSummand;
        MatrixExpression commonFactor, factorizedSummand;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            factorsOfLeftSummand = SimplifyMatrixUtilities.getFactors(summands.get(i));

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }

                factorsOfRightSummand = SimplifyMatrixUtilities.getFactors(summands.get(j));

                factorizedSummand = null;
                // Jetzt wird der erste nichtskalare Faktor im linken und im rechten Summanden gesucht.
                for (int p = 0; p < factorsOfLeftSummand.getBound(); p++) {
                    if (factorsOfLeftSummand.get(p).convertOneTimesOneMatrixToExpression() instanceof MatrixExpression) {
                        for (int q = 0; q < factorsOfRightSummand.getBound(); q++) {
                            if (factorsOfRightSummand.get(q).convertOneTimesOneMatrixToExpression() instanceof MatrixExpression) {
                                if (factorsOfLeftSummand.get(p).equivalent(factorsOfRightSummand.get(q))) {
                                    commonFactor = factorsOfLeftSummand.get(p);
                                    factorsOfLeftSummand.remove(p);
                                    factorsOfRightSummand.remove(q);
                                    factorizedSummand = commonFactor.mult(SimplifyMatrixUtilities.produceProduct(factorsOfLeftSummand).add(
                                            SimplifyMatrixUtilities.produceProduct(factorsOfRightSummand)));
                                }
                                break;
                            }
                        }
                        break;
                    }
                }

                // Faktorisierten Summanden ablegen, falls solch einer existiert.
                if (factorizedSummand != null) {
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    break;
                }

            }

        }

    }

    public static void factorizeInDifference(MatrixExpressionCollection summandsLeft, MatrixExpressionCollection summandsRight) throws EvaluationException {

        MatrixExpressionCollection factorsOfLeftSummand, factorsOfRightSummand;
        MatrixExpression commonFactor, factorizedSummand;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }

            factorsOfLeftSummand = SimplifyMatrixUtilities.getFactors(summandsLeft.get(i));

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }

                factorsOfRightSummand = SimplifyMatrixUtilities.getFactors(summandsRight.get(j));

                factorizedSummand = null;
                // Jetzt wird der erste nichtskalare Faktor im linken und im rechten Summanden gesucht.
                for (int p = 0; p < factorsOfLeftSummand.getBound(); p++) {
                    if (factorsOfLeftSummand.get(p).convertOneTimesOneMatrixToExpression() instanceof MatrixExpression) {
                        for (int q = 0; q < factorsOfRightSummand.getBound(); q++) {
                            if (factorsOfRightSummand.get(q).convertOneTimesOneMatrixToExpression() instanceof MatrixExpression) {
                                if (factorsOfLeftSummand.get(p).equivalent(factorsOfRightSummand.get(q))) {
                                    commonFactor = factorsOfLeftSummand.get(p);
                                    factorsOfLeftSummand.remove(p);
                                    factorsOfRightSummand.remove(q);
                                    factorizedSummand = commonFactor.mult(SimplifyMatrixUtilities.produceProduct(factorsOfLeftSummand).sub(
                                            SimplifyMatrixUtilities.produceProduct(factorsOfRightSummand)));
                                }
                                break;
                            }
                        }
                        break;
                    }
                }

                // Faktorisierten Summanden ablegen, falls solch einer existiert.
                if (factorizedSummand != null) {
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    break;
                }

            }

        }

    }

    /*
     * Hilfsprozeduren f??r das Zusammenfassen von Konstanten.
     */
    /**
     * Sammelt bei Addition Matrizen in summands im 1. Summanden.
     *
     * @throws EvaluationException
     */
    public static void collectMatricesInSum(MatrixExpressionCollection summands) throws EvaluationException {

        Dimension dim = new Dimension(1, 1);
        if (!summands.isEmpty()) {
            for (MatrixExpression summand : summands) {
                dim = summand.getDimension();
                break;
            }
        }

        MatrixExpression matrixSummand = MatrixExpression.getZeroMatrix(dim.height, dim.width);

        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) == null) {
                continue;
            }
            if (summands.get(i).isMatrix()) {
                matrixSummand = matrixSummand.add(summands.get(i)).simplifyComputeMatrixOperations();
                summands.remove(i);
            }
        }

        if (!matrixSummand.isZeroMatrix()) {
            summands.insert(0, matrixSummand);
        }

    }

    /**
     * Sammelt in einer Differenz Matrizen im 1. Summanden in summandsLeft.
     *
     * @throws EvaluationException
     */
    public static void collectMatricesInDifference(MatrixExpressionCollection summandsLeft, MatrixExpressionCollection summandsRight) throws EvaluationException {

        MatrixExpressionCollection result = new MatrixExpressionCollection();

        Dimension dim = new Dimension(1, 1);
        if (!summandsLeft.isEmpty()) {
            for (MatrixExpression summand : summandsLeft) {
                dim = summand.getDimension();
                break;
            }
        } else if (!summandsRight.isEmpty()) {
            for (MatrixExpression summand : summandsRight) {
                dim = summand.getDimension();
                break;
            }
        }

        MatrixExpression matrixSummand = MatrixExpression.getZeroMatrix(dim.height, dim.width);

        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if (summandsLeft.get(i) == null) {
                continue;
            }
            if (summandsLeft.get(i).isMatrix()) {
                matrixSummand = matrixSummand.add(summandsLeft.get(i)).simplifyComputeMatrixOperations();
                summandsLeft.remove(i);
            }
        }
        for (int i = 0; i < summandsRight.getBound(); i++) {
            if (summandsRight.get(i) == null) {
                continue;
            }
            if (summandsRight.get(i).isMatrix()) {
                matrixSummand = matrixSummand.sub(summandsRight.get(i)).simplifyComputeMatrixOperations();
                summandsRight.remove(i);
            }
        }

        if (!matrixSummand.isZeroMatrix()) {
            summandsLeft.insert(0, matrixSummand);
        }

    }

    /**
     * Sammelt Matrizen im Produkt.
     *
     * @throws EvaluationException
     */
    public static void collectMatricesInProduct(MatrixExpressionCollection factors) throws EvaluationException {

        /*
         Es werden immer nur ZWEI aufeinanderfolgende Matrizen ausmultipliziert.
         GRUND: Dieses Produkt muss im Zuge von simplify() zun??chst m??glichst weit
         vereinfacht werden, damit (schnell) weitermultipliziert werden kann. 
         Sonst wird es zu rechenlastig.
         */
        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isMatrix()) {
                for (int j = i + 1; j < factors.getBound(); j++) {
                    if (factors.get(j) == null) {
                        continue;
                    }
                    if (factors.get(j).isMatrix()) {
                        factors.put(i, factors.get(i).mult(factors.get(j)).simplifyComputeMatrixOperations());
                        factors.remove(j);
                        break;
                    } else {
                        break;
                    }

                }
            }

        }

    }

    public static MatrixExpression simplifyPowerOfDiagonalizableMatrix(Matrix m, Expression exp) {

        if (EigenvaluesEigenvectorsUtils.isMatrixDiagonalizable(m)) {

            Object eigenvectorMatrix = EigenvaluesEigenvectorsUtils.getEigenvectorBasisMatrix(m);
            if (eigenvectorMatrix instanceof Matrix) {
                try {
                    MatrixExpression matrixInDiagonalForm = ((Matrix) eigenvectorMatrix).pow(-1).mult(m).mult((Matrix) eigenvectorMatrix).simplify();
                    if (matrixInDiagonalForm instanceof Matrix && ((Matrix) matrixInDiagonalForm).isDiagonalMatrix()) {
                        // Das Folgende kann dann direkt explizit berechnet werden.
                        return ((Matrix) eigenvectorMatrix).mult(new MatrixPower(((Matrix) matrixInDiagonalForm), exp)).mult(((Matrix) eigenvectorMatrix).pow(-1));
                    }
                } catch (EvaluationException e) {
                    return new MatrixPower(m, exp);
                }
            }

        }

        return new MatrixPower(m, exp);

    }

}
