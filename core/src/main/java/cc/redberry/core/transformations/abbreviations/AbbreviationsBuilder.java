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
package cc.redberry.core.transformations.abbreviations;

import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class AbbreviationsBuilder implements Transformation {
    public static final int DEFAULT_ABBR_SIZE = 50;
    public static final String DEFAULT_ABBR_PREFIX = "abbr";

    final TIntObjectHashMap<List<Abbreviation>> abbrs = new TIntObjectHashMap<>();
    public int maxSumSize = DEFAULT_ABBR_SIZE;
    public String abbrPrefix = DEFAULT_ABBR_PREFIX;
    public boolean abbreviateScalars = true;
    public boolean abbreviateScalarsSeparately = false;
    public boolean abbreviateTopLevel = false;

    @SuppressWarnings("unchecked")
    public Indicator<Tensor> filter = Indicator.TRUE_INDICATOR;
    public Indicator<FromChildToParentIterator> aFilter = Indicator.TRUE_INDICATOR;
    private int abbrCounter = 0;

    @Override
    public Tensor transform(Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null) {
            if (!abbreviateTopLevel && iterator.depth() == 0)
                continue;
            if (c instanceof Product && abbreviateScalars)
                iterator.set(abbreviateProduct(c));
            if (!filter.is(c))
                continue;
            if (!aFilter.is(iterator))
                continue;
            if (c instanceof Sum
                    && c.size() < maxSumSize
                    && TensorUtils.isSymbolic(c))
                iterator.set(abbreviate(c));
        }
        return iterator.result();
    }

    private Tensor abbreviateProduct(Tensor c) {
        Product p = (Product) c;
        final ProductContent content = p.getContent();
        Tensor[] scalars = content.getScalars();
        if (scalars.length == 0)
            return c;

        Tensor nonScalar = content.getNonScalar();
        if (nonScalar == null)
            nonScalar = Complex.ONE;

        Tensor abbr;
        if (abbreviateScalarsSeparately) {
            ProductBuilder pb = new ProductBuilder();
            for (Tensor sc : scalars) {
                if (filter.is(sc))
                    pb.put(abbreviate(sc));
                else pb.put(sc);
            }
            abbr = pb.build();
        } else
            abbr = abbreviate(multiply(scalars));

        return multiply(p.getIndexlessSubProduct(), abbr, nonScalar);
    }

    private Tensor abbreviate(Tensor c) {
        final int hashCode = c.hashCode();
        List<Abbreviation> list = abbrs.get(hashCode);
        if (list == null)
            abbrs.put(hashCode, list = new ArrayList<>());

        for (Abbreviation abbr : list) {
            Boolean compare = TensorUtils.compare1(abbr.definition, c);
            if (compare != null) {
                ++abbr.count;
                return compare ? abbr.negatedAbbreviation : abbr.abbreviation;
            }
        }

        Abbreviation abbr = nextAbbreviation(c);
        list.add(abbr);
        return abbr.abbreviation;
    }

    private Abbreviation nextAbbreviation(Tensor t) {
        int index = abbrCounter++;
        return new Abbreviation(index, t,
                simpleTensor(abbrPrefix + index, IndicesFactory.EMPTY_SIMPLE_INDICES));
    }

    public List<Abbreviation> getAbbreviations() {
        ArrayList<Abbreviation> r = new ArrayList<>();
        for (List<Abbreviation> abbr : abbrs.valueCollection())
            r.addAll(abbr);
        Collections.sort(r, TOPOLOGICAL_SORT_COMPARATOR);
        return r;
    }

    public SubstitutionTransformation abbreviationReplacements() {
        final List<Abbreviation> abbrs = getAbbreviations();
        final Expression[] subs = new Expression[abbrs.size()];
        for (int i = 0; i < abbrs.size(); i++)
            subs[i] = abbrs.get(i).asSubstitution();
        return new SubstitutionTransformation(subs, true);
    }

    public long abbreviationsSymbolCount() {
        long s = 0;
        for (List<Abbreviation> abbr : abbrs.valueCollection())
            for (Abbreviation abb : abbr)
                s += TensorUtils.symbolsCount(abb.definition);
        return s;
    }

    public static final class Abbreviation {
        public long count = 1;
        public final int index;
        public final Tensor definition, abbreviation, negatedAbbreviation;

        public Abbreviation(int index, Tensor definition, Tensor abbreviation) {
            this.index = index;
            this.definition = definition;
            this.abbreviation = abbreviation;
            this.negatedAbbreviation = negate(abbreviation);
        }

        public Expression asSubstitution() {
            return expression(abbreviation, definition);
        }

        @Override
        public String toString() {
            return "(" + count + ") " + abbreviation + " = " + definition;
        }
    }

    private static final Comparator<Abbreviation> TOPOLOGICAL_SORT_COMPARATOR = new Comparator<Abbreviation>() {
        @Override
        public int compare(Abbreviation o1, Abbreviation o2) {
            return Integer.compare(o1.index, o2.index);
        }
    };
}
