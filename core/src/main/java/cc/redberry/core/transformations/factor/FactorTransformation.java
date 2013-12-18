/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.transformations.factor;

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.tensor.iterator.FromParentToChildIterator;
import cc.redberry.core.tensor.iterator.TreeIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.fractions.TogetherTransformation;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.LocalSymbolsProvider;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Factors over the integers all scalar sub-tensors appearing at the top level of expression tree. The default
 * implementation is based on Heinz Kredel Java Algebra System (http://krum.rz.uni-mannheim.de/jas/).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1
 */
public class FactorTransformation implements Transformation {
    /**
     * Singleton instance.
     */
    public static final FactorTransformation FACTOR = new FactorTransformation(true, JasFactor.ENGINE);
    private final boolean factorScalars;
    private final FactorizationEngine factorizationEngine;

    /**
     * @param factorScalars       specifies whether scalar but not symbolic (i.e. scalar indexed expressions) should be
     *                            factorized on a par with symbolic (i.e. without any indices) expressions
     * @param factorizationEngine custom factorization engine
     */
    public FactorTransformation(boolean factorScalars, FactorizationEngine factorizationEngine) {
        this.factorScalars = factorScalars;
        this.factorizationEngine = factorizationEngine;
    }


    @Override
    public Tensor transform(Tensor t) {
        return factor(t, factorScalars, factorizationEngine);
    }


    /**
     * Factors scalar parts of tensor over the integers.
     *
     * @param tensor              tensor
     * @param factorScalars       if false, then only symbolic (without any indices) sub-tensors will be factorized
     * @param factorizationEngine factorization engine
     * @return
     */
    public static Tensor factor(Tensor tensor, boolean factorScalars, FactorizationEngine factorizationEngine) {
        if (factorScalars) {
            Expression[] replacementsOfScalars = TensorUtils.generateReplacementsOfScalars(tensor,
                    new LocalSymbolsProvider(tensor, "sclr"));
            for (Expression e : replacementsOfScalars)
                tensor = e.transform(tensor);
            tensor = factorSymbolicTerms(tensor, factorizationEngine);
            for (Expression e : replacementsOfScalars)
                tensor = e.transpose().transform(tensor);
            return tensor;
        } else
            return factorSymbolicTerms(tensor, factorizationEngine);
    }

    /**
     * Factors scalar parts of tensor over the integers. The
     * implementation is based on Heinz Kredel Java Algebra System (http://krum.rz.uni-mannheim.de/jas/).
     *
     * @param tensor        tensor
     * @param factorScalars if false, then only symbolic (without any indices) sub-tensors will be factorized
     * @return
     */
    public static Tensor factor(Tensor tensor, boolean factorScalars) {
        return factor(tensor, factorScalars, JasFactor.ENGINE);
    }

    /**
     * Factors scalar of tensor over the integers. The
     * implementation is based on Heinz Kredel Java Algebra System (http://krum.rz.uni-mannheim.de/jas/).
     *
     * @param tensor tensor
     * @return result
     */
    public static Tensor factor(Tensor tensor) {
        return factor(tensor, true, JasFactor.ENGINE);
    }

    private static Tensor factorSymbolicTerms(Tensor tensor, FactorizationEngine factorizationEngine) {
        FromParentToChildIterator iterator = new FromParentToChildIterator(tensor);
        Tensor c;
        while ((c = iterator.next()) != null) {
            if (!(c instanceof Sum))
                continue;
            Tensor remainder = c, temp;
            IntArrayList symbolicPositions = new IntArrayList();
            for (int i = c.size() - 1; i >= 0; --i) {
                temp = c.get(i);
                if (isSymbolic(temp)) {
                    symbolicPositions.add(i);
                    if (remainder instanceof Sum)
                        remainder = ((Sum) remainder).remove(i);
                    else remainder = Complex.ZERO;
                }
            }
            Tensor symbolicPart = ((Sum) c).select(symbolicPositions.toArray());
            symbolicPart = factorSymbolicTerm(symbolicPart, factorizationEngine);
            if (remainder instanceof Sum) {
                SumBuilder sb = new SumBuilder(remainder.size());
                for (Tensor tt : remainder)
                    sb.put(factorSymbolicTerms(tt, factorizationEngine));
                remainder = sb.build();
            } else
                remainder = factorSymbolicTerms(remainder, factorizationEngine);
            iterator.set(Tensors.sum(symbolicPart, remainder));
        }
        return iterator.result();
    }

    private static Tensor factorSymbolicTerm(Tensor sum, FactorizationEngine factorizationEngine) {
        TreeIterator iterator = new FromChildToParentIterator(sum);
        Tensor c;
        while ((c = iterator.next()) != null)
            if (c instanceof Sum)
                iterator.set(factorOut(c, factorizationEngine));

        iterator = new FromParentToChildIterator(iterator.result());
        while ((c = iterator.next()) != null) {
            if (!(c instanceof Sum))
                continue;
            if (needTogether(c)) {
                c = TogetherTransformation.together(c, true);
                if (c instanceof Product) {
                    TensorBuilder pb = null;
                    for (int i = c.size() - 1; i >= 0; --i) {
                        if (c.get(i) instanceof Sum) {
                            if (pb == null) {
                                pb = c.getBuilder();
                                for (int j = c.size() - 1; j > i; --j)
                                    pb.put(c.get(j));
                            }
                            pb.put(factorSum1(c.get(i), factorizationEngine));
                        } else if (pb != null)
                            pb.put(c.get(i));
                    }
                    iterator.set(pb == null ? c : pb.build());
                } else iterator.set(c);
            } else
                iterator.set(factorSum1(c, factorizationEngine));
        }
        return iterator.result();
    }

    private static Tensor factorSum1(Tensor sum, FactorizationEngine engine) {
        Tensor[] parts = reIm(sum);
        if (!TensorUtils.isZero(parts[0])) {
            Tensor im = parts[0];
            if (im instanceof Sum)
                im = FastTensors.multiplySumElementsOnFactor((Sum) im, Complex.IMAGINARY_UNIT);
            else
                im = Tensors.multiply(im, Complex.IMAGINARY_UNIT);
            im = engine.factor(im);
            im = Tensors.multiply(im, Complex.NEGATIVE_IMAGINARY_UNIT);
            parts[0] = im;
        }

        if (!TensorUtils.isZero(parts[1]))
            parts[1] = engine.factor(parts[1]);


        return Tensors.sum(parts[0], parts[1]);
    }

    private static Tensor[] reIm(Tensor sum) {
        IntArrayList im = new IntArrayList(sum.size());
        for (int i = sum.size() - 1; i >= 0; --i) {
            if (sum.get(i) instanceof Complex && !((Complex) sum.get(i)).getImaginary().isZero())
                im.add(i);
            else if (sum.get(i) instanceof Product && !((Product) sum.get(i)).getFactor().getImaginary().isZero())
                im.add(i);
        }
        Tensor[] parts = new Tensor[2];
        int[] positions = im.toArray();
        parts[0] = ((Sum) sum).select(positions);
        parts[1] = ((Sum) sum).remove(positions);
        return parts;
    }

    private static boolean needTogether(Tensor t) {
        if (t instanceof Power) {
            if (needTogether(t.get(0)))
                return true;
            return ((Complex) t.get(1)).getReal().signum() < 0;
        }
        if (t instanceof SimpleTensor)
            return false;
        for (Tensor tt : t)
            if (needTogether(tt))
                return true;
        return false;
    }

    private static boolean isSymbolic(Tensor t) {
        if (t.getIndices().size() != 0 || t instanceof ScalarFunction)
            return false;
        if (t instanceof SimpleTensor)
            return t.size() == 0;//not a field
        if (t instanceof Power) {
            if (!isSymbolic(t.get(0)))
                return false;
            if (!TensorUtils.isInteger(t.get(1)))
                return false;
            Complex e = (Complex) t.get(1);
            if (!e.isReal() || e.isNumeric())
                return false;
            return true;
        }
        for (Tensor tt : t)
            if (!isSymbolic(tt))
                return false;
        return true;
    }

    static Tensor factorOut(Tensor tensor) {
        return factorOut(tensor, JasFactor.ENGINE);
    }

    static Tensor factorOut(Tensor tensor, FactorizationEngine factorizationEngine) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(tensor);
        Tensor c;
        while ((c = iterator.next()) != null)
            if (c instanceof Sum)
                iterator.set(factorOut1(c, factorizationEngine));
        return iterator.result();
    }

    private static boolean isProductOfSums(Tensor tensor) {
        if (tensor instanceof Sum)
            return false;
        if (tensor instanceof Product)
            if (tensor instanceof Product)
                for (Tensor t : tensor)
                    if (isIntegerPowerOfSum(t))
                        return true;
        return isIntegerPowerOfSum(tensor);
    }

    private static boolean isIntegerPowerOfSum(Tensor tensor) {
        if (tensor instanceof Sum)
            return true;
        return tensor instanceof Power && tensor.get(0) instanceof Sum
                && TensorUtils.isInteger(tensor.get(1));
    }

    static Tensor factorOut1(Tensor tensor, FactorizationEngine factorizationEngine) {
        /*
         * S0: factor out imaginary numbers
         * I*a + I*b
         */

        //check
        Boolean factorOutImageOne = null;
        boolean containsImageOne;
        for (Tensor t : tensor) {
            if (t instanceof Product)
                containsImageOne = ((Product) t).getFactor().isImaginary();
            else if (t instanceof Complex)
                containsImageOne = ((Complex) t).isImaginary();
            else containsImageOne = false;

            if (factorOutImageOne == null)
                factorOutImageOne = containsImageOne;
            else if (factorOutImageOne != containsImageOne)
                factorOutImageOne = false;
        }

        if (factorOutImageOne)
            tensor = FastTensors.multiplySumElementsOnFactor((Sum) tensor, Complex.NEGATIVE_IMAGINARY_UNIT);

        if (!(tensor instanceof Sum)) {
            if (factorOutImageOne)
                tensor = Tensors.multiply(Complex.IMAGINARY_UNIT, tensor);
            return factorOut(tensor, factorizationEngine);
        }

        /*
         * S1:
         * (a+b)*c + a*d + b*d -> (a+b)*c + (a+b)*d
         */
        Tensor temp = tensor;
        int i, j = temp.size();
        IntArrayList nonProductOfSumsPositions = new IntArrayList();
        for (i = 0; i < j; ++i)
            if (!isProductOfSums(temp.get(i)))
                nonProductOfSumsPositions.add(i);
//        if (nonProductOfSumsPositions.size() == tensor.size())
//            //when tensor = a*d + b*d + ... (no sums in products)
//            return JasFactor.factor(tensor);

         /*
         * S2:
         * finding product of sums in tensor with minimal number of multipliers
         * we call this term pivot
         */
        final Term[] terms;
        Int pivotPosition = new Int();
        if (nonProductOfSumsPositions.isEmpty() || nonProductOfSumsPositions.size() == temp.size()) {
            //if nonProductOfSumsPositions.isEmpty(), then tensor already of form (a+b)*c + (a+b)*d
            //or if no any sum in terms, then tensor has form a*c + b*c
            terms = sum2SplitArray((Sum) temp, pivotPosition);
        } else {//if no, we need to rebuild tensor
            SumBuilder sb = new SumBuilder();
            for (i = nonProductOfSumsPositions.size() - 1; i >= 0; --i) {
                assert temp instanceof Sum;
                sb.put(temp.get(nonProductOfSumsPositions.get(i)));
                temp = ((Sum) temp).remove(nonProductOfSumsPositions.get(i));
            }
            Tensor withoutSumsTerm = factorSymbolicTerms(sb.build(), factorizationEngine);
            if (isProductOfSums(withoutSumsTerm)) {
                temp = Tensors.sum(temp, withoutSumsTerm);
                if (!(temp instanceof Sum))
                    //case when e.g. (a+b)*c - a*c - b*c = (a+b)*c - (a+b)*c = 0
                    return temp;
                terms = sum2SplitArray((Sum) temp, pivotPosition);
            } else {
                //case when tensor of form (a+b)*c + a + b
                if (!(temp instanceof Sum))
                    terms = new Term[]{tensor2term(temp), tensor2term(withoutSumsTerm)};
                else {
                    terms = new Term[temp.size() + 1];
                    System.arraycopy(sum2SplitArray((Sum) temp, pivotPosition), 0, terms, 0, temp.size());
                    terms[temp.size()] = tensor2term(withoutSumsTerm);//new Term(new FactorNode[]{createNode(withoutSumsTerm)});
                }
                pivotPosition.value = terms[pivotPosition.value].factors.length > terms[terms.length - 1].factors.length
                        ? terms.length - 1 : pivotPosition.value;
            }
        }
        //do stuff
        temp = mergeTerms(terms, pivotPosition.value, tensor);
        if (factorOutImageOne)
            temp = Tensors.multiply(Complex.IMAGINARY_UNIT, temp);
        return temp;
    }

    private static Tensor mergeTerms(final Term[] terms, final int pivotPosition, final Tensor tensor) {
        /*
        * S3:
        * initialize reference in pivot factors
        */
        Term pivot = terms[pivotPosition];
        FactorNode baseNode;
        List<FactorNode> baseFactors = new ArrayList<>(pivot.factors.length);
        for (FactorNode node : pivot.factors) {
            baseFactors.add(baseNode = node.clone());
            baseNode.minExponent = new BigInt();
        }

        /*
         * S4:
         * merge all terms
         */
        BigInteger baseExponent, tempExponent;
        ArrayList<FactorNode> tempList;
        Boolean sign = null;
        int i, j;

        for (i = terms.length - 1; i >= 0; --i) {
            if (baseFactors.isEmpty())
                return tensor;

            for (j = baseFactors.size() - 1; j >= 0; --j) {
                baseNode = baseFactors.get(j);
                tempList = terms[i].map.get(baseNode.tensor.hashCode());
                if (tempList == null) {
                    baseFactors.remove(j);
                    continue;
                }
                for (FactorNode nn : tempList) {
                    sign = TensorUtils.compare1(baseNode.tensor, nn.tensor);
                    if (sign == null)
                        continue;

                    baseExponent = baseNode.exponent;
                    tempExponent = nn.exponent;

                    if (baseExponent.signum() != tempExponent.signum()) {
                        baseFactors.remove(j);
                        continue;
                    }

                    if (sign)
                        nn.diffSigns = true;

                    nn.minExponent = baseNode.minExponent;

                    if (baseExponent.signum() > 0 && baseExponent.compareTo(tempExponent) > 0)
                        baseNode.exponent = tempExponent;
                    else if (baseExponent.signum() < 0 && baseExponent.compareTo(tempExponent) < 0)
                        baseNode.exponent = tempExponent;
                    break;
                }

                if (sign == null) {
                    baseFactors.remove(j);
                    continue;
                }
            }
        }

        if (baseFactors.isEmpty())
            return tensor;
        ProductBuilder pb = new ProductBuilder(baseFactors.size(), baseFactors.size());
        for (FactorNode node : baseFactors) {
            pb.put(node.toTensor());
            node.minExponent.value = node.exponent;
        }

        SumBuilder sb = new SumBuilder(tensor.size());
        for (Term term : terms)
            sb.put(nodesToProduct(term.factors));

        return Tensors.multiply(pb.build(), sb.build());
    }

    private static Term[] sum2SplitArray(Sum sum, Int pivotPosition) {
        Term[] terms = new Term[sum.size()];
        int pivotSumsCount = Integer.MAX_VALUE, pivotPosition1 = -1;
        for (int i = sum.size() - 1; i >= 0; --i) {
            terms[i] = tensor2term(sum.get(i));
            if (terms[i].factors.length < pivotSumsCount) {
                pivotSumsCount = terms[i].factors.length;
                pivotPosition1 = i;
            }
        }
        pivotPosition.value = pivotPosition1;
        return terms;
    }

    private static Term tensor2term(Tensor tensor) {
        if (tensor instanceof Product) {
            FactorNode[] factors = new FactorNode[tensor.size()];
            int i = -1;
            for (Tensor t : tensor)
                factors[++i] = createNode(t);
            return new Term(factors);
        }
        return new Term(new FactorNode[]{createNode(tensor)});
    }


    private static FactorNode createNode(Tensor tensor) {
        if (tensor instanceof Power && TensorUtils.isInteger(tensor.get(1)))
            return new FactorNode(tensor.get(0),
                    ((Rational) ((Complex) tensor.get(1)).getReal()).getNumerator());
        return new FactorNode(tensor, BigInteger.ONE);
    }

    private static Tensor nodesToProduct(FactorNode[] nodes) {
        Tensor[] tensors = new Tensor[nodes.length];
        for (int i = nodes.length - 1; i >= 0; --i)
            tensors[i] = nodes[i].toTensor();
        return Tensors.multiply(tensors);
    }

    private static class Term {
        final FactorNode[] factors;
        final TIntObjectHashMap<ArrayList<FactorNode>> map;

        private Term(FactorNode[] factors) {
            this.factors = factors;
            map = new TIntObjectHashMap<>(factors.length);
            ArrayList<FactorNode> list;
            for (FactorNode t : factors) {
                list = map.get(t.tensor.hashCode());
                if (list != null) {
                    list.add(t);
                    continue;
                }
                list = new ArrayList<>();
                list.add(t);
                map.put(t.tensor.hashCode(), list);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; ; ++i) {
                sb.append('(').append(factors[i]).append(')');
                if (i == factors.length - 1)
                    return sb.toString();
                sb.append("*");
            }
        }
    }

    private static class FactorNode {
        final Tensor tensor;
        BigInteger exponent;
        BigInt minExponent;
        boolean diffSigns = false;

        private FactorNode(Tensor tensor, BigInteger exponent) {
            this.tensor = tensor;
            this.exponent = exponent;
        }

        Tensor toTensor() {
            BigInteger exponent = this.exponent;
            if (minExponent != null && minExponent.value != null) {
                exponent = exponent.subtract(minExponent.value);
                if (diffSigns && minExponent.value.testBit(0))
                    return Tensors.negate(Tensors.pow(tensor, new Complex(exponent)));
            }
            return Tensors.pow(tensor, new Complex(exponent));
        }

        @Override
        public boolean equals(Object o) {
            return TensorUtils.equals(((FactorNode) o).tensor, tensor);
        }

        @Override
        public int hashCode() {
            return tensor.hashCode();
        }

        @Override
        public String toString() {
            return tensor + " -> " + exponent;
        }

        public FactorNode clone() {
            return new FactorNode(tensor, exponent);
        }
    }

    private static final class BigInt {
        BigInteger value;
    }

    private static final class Int {
        int value;
    }
}
