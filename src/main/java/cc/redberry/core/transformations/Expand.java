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
package cc.redberry.core.transformations;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.TensorUtils;

import java.util.ArrayList;

import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.ExpandUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Expand implements Transformation {
    public static final Expand Expand = new Expand();

    private final Transformation[] transformations;

    public Expand() {
        this.transformations = new Transformation[0];
    }

    public Expand(Transformation[] transformations) {
        this.transformations = transformations;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        return expand(tensor, transformations);
    }

    public static Tensor expand(Tensor tensor) {
        return expand(tensor, new Transformation[0]);
    }

    public static Tensor expand(Tensor tensor, Transformation... transformations) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor, TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (current instanceof Product)
                iterator.unsafeSet(expandProductOfSums(current, transformations));
            else if (isPositiveIntegerPower(current)) {
                Sum sum = (Sum) current.get(0);
                int exponent = ((Complex) current.get(1)).getReal().intValue();
                if (TensorUtils.isSymbolic(sum))
                    iterator.unsafeSet(expandSymbolicPower(sum, exponent, transformations));
                else
                    iterator.set(expandPower(sum, exponent, iterator.getForbidden(), transformations));
            }

        }
        return iterator.result();
    }

    private static Tensor expandProductOfSums(Tensor current, Transformation[] transformations) {

        // a*b | a_m*b_v | (a+b*f) | (a_i+(c+2)*b_i)
        //1: a*b | (a+b*f)  => result1 = a**2*b+b**2*a*f    Tensors.multiplyAndExpand(nonSum, Sum)
        //2: a_m*b_v | (a_i+(c+2)*b_i)   => result2 = a_m*b_v*a_i+(c+2)*a_m*b_v*b_i
        //3: result1 * result2                              Tensors.multiplyAndExpand(scalarSum, nonScalarSum)
        // (a_m^m**2*b+b**2*a*a_m^m) * (a_m^m*a_m*b_v*a_i+a_m^m**2*B_avi+(c+2)*a_m*b_v*b_i) => (.....)*a_m*b_v*a_i +(....)*B_avi + ( .....  )*a_m*b_v*b_i

        ArrayList<Tensor> indexlessNonSums = new ArrayList<>();
        ArrayList<Tensor> nonSums = new ArrayList<>();

        Sum indexlessSum = null;
        Sum sum = null;

        int i;
        Tensor t, temp;
        boolean expand = false;
        for (i = current.size() - 1; i >= 0; --i) {
            t = current.get(i);
            if (t.getIndices().size() == 0 && !sumContainsNonIndexless(t))
                if (t instanceof Sum)
                    if (indexlessSum == null)
                        indexlessSum = (Sum) t;
                    else {
                        temp = expandPairOfSums((Sum) t, indexlessSum, transformations);
                        expand = true;
                        if (temp instanceof Sum)
                            indexlessSum = (Sum) temp;
                        else {
                            indexlessNonSums.add(temp);
                            indexlessSum = null;
                        }
                    }
                else
                    indexlessNonSums.add(t);
            else if (t instanceof Sum)
                if (sum == null)
                    sum = (Sum) t;
                else {
                    temp = expandPairOfSums((Sum) t, sum, transformations);
                    temp = expand(temp, transformations);
                    expand = true;
                    if (temp instanceof Sum)
                        sum = (Sum) temp;
                    else {
                        nonSums.add(temp);
                        sum = null;
                    }
                }
            else
                nonSums.add(t);
        }

        if (!expand && sum == null && (indexlessSum == null || indexlessNonSums.isEmpty()))
            return current;


        Tensor indexless = multiply(indexlessNonSums.toArray(new Tensor[indexlessNonSums.size()]));
        if (indexlessSum != null)
            indexless = multiplySumElementsOnFactor(indexlessSum, indexless);

        Tensor main = multiply(nonSums.toArray(new Tensor[nonSums.size()]));
        if (sum != null)
            main = multiplySumElementsOnFactor(sum, main);

        if (main instanceof Sum)
            main = multiplySumElementsOnFactorAndExpandScalars((Sum) main, indexless);
        else
            main = multiply(indexless, main);

        return main;
    }


}
