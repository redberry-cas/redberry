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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class AbbreviationsBuilder implements Transformation, Serializable {
    public static final int DEFAULT_ABBR_SIZE = 50;
    public static final String DEFAULT_ABBR_PREFIX = "abbr";

    final TIntObjectHashMap<List<Abbreviation>> abbrs = new TIntObjectHashMap<>();
    public int maxSumSize = DEFAULT_ABBR_SIZE;
    public String abbrPrefix = DEFAULT_ABBR_PREFIX;
    public boolean abbreviateScalars = true;
    public boolean abbreviateScalarsSeparately = false;
    public boolean abbreviateTopLevel = false;

    @SuppressWarnings("unchecked")
    public transient Indicator<Tensor> filter = Indicator.TRUE_INDICATOR;
    @SuppressWarnings("unchecked")
    public transient Indicator<FromChildToParentIterator> aFilter = Indicator.TRUE_INDICATOR;
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

    public Abbreviation addAbbreviation(Abbreviation oth) {
        final int hashCode = oth.definition.hashCode();
        List<Abbreviation> list = abbrs.get(hashCode);
        if (list == null)
            abbrs.put(hashCode, list = new ArrayList<>());
        for (ListIterator<Abbreviation> iterator = list.listIterator(); iterator.hasNext(); ) {
            Abbreviation abbr = iterator.next();
            Boolean compare = TensorUtils.compare1(abbr.definition, oth.definition);
            if (compare != null) {
                oth = new Abbreviation(abbr.index, compare ? negate(oth.definition) : oth.definition, oth.abbreviation);
                iterator.set(oth);
                return abbr;
            }
        }

        oth = new Abbreviation(abbrCounter++, oth.definition, oth.abbreviation);
        list.add(oth);
        return oth;
    }

    public void mergeFrom(AbbreviationsBuilder oth) {
        for (Abbreviation abbr : oth.getAbbreviations())
            addAbbreviation(abbr);
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


    @Override
    public String toString() {
        return "AbbreviationsBuilder{\n\t" +
                "abbrs=" + abbrs + "\n\t" +
                "maxSumSize=" + maxSumSize + "\n\t" +
                "abbrPrefix='" + abbrPrefix + '\'' + "\n\t" +
                "abbreviateScalars=" + abbreviateScalars + "\n\t" +
                "abbreviateScalarsSeparately=" + abbreviateScalarsSeparately + "\n\t" +
                "abbreviateTopLevel=" + abbreviateTopLevel + "\n\t" +
                "filter=" + filter + "\n\t" +
                "aFilter=" + aFilter + "\n\t" +
                "abbrCounter=" + abbrCounter + "\n" +
                '}';
    }

    private void writeObject(ObjectOutputStream oos)
            throws IOException {
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.filter = Indicator.TRUE_INDICATOR;
        this.aFilter = Indicator.TRUE_INDICATOR;
    }

    public static final class Abbreviation implements Serializable {
        public long count = 1;
        public final int index;
        public final transient Tensor definition, abbreviation, negatedAbbreviation;

        private Abbreviation(long count, int index, Tensor definition, Tensor abbreviation, Tensor negatedAbbreviation) {
            this.count = count;
            this.index = index;
            this.definition = definition;
            this.abbreviation = abbreviation;
            this.negatedAbbreviation = negatedAbbreviation;
        }

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

        private void writeObject(ObjectOutputStream oos)
                throws IOException {
            oos.defaultWriteObject();
            oos.writeObject(definition.toString());
            oos.writeObject(abbreviation.toString());
            oos.writeObject(negatedAbbreviation.toString());
        }

        private transient Tensor[] weakContainer = null;

        private void readObject(ObjectInputStream ois)
                throws ClassNotFoundException, IOException {
            // default deserialization
            ois.defaultReadObject();
            weakContainer = new Tensor[3];
            weakContainer[0] = parse((String) ois.readObject());
            weakContainer[1] = parse((String) ois.readObject());
            weakContainer[2] = parse((String) ois.readObject());
        }

        private Object readResolve() {
            final Abbreviation abbr = new Abbreviation(count, index, weakContainer[0], weakContainer[1], weakContainer[2]);
            weakContainer = null;
            return abbr;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Abbreviation that = (Abbreviation) o;

            return abbreviation.equals(that.abbreviation) && TensorUtils.equals(definition, that.definition);
        }

        @Override
        public int hashCode() {
            int result = definition.hashCode();
            result = 31 * result + abbreviation.hashCode();
            return result;
        }
    }

    private static final Comparator<Abbreviation> TOPOLOGICAL_SORT_COMPARATOR = new Comparator<Abbreviation>() {
        @Override
        public int compare(Abbreviation o1, Abbreviation o2) {
            return Integer.compare(o1.index, o2.index);
        }
    };
}
