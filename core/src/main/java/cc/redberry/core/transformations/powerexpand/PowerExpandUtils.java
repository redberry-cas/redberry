/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.core.transformations.powerexpand;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PowerExpandUtils {
    private PowerExpandUtils() {
    }

    public static boolean powerUnfoldApplicable(Tensor power, Indicator<Tensor> indicator) {
        return power instanceof Power &&
                (power.get(0).getIndices().size() != 0
                        || (power.get(0) instanceof Product && !TensorUtils.isInteger(power.get(1)))
                ) && powerExpandApplicable1(power, indicator);
    }

    public static boolean powerExpandApplicable(Tensor power, Indicator<Tensor> indicator) {
        return power instanceof Power && power.get(0) instanceof Product && !TensorUtils.isInteger(power.get(1)) && powerExpandApplicable1(power, indicator);
    }

    static boolean powerExpandApplicable1(Tensor power, Indicator<Tensor> indicator) {
        for (Tensor t : power.get(0))
            if (indicator.is(t)) return true;
        return indicator.is(power);
    }

    public static Tensor[] powerExpandToArray(Power power) {
        return powerExpandToArray(power, Indicator.TRUE_INDICATOR);
    }

    static Indicator<Tensor> varsToIndicator(final SimpleTensor[] vars) {
        final int[] names = new int[vars.length];
        for (int i = 0; i < vars.length; ++i)
            names[i] = vars[i].getName();
        Arrays.sort(names);

        return new Indicator<Tensor>() {
            @Override
            public boolean is(Tensor object) {
                int toCheck;
                if (object instanceof SimpleTensor)
                    toCheck = object.hashCode();
                else if (object instanceof Power) {
                    if (!(object.get(0) instanceof SimpleTensor))
                        return false;
                    toCheck = object.get(0).hashCode();
                } else return false;
                return Arrays.binarySearch(names, toCheck) >= 0;
            }
        };
    }

    public static Tensor[] powerExpandToArray(final Power power, final SimpleTensor[] vars) {
        return powerExpandToArray(power, varsToIndicator(vars));
    }

    public static Tensor[] powerExpandToArray(Power power, Indicator<Tensor> indicator) {
        if (!(power.get(0) instanceof Product))
            throw new IllegalArgumentException("Base should be product of tensors.");
        return powerExpandToArray1(power, indicator);
    }

    static Tensor[] powerExpandToArray1(Tensor power, Indicator<Tensor> indicator) {
        final Tensor[] scalars = ((Product) power.get(0)).getAllScalars();
        ArrayList<Tensor> factorOut = new ArrayList<>(scalars.length),
                leave = new ArrayList<>(scalars.length);

        Tensor exponent = power.get(1);
        for (int i = 0; i < scalars.length; ++i) {
            if (indicator.is(scalars[i]))
                factorOut.add(Tensors.pow(scalars[i], exponent));
            else leave.add(scalars[i]);
        }

        if (!leave.isEmpty())
            factorOut.add(Tensors.pow(
                    Tensors.multiply(leave.toArray(new Tensor[leave.size()])),
                    exponent));
        return factorOut.toArray(new Tensor[factorOut.size()]);
    }

    static Tensor[] powerExpandIntoChainToArray(Power power, int[] forbiddenIndices, Indicator<Tensor> indicator) {
        if (!(power.get(0) instanceof Product))
            throw new IllegalArgumentException("Base should be product of tensors.");
        return powerExpandIntoChainToArray1(power, forbiddenIndices, indicator);
    }

    static Tensor[] powerExpandIntoChainToArray1(Tensor power, int[] forbiddenIndices, Indicator<Tensor> indicator) {
        if (!TensorUtils.isPositiveNaturalNumber(power.get(1)))
            return powerExpandToArray1(power, indicator);
        final int exponent = ((Complex) power.get(1)).intValue();
        final Tensor[] scalars;
        if (power.get(0) instanceof Product)
            scalars = ((Product) power.get(0)).getAllScalars();
        else
            scalars = new Tensor[]{power.get(0)};

        ArrayList<Tensor> factorOut = new ArrayList<>(scalars.length),
                leave = new ArrayList<>(scalars.length);

        TIntHashSet allForbidden = new TIntHashSet(forbiddenIndices);
        int j;
        Tensor temp;
        for (int i = 0; i < scalars.length; ++i) {
            if (indicator.is(scalars[i])) {
                if (scalars[i] instanceof SimpleTensor && scalars[i].getIndices().size() == 0)//simple symbolic factor
                    factorOut.add(Tensors.pow(scalars[i], exponent));
                else
                    for (j = 0; j < exponent; ++j) {
                        temp = ApplyIndexMapping.renameDummy(scalars[i], allForbidden.toArray());
                        allForbidden.addAll(TensorUtils.getAllIndicesNamesT(temp));
                        factorOut.add(temp);
                    }
            } else leave.add(scalars[i]);
        }

        if (!leave.isEmpty())
            factorOut.add(Tensors.pow(
                    Tensors.multiply(leave.toArray(new Tensor[leave.size()])),
                    exponent));
        return factorOut.toArray(new Tensor[factorOut.size()]);
    }
}
