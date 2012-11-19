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
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.ExpVector;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.Monomial;
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
        TIntObjectMap<Var> vars = getVars(t);
        Var[] varsArray = vars.values(new Var[vars.size()]);
        Arrays.sort(varsArray);
        String[] forFactoryNames = new String[varsArray.length];
        for (int i = 0; i < varsArray.length; ++i)
            varsArray[i].polyName =
                    forFactoryNames[varsArray[i].position = i]
                            = String.valueOf((char) (START_CHAR + i));

        GenPolynomialRing<BigRational> factory =
                new GenPolynomialRing<>(BigRational.ONE, forFactoryNames);

        GenPolynomial<BigRational> poly = tensor2Poly(t, factory, vars);

        FactorAbstract<BigRational> jasFactor = FactorFactory.getImplementation(BigRational.ONE);

        SortedMap<GenPolynomial<BigRational>, Long> map = jasFactor.factors(poly);

        List<Tensor> toMultiply = new ArrayList<>(map.size());
        for (SortedMap.Entry<GenPolynomial<BigRational>, Long> entry : map.entrySet())
            toMultiply.add(Tensors.pow(poly2Tensor(entry.getKey(), varsArray),
                    new Complex(entry.getValue())));

        return Tensors.multiply(toMultiply.toArray(new Tensor[toMultiply.size()]));
    }

    /**
     * Only for univariate polynomials.
     *
     * @param a
     * @param b
     * @return [a/gcd, b/gcd]
     */
    static Tensor[] reduceGCD(Tensor a, Tensor b) {
        TIntObjectMap<Var> vars = getVars(a, b);
        Var[] varsArray = vars.values(new Var[vars.size()]);
        Arrays.sort(varsArray);
        String[] forFactoryNames = new String[varsArray.length];
        for (int i = 0; i < varsArray.length; ++i)
            varsArray[i].polyName =
                    forFactoryNames[varsArray[i].position = i]
                            = String.valueOf((char) (START_CHAR + i));

        GenPolynomialRing<BigRational> factory =
                new GenPolynomialRing<>(BigRational.ONE, forFactoryNames);

        GenPolynomial<BigRational> aPoly = tensor2Poly(a, factory, vars),
                bPoly = tensor2Poly(b, factory, vars);
        GenPolynomial<BigRational> gcd = aPoly.gcd(bPoly);
        aPoly = aPoly.divide(gcd);
        bPoly = bPoly.divide(gcd);
        return new Tensor[]{poly2Tensor(aPoly, varsArray), poly2Tensor(bPoly, varsArray)};
    }

    static GenPolynomial<BigRational> tensor2Poly(Tensor tensor, GenPolynomialRing<BigRational> factory, TIntObjectMap<Var> vars) {
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
                GenPolynomial<BigRational> result = factory.getONE();
                GenPolynomial<BigRational> base = tensor2Poly(tensor.get(0), factory, vars);
                while (pow > 0) {
                    if ((pow & 0x1) != 0)
                        result = result.multiply(base);
                    base = base.multiply(base);
                    pow = pow >>> 1;
                }
                return result;
            }
        } else if (tensor.getClass() == Sum.class) {
            GenPolynomial<BigRational> result = factory.getZERO();
            for (Tensor t : tensor)
                result = result.sum(tensor2Poly(t, factory, vars));
            return result;
        } else if (tensor.getClass() == Product.class) {
            GenPolynomial<BigRational> result = factory.getONE();
            for (Tensor t : tensor)
                result = result.multiply(tensor2Poly(t, factory, vars));
            return result;
        } else if (tensor.getClass() == Complex.class) {
            Rational rational = (Rational) ((Complex) tensor).getReal();
            return factory.getONE().multiply(
                    new BigRational(new BigInteger(rational.getNumerator()),
                            new BigInteger(rational.getDenominator())));
        }
        throw new RuntimeException("ddd");
    }

    static Tensor poly2Tensor(GenPolynomial<BigRational> poly, Var[] varsArray) {
        if (poly.length() == 0)
            return Complex.ZERO;
        List<Tensor> temp = new ArrayList<>(), sum = new ArrayList<>(poly.length());
        long lExp;
        BigRational coefficient;
        ExpVector exp;
        for (Monomial<BigRational> monomial : poly) {
            coefficient = monomial.coefficient();
            exp = monomial.exponent();
            temp.clear();

            temp.add(new Complex(new Rational(coefficient.numerator(), coefficient.denominator())));
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
