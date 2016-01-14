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
package cc.redberry.core.transformations;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.expand.ExpandPort;
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation;
import cc.redberry.core.utils.OutputPort;
import gnu.trove.set.hash.TIntHashSet;

import static cc.redberry.core.utils.TensorUtils.getSimpleTensorsNames;

/**
 * @author Stanislav Poslavsky
 */
public final class ApplyDiracDeltasTransformation implements TransformationToStringAble {
    public static final ApplyDiracDeltasTransformation APPLY_DIRAC_DELTAS_TRANSFORMATION = new ApplyDiracDeltasTransformation();

    private ApplyDiracDeltasTransformation() {
    }

    private static boolean containsDiracDeltas(Tensor t) {
        if (t instanceof TensorField && ((TensorField) t).isDiracDelta()) {
            if (t.get(0) instanceof SimpleTensor)
                return true;
            else if (t.get(0) instanceof Product)
                return ((Product) t.get(0)).getDataSubProduct() instanceof SimpleTensor;
            else return false;
        }

        boolean c = false;
        for (Tensor tensor : t)
            if (containsDiracDeltas(tensor))
                return true;
        return false;
    }

    @Override
    public Tensor transform(Tensor t) {
        if (!containsDiracDeltas(t))
            return t;

        OutputPort<Tensor> port = ExpandPort.createPort(t, true);
        SumBuilder sb = new SumBuilder();

        Tensor current, temp;
        while ((current = port.take()) != null) {
            if (current instanceof Product) {
                out:
                while (true) {
                    Product product = ((Product) current);
                    for (int i = 0; i < product.size(); i++) {
                        Tensor dd = current.get(i);
                        if (dd instanceof TensorField && ((TensorField) dd).isDiracDelta()) {
                            temp = product.remove(i);
                            temp = createSubstitution((TensorField) dd).transform(temp);
                            if (!intersects(getSimpleTensorsNames(temp), getSimpleTensorsNames(dd.get(0)))) {
                                current = temp;
                                if (current instanceof Product)
                                    continue out;
                                else break out;
                            }
                        }
                    }
                    break;
                }
            }
            sb.put(current);
        }
        return sb.build();
    }

    private static Transformation createSubstitution(TensorField dd) {
        Tensor from = dd.get(0), to = dd.get(1);
        to = ApplyIndexMapping.applyIndexMapping(to, new Mapping(dd.getArgIndices(1).toArray(),
                dd.getArgIndices(0).toArray()), new int[0]);
        return new SubstitutionTransformation(from, to);
    }

    @Override
    public String toString() {
        return this.toString(CC.getDefaultOutputFormat());
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "ApplyDiracDeltas";
    }

    private static boolean intersects(TIntHashSet a, TIntHashSet b) {
        if (b.size() > a.size())
            return intersects(b, a);
        b.retainAll(a);
        return !b.isEmpty();
    }
}
