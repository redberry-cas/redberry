/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.number.Real;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.TransformationToStringAble;

import java.math.BigInteger;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class FactorOutNumber implements TransformationToStringAble {
    public static final FactorOutNumber FACTOR_OUT_NUMBER = new FactorOutNumber();

    private FactorOutNumber() {
    }

    @Override
    public Tensor transform(Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        out:
        while ((c = iterator.next()) != null) {
            if (!(c instanceof Sum))
                continue;

            for (int i = c.size() - 1; i >= 0; --i)
                if (isComposite(getFactor(c.get(i))))
                    continue out;

            final BigInteger[] nums = new BigInteger[c.size()], dens = new BigInteger[c.size()];
            boolean allImaginary = true;

            for (int i = c.size() - 1; i >= 0; --i) {
                Complex factor = getFactor(c.get(i));
                Real real = factor.getReal();
                if (real.isZero())
                    real = factor.getImaginary();
                else
                    allImaginary = false;
                Rational rat = (Rational) real;
                nums[i] = rat.getNumerator();
                dens[i] = rat.getDenominator();
            }

            BigInteger commonNum = gcd(nums);
            BigInteger commonDen = gcd(dens);
            if (commonNum.equals(BigInteger.ONE)
                    && commonDen.equals(BigInteger.ONE)
                    && !allImaginary)
                continue;


            Complex commonFactor = new Complex(new Rational(commonNum, commonDen));
            if (allImaginary)
                commonFactor = commonFactor.multiply(Complex.IMAGINARY_UNIT);

            iterator.set(Tensors.multiply(commonFactor, FastTensors.multiplySumElementsOnFactor((Sum) c,
                    commonFactor.reciprocal())));
        }

        return iterator.result();
    }

    private static BigInteger gcd(BigInteger... array) {
        BigInteger result = array[0];
        for (int i = 1; i < array.length; ++i)
            result = result.gcd(array[i]);
        return result;
    }

    private static boolean isComposite(Complex a) {
        return (a == null
                || !(a.getReal().isZero() || a.getImaginary().isZero())
                || a.isOneOrMinusOne()
                || a.isNumeric());
    }

    private static Complex getFactor(Tensor t) {
        if (t instanceof Product)
            return ((Product) t).getFactor();
        else if (t instanceof Complex)
            return (Complex) t;
        return null;
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "FactorOutNumber";
    }
}
