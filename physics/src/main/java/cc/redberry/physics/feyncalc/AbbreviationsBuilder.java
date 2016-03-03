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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.Transformation;
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
    final int maxSumSize;
    final String abbrPrefix;

    public AbbreviationsBuilder(int maxSumSize, String abbrPrefix) {
        this.maxSumSize = maxSumSize;
        this.abbrPrefix = abbrPrefix;
    }

    public AbbreviationsBuilder() {
        this(DEFAULT_ABBR_SIZE, DEFAULT_ABBR_PREFIX);
    }

    @Override
    public Tensor transform(Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        out:
        while ((c = iterator.next()) != null) {
            if (!(c instanceof Sum) || c.size() > maxSumSize
                    || c.getIndices().size() != 0 || !TensorUtils.isSymbolic(c))
                continue;

            final int hashCode = c.hashCode();
            List<Abbreviation> list = abbrs.get(hashCode);
            if (list == null)
                abbrs.put(hashCode, list = new ArrayList<>());

            for (Abbreviation abbr : list) {
                Boolean compare = TensorUtils.compare1(abbr.definition, c);
                if (compare != null) {
                    ++abbr.count;
                    iterator.set(compare ? abbr.negatedAbbreviation : abbr.abbreviation);
                    continue out;
                }
            }

            Abbreviation abbr = nextAbbreviation(c);
            list.add(abbr);
            iterator.set(abbr.abbreviation);
        }
        return iterator.result();
    }

    private int abbrCounter = 0;

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

    private final Comparator<Abbreviation> TOPOLOGICAL_SORT_COMPARATOR = new Comparator<Abbreviation>() {
        @Override
        public int compare(Abbreviation o1, Abbreviation o2) {
            return Integer.compare(o1.index, o2.index);
        }
    };
}
