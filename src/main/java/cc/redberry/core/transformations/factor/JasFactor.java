/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
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
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigRational;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.FactorAbstract;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.FactorFactory;
import cc.redberry.core.utils.TensorUtils;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class JasFactor {
    public static final char START_CHAR = 'a';

    public static Tensor factor(Tensor t) {
//        System.out.println(t);
        TIntObjectMap<Var> vars = getVars(t);
        Var[] varsArray = vars.values(new Var[vars.size()]);
        Arrays.sort(varsArray);
        String[] forFactoryNames = new String[varsArray.length];
        for (int i = 0; i < varsArray.length; ++i)
            varsArray[i].polyName =
                    forFactoryNames[varsArray[i].position = i]
                            = String.valueOf((char) (START_CHAR + i));
        GenPolynomialRing<BigInteger> factory =
                new GenPolynomialRing<>(BigInteger.ONE, forFactoryNames);

        GenPolynomial<BigInteger> poly;
        java.math.BigInteger gcd, lcm;
        if (containsRationals(t)) {
            GenPolynomialRing<BigRational> ratFactory =
                    new GenPolynomialRing<>(BigRational.ONE, forFactoryNames);

            GenPolynomial<BigRational> polyRat = tensor2Poly(t, ratFactory, vars, RationalConverter);
            Object[] factors = PolyUtil.integerFromRationalCoefficientsFactor(factory, polyRat);
            gcd = (java.math.BigInteger) factors[0];
            lcm = (java.math.BigInteger) factors[1];
            poly = (GenPolynomial<BigInteger>) factors[2];
        } else {
            gcd = java.math.BigInteger.ONE;
            lcm = java.math.BigInteger.ONE;
            poly = tensor2Poly(t, factory, vars, IntegerConverter);
        }


        FactorAbstract<BigInteger> jasFactor = FactorFactory.getImplementation(BigInteger.ONE);

        SortedMap<GenPolynomial<BigInteger>, Long> map = jasFactor.factors(poly);

        //assert jasFactor.isFactorization(poly, map);

        List<Tensor> toMultiply = new ArrayList<>(map.size());
        for (SortedMap.Entry<GenPolynomial<BigInteger>, Long> entry : map.entrySet())
            toMultiply.add(Tensors.pow(poly2Tensor(entry.getKey(), varsArray),
                    new Complex(entry.getValue())));
        if (!gcd.equals(java.math.BigInteger.ONE) || !lcm.equals(java.math.BigInteger.ONE))
            toMultiply.add(new Complex(new Rational(gcd, lcm)));

        return Tensors.multiply(toMultiply.toArray(new Tensor[toMultiply.size()]));
    }

    static <T extends RingElem<T>> GenPolynomial<T> tensor2Poly(Tensor tensor, GenPolynomialRing<T> factory, TIntObjectMap<Var> vars, NumberConverter<T> numberConverter) {
        if (tensor.getClass() == SimpleTensor.class)
            return factory.getONE().
                    multiply(ExpVector.create(vars.size(), vars.get(((SimpleTensor) tensor).getName()).position, 1L));
        else if (tensor.getClass() == Power.class) {
            long pow = ((Complex) tensor.get(1)).longValue();
            if (tensor.get(0) instanceof SimpleTensor)
                return factory.getONE().
                        multiply(ExpVector.create(vars.size(),
                                vars.get(((SimpleTensor) tensor.get(0)).getName()).position, pow));
            else {
                GenPolynomial<T> result = factory.getONE();
                GenPolynomial<T> base = tensor2Poly(tensor.get(0), factory, vars, numberConverter);
                while (pow > 0) {
                    if ((pow & 0x1) != 0)
                        result = result.multiply(base);
                    base = base.multiply(base);
                    pow = pow >>> 1;
                }
                return result;
            }
        } else if (tensor.getClass() == Sum.class) {
            GenPolynomial<T> result = factory.getZERO();
            for (Tensor t : tensor)
                result = result.sum(tensor2Poly(t, factory, vars, numberConverter));
            return result;
        } else if (tensor.getClass() == Product.class) {
            GenPolynomial<T> result = factory.getONE();
            for (Tensor t : tensor)
                result = result.multiply(tensor2Poly(t, factory, vars, numberConverter));
            return result;
        } else if (tensor.getClass() == Complex.class) {
            return factory.getONE().multiply(numberConverter.convertComplex(((Complex) tensor)));
        }
        throw new RuntimeException("ddd");
    }

    private static interface NumberConverter<T extends RingElem<T>> {
        T convertComplex(Complex complex);
    }

    private static final NumberConverter<BigRational> RationalConverter = new NumberConverter<BigRational>() {
        @Override
        public BigRational convertComplex(Complex complex) {
            Rational rational = (Rational) complex.getReal();
            return new BigRational(new BigInteger(rational.getNumerator()),
                    new BigInteger(rational.getDenominator()));
        }
    };

    private static final NumberConverter<BigInteger> IntegerConverter = new NumberConverter<BigInteger>() {
        @Override
        public BigInteger convertComplex(Complex complex) {
            return new BigInteger(((Rational) complex.getReal()).getNumerator());
        }
    };

    private static boolean containsRationals(Tensor tensor) {
        if (tensor instanceof Complex) {
            if (((Complex) tensor).isInteger())
                return false;
            return true;
        }
        for (Tensor t : tensor) {
            if (containsRationals(t))
                return true;
        }
        return false;
    }

    static Tensor poly2Tensor(GenPolynomial<BigInteger> poly, Var[] varsArray) {
        if (poly.length() == 0)
            return Complex.ZERO;
        List<Tensor> temp = new ArrayList<>(), sum = new ArrayList<>(poly.length());
        long lExp;
        BigInteger coefficient;
        ExpVector exp;
        for (Monomial<BigInteger> monomial : poly) {
            coefficient = monomial.coefficient();
            exp = monomial.exponent();
            temp.clear();

            temp.add(new Complex(new Rational(coefficient.getVal())));
            for (int i = 0; i < exp.length(); ++i)
                if ((lExp = exp.getVal(i)) != 0)
                    temp.add(Tensors.pow(varsArray[i].simpleTensor, new Complex(lExp)));

            sum.add(Tensors.multiply(temp.toArray(new Tensor[temp.size()])));
        }
        return Tensors.sum(sum.toArray(new Tensor[sum.size()]));
    }

    static TIntObjectMap<Var> getVars(Tensor... tensors) {
        TIntObjectMap<Var> vars = new TIntObjectHashMap<>();
        for (Tensor t : tensors)
            addVars(t, vars, 1);
        return vars;
    }

    static void addVars(Tensor tensor, TIntObjectMap<Var> vars, long power) {
        if (power < 0)
            throw new IllegalArgumentException("Negative powers.");
        if (tensor.getClass() == SimpleTensor.class) {
            if (tensor.getIndices().size() != 0)
                throw new IllegalArgumentException();
            int name = ((SimpleTensor) tensor).getName();
            Var var = vars.get(name);
            if (var == null)
                vars.put(name, var = new Var((SimpleTensor) tensor));
            var.maxPower = Math.max(power, var.maxPower);
            return;
        } else if (tensor.getClass() == Power.class) {
            if (!TensorUtils.isNaturalNumber(tensor.get(1)))
                throw new IllegalArgumentException();
            long pow = power * ((Complex) tensor.get(1)).longValue();
            addVars(tensor.get(0), vars, pow);
            return;
        } else if (tensor instanceof MultiTensor) {
            for (Tensor t : tensor)
                addVars(t, vars, power);
            return;
        } else if (tensor.getClass() == Complex.class) {
            if (((Complex) tensor).isNumeric() || !((Complex) tensor).isReal())
                throw new IllegalArgumentException("Illegal coefficients.");
            return;
        }
        throw new IllegalArgumentException();
    }


    static class Var implements Comparable<Var> {
        final int name;
        String polyName = null;
        int position;
        long maxPower;
        final SimpleTensor simpleTensor;

        private Var(SimpleTensor simpleTensor) {
            this.simpleTensor = simpleTensor;
            this.name = simpleTensor.getName();
        }

        @Override
        public int compareTo(Var o) {
            return -Long.compare(o.maxPower, this.maxPower);
        }
    }
}
