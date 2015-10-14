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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.options.Options;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;

import java.util.ArrayList;
import java.util.List;

import static cc.redberry.core.transformations.expand.ExpandUtils.expandPairOfSums;
import static cc.redberry.core.transformations.expand.ExpandUtils.multiplySumElementsOnFactor;

/**
 * Expands out products leaving all symbolic parts unexpanded.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.9
 */
public final class ExpandTensorsTransformation implements TransformationToStringAble {
    /**
     * The default instance.
     */
    public static final ExpandTensorsTransformation EXPAND_TENSORS = new ExpandTensorsTransformation();

    private final boolean leaveScalars;
    private final Transformation[] transformations;
    private final TraverseGuide traverseGuide;

    private ExpandTensorsTransformation() {
        this(new Transformation[0]);
    }

    /**
     * Creates expand transformation with specified additional transformations to
     * be applied after each step of expand.
     *
     * @param transformations transformations to be applied after each step of expand
     */
    public ExpandTensorsTransformation(Transformation... transformations) {
        this(false, transformations);
    }

    /**
     * Creates expand transformation with specified additional transformations to
     * be applied after each step of expand.
     *
     * @param leaveScalars    leave scalars (like (f_a*f^a + t_a*t^a)*(n_a*n^a + j_a*j^a) not expanded
     * @param transformations transformations to be applied after each step of expand
     */
    public ExpandTensorsTransformation(boolean leaveScalars, Transformation... transformations) {
        this(leaveScalars, transformations, TraverseGuide.ALL);
    }

    /**
     * Creates expand transformation with specified additional transformations to
     * be applied after each step of expand and leaves unexpanded parts of expression specified by
     * {@code traverseGuide}.
     *
     * @param leaveScalars    leave scalars (like (f_a*f^a + t_a*t^a)*(n_a*n^a + j_a*j^a) not expanded
     * @param transformations transformations to be applied after each step of expand
     * @param traverseGuide   traverse guide
     */
    public ExpandTensorsTransformation(boolean leaveScalars, Transformation[] transformations, TraverseGuide traverseGuide) {
        this.leaveScalars = leaveScalars;
        this.transformations = transformations;
        this.traverseGuide = traverseGuide;
    }

    @Creator
    public ExpandTensorsTransformation(@Options ExpandTensorsOptions options) {
        this.leaveScalars = options.leaveScalars;
        this.transformations = new Transformation[]{options.simplifications};
        this.traverseGuide = options.traverseGuide;
    }


    @Override
    public Tensor transform(Tensor tensor) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor, traverseGuide);
        Tensor current;
        while ((current = iterator.next()) != null)
            if (current instanceof Product)
                iterator.unsafeSet(expandProduct((Product) current));
        return iterator.result();
    }

    private Tensor expandProduct(Product product) {
        List<Tensor> ns;//non sums
        List<Sum> sums;
        if (leaveScalars) {
            ProductContent content = product.getContent();
            ns = new ArrayList<>(content.size());
            ns.add(product.getIndexlessSubProduct());
            sums = new ArrayList<>(content.size());
            for (Tensor t : content)
                if (t instanceof Sum) sums.add((Sum) t);
                else ns.add(t);
        } else {
            ns = new ArrayList<>(product.size());
            sums = new ArrayList<>(product.size());
            for (Tensor t : product)
                if (ExpandUtils.sumContainsIndexed(t)) sums.add((Sum) t);
                else ns.add(t);
        }

        if (sums.isEmpty())
            return product;

        if (sums.size() == 1)
            return multiplySumElementsOnFactor(sums.get(0), Tensors.multiply(ns), transformations);

        Tensor base = sums.get(0);
        for (int i = 1, size = sums.size(); ; ++i)
            if (i == size - 1) {
                if (base == null)
                    return multiplySumElementsOnFactor(sums.get(i), Tensors.multiply(ns), transformations);
                else
                    return expandPairOfSums((Sum) base, sums.get(i), ns.toArray(new Tensor[ns.size()]), transformations);
            } else {
                if (base == null) {
                    base = sums.get(i);
                    continue;
                }

                base = expandPairOfSums((Sum) base, sums.get(i), transformations);
                if (!(base instanceof Sum)) {
                    ns.add(base);
                    base = null;
                }
            }
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "ExpandTensors";
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }
}
