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
package cc.redberry.core.transformations.fractions;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.factor.Factor;
import cc.redberry.core.utils.THashMap;
import cc.redberry.core.utils.TensorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cc.redberry.core.transformations.CollectScalarFactors.collectScalarFactors;
import static cc.redberry.core.transformations.CollectScalarFactors.collectScalarFactorsInProduct;

/**
 * Puts terms in a sum over a common denominator.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
//TODO review after logical completion of tensors standard form strategy 
public final class Together implements Transformation {

    public static final Together INSTANCE = new Together(false);
    public static final Together TOGETHER_FACTOR = new Together(true);

    private final boolean doFactor;

    private Together(boolean doFactor) {
        this.doFactor = doFactor;
    }

    @Override
    public Tensor transform(Tensor t) {
        return together(t, doFactor);
    }

    public static Tensor together(Tensor t) {
        return together(t, false);
    }

    public static Tensor together(Tensor t, boolean doFactor) {
        TensorLastIterator iterator = new TensorLastIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null) {
            if (c instanceof Sum)
                iterator.set(togetherSum(c, doFactor));
            if (c instanceof Product)
                iterator.set(collectScalarFactorsInProduct((Product) c));
        }
        return iterator.result();
    }

    private static Tensor togetherSum(Tensor t, boolean doFactor) {
        boolean performTogether = false;
        for (Tensor s : t)
            if (s instanceof Product) {
                for (Tensor p : s)
                    if (checkPower(p)) {
                        performTogether = true;
                        break;
                    }
            } else if (checkPower(s)) {
                performTogether = true;
                break;
            }
        if (!performTogether)
            return t;

        SplitStruct base = splitFraction(t.get(0), doFactor), temp;
        @SuppressWarnings("unchecked") List<Tensor> numeratorTerms[] = new List[t.size()];
        numeratorTerms[0] = new ArrayList<>();
        numeratorTerms[0].add(base.numerator);
        Tensor s, power;
        Complex exponent, _exponent;
        int i, j;
        for (i = 1; i < t.size(); ++i) {
            s = t.get(i);
            temp = splitFraction(s, doFactor);

            List<Tensor> newNumeratorTerm = new ArrayList<>();
            newNumeratorTerm.add(temp.numerator);
            numeratorTerms[i] = newNumeratorTerm;

            for (Map.Entry<Tensor, Complex> baseEntry : base.denominators.entrySet()) {
                exponent = temp.denominators.get(baseEntry.getKey());
                if (exponent == null) {
                    power = Tensors.pow(baseEntry.getKey(), baseEntry.getValue());
                    newNumeratorTerm.add(power);
                } else if ((_exponent = baseEntry.getValue().subtract(exponent)).getReal().signum() > 0) {
                    power = Tensors.pow(baseEntry.getKey(), _exponent);
                    newNumeratorTerm.add(power);
                }
            }

            for (Map.Entry<Tensor, Complex> tempEntry : temp.denominators.entrySet()) {
                exponent = base.denominators.get(tempEntry.getKey());
                if (exponent == null) {
                    power = Tensors.pow(tempEntry.getKey(), tempEntry.getValue());
                    for (j = 0; j < i; ++j)
                        numeratorTerms[j].add(power);
                    base.denominators.put(tempEntry.getKey(), tempEntry.getValue());
                } else if ((_exponent = tempEntry.getValue().subtract(exponent)).getReal().signum() > 0) {
                    power = Tensors.pow(tempEntry.getKey(), _exponent);
                    for (j = 0; j < i; ++j)
                        numeratorTerms[j].add(power);
                    base.denominators.put(tempEntry.getKey(), tempEntry.getValue());
                }
            }

        }
        SumBuilder numeratorSumBuilder = new SumBuilder();
        for (List<Tensor> term : numeratorTerms)
            numeratorSumBuilder.put(collectScalarFactors(Tensors.multiplyAndRenameConflictingDummies(term.toArray(new Tensor[term.size()]))));//TODO ?rename conflicts
        //TODO improve performance
        Tensor[] resultProduct = new Tensor[1 + base.denominators.size()];
        resultProduct[0] = numeratorSumBuilder.build();
        i = 0;
        for (Map.Entry<Tensor, Complex> baseEntry : base.denominators.entrySet())
            resultProduct[++i] = Tensors.pow(baseEntry.getKey(), baseEntry.getValue().negate());
        return Tensors.multiplyAndRenameConflictingDummies(resultProduct);
    }

    private static class SplitStruct {

        final THashMap<Tensor, Complex> denominators;
        final Tensor numerator;

        public SplitStruct(THashMap<Tensor, Complex> denominators, Tensor numerator) {
            this.denominators = denominators;
            this.numerator = numerator;
        }
    }

    private static SplitStruct splitFraction(Tensor tensor, boolean doFactor) {
        if (doFactor)
            tensor = Factor.factor(tensor);

        THashMap<Tensor, Complex> map = new THashMap<>();
        if (checkPower(tensor)) {
            map.put(tensor.get(0), ((Complex) tensor.get(1)).negate());
            return new SplitStruct(map, Complex.ONE);
        }
        if (tensor instanceof Product) {
            Tensor product = tensor;
            Tensor temp = null, m;
            for (int i = tensor.size() - 1; i >= 0; --i) {
                m = tensor.get(i);
                if (checkPower(m)) {
                    map.put(m.get(0), ((Complex) m.get(1)).negate());
                    if (product instanceof Product)
                        temp = product = ((Product) product).remove(i);
                    else {
                        assert i == 0;
                        temp = Complex.ONE;
                    }
                }
            }
            if (temp == null)
                temp = tensor;
            return new SplitStruct(map, temp);
        }
        return new SplitStruct(map, tensor);
    }

    private static boolean checkPower(Tensor power) {
        return power instanceof Power && TensorUtils.isRealNegativeNumber(power.get(1));
    }
}
