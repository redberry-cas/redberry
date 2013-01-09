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
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.math.BigInteger;
import java.util.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FactorTransformation implements Transformation {
    public static final FactorTransformation FACTOR = new FactorTransformation();

    private FactorTransformation() {
    }

    @Override
    public Tensor transform(Tensor t) {
        return factor(t);
    }

    public static Tensor factorSymbolicTerms(Tensor tensor) {
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
            symbolicPart = factorSymbolicTerm(symbolicPart);
            if (remainder instanceof Sum) {
                SumBuilder sb = new SumBuilder(remainder.size());
                for (Tensor tt : remainder)
                    sb.put(factorSymbolicTerms(tt));
                remainder = sb.build();
            } else
                remainder = factorSymbolicTerms(remainder);
            iterator.set(Tensors.sum(symbolicPart, remainder));
        }
        return iterator.result();
    }

    private static Tensor factorSymbolicTerm(Tensor sum) {
        TreeIterator iterator = new FromChildToParentIterator(sum);
        Tensor c;
        while ((c = iterator.next()) != null)
            if (c instanceof Sum)
                iterator.set(factorOut(c));

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
                            pb.put(JasFactor.factor(c.get(i)));
                        } else if (pb != null)
                            pb.put(c.get(i));
                    }
                    iterator.set(pb == null ? c : pb.build());
                }
            } else
                iterator.set(JasFactor.factor(c));
        }
        return iterator.result();
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
            if (!(t.get(1) instanceof Complex))
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

    public static Tensor factor(Tensor tensor) {
        return factorSymbolicTerms(tensor);
//        TensorFirstIterator iterator = new TensorFirstIterator(tensor);
//        TreeIterator iterator1;
//        Tensor c, t;
//        Complex e;
//        out:
//        while ((c = iterator.next()) != null) {
//            if (!(c instanceof Sum))
//                continue;
//            iterator1 = new TensorLastIterator(c);
//            boolean needTogether = false;
//            while ((t = iterator1.next()) != null) {
//                if (t.getIndices().size() != 0 || t instanceof ScalarFunction)
//                    continue out;
//
//                if (t instanceof Power) {
//                    if (!(t.get(1) instanceof Complex))
//                        continue out;
//                    e = (Complex) t.get(1);
//                    if (!e.isReal() || e.isNumeric())
//                        continue out;
//                    if (e.getReal().signum() < 0)
//                        needTogether = true;
//                }
//
//                if (t instanceof Product)
//                    for (Tensor tt : t)
//                        if (tt instanceof Power && TensorUtils.isNegativeIntegerNumber(tt.get(1)))
//                            needTogether = true;
//
//
//                if (t instanceof Sum)
//                    iterator1.set(factorOut(t));
//            }
//
//            iterator1 = new TensorFirstIterator(iterator1.result());
//            while ((c = iterator1.next()) != null) {
//                if (!(c instanceof Sum))
//                    continue;
//
//                if (needTogether) {
//                    c = Together.together(c, true);
//                    if (c instanceof Product) {
//                        for (int i = c.size() - 1; i >= 0; --i) {
//                            if (c.get(i) instanceof Sum)
//                                c = c.set(i, JasFactor.factor(c.get(i)));
//                        }
//                        iterator1.set(c);
//                    }
//                } else {
//                    iterator1.set(JasFactor.factor(c));
//                }
//            }
//            iterator.set(iterator1.result());
//        }
//        return iterator.result();
    }


    static Tensor factorOut(Tensor tensor) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(tensor);
        Tensor c;
        while ((c = iterator.next()) != null)
            if (c instanceof Sum)
                iterator.set(factorOut1(c));
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

    static Tensor factorOut1(Tensor tensor) {
        Tensor temp = tensor;
        /*
         * S1:
         * (a+b)*c + a*d + b*d -> (a+b)*c + (a+b)*d
         */
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
            Tensor withoutSumsTerm = factor(sb.build());
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
        return mergeTerms(terms, pivotPosition.value, tensor);
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
