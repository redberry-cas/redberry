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
package cc.redberry.core.transformations.contractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import cc.redberry.core.tensor.SimpleTensor;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class MetricKroneckerContainerImpl implements MetricKroneckerContainer {

    final MetricKroneckerContainer parent;
    List<MetricKroneckerWrapper> container;

    MetricKroneckerContainerImpl(MetricKroneckerContainer parent) {
        container = new ArrayList<>();
        this.parent = parent;
    }

    private MetricKroneckerContainerImpl(List<MetricKroneckerWrapper> container, MetricKroneckerContainer parent) {
        this.container = container;
        this.parent = parent;
    }

    void applyTo(MetricKroneckerWrapper mk) {
        ListIterator<MetricKroneckerWrapper> it = container.listIterator();
        while (it.hasNext()) {
            MetricKroneckerWrapper _mk = it.next();
            if (mk.apply(_mk))
                it.remove(); //                return;
        }
        if (parent instanceof MetricKroneckerContainerImpl)
            ((MetricKroneckerContainerImpl) parent).applyTo(mk);
    }

    @Override
    public void add(MetricKroneckerWrapper mK) {
        applyTo(mK);
        container.add(mK);
    }

    @Override
    public SimpleTensor apply(SimpleTensor t) {
        ListIterator<MetricKroneckerWrapper> iterator = container.listIterator();
        MetricKroneckerWrapper current;
        SimpleTensor newVal, oldVal = t;
        while (iterator.hasNext()) {
            current = iterator.next();
            if ((newVal = current.apply(oldVal)) != oldVal) {
                iterator.remove();
                oldVal = newVal;
            }
        }
        newVal = parent.apply(oldVal);
        return newVal;
    }

    @Override
    public MetricKroneckerContainerImpl clone() {
        List<MetricKroneckerWrapper> newList = new ArrayList<>();
        for (MetricKroneckerWrapper mk : container)
            newList.add(mk.clone());
        return new MetricKroneckerContainerImpl(newList, parent.clone());
    }

    @Override
    public boolean equals(MetricKroneckerContainer gC) {
        if (gC instanceof RootMetricKroneckerContainer)
            return false;
        MetricKroneckerContainerImpl gc = (MetricKroneckerContainerImpl) gC;
        if (container.size() != gc.container.size())
            return false;
        Collections.sort(container);
        Collections.sort(gc.container);
        for (int i = 0; i < container.size(); ++i)
            if (!container.get(i).equals(gc.container.get(i)))
                return false;
        return parent.equals(gc.parent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MetricKroneckerWrapper mk : container)
            sb.append(mk.toString()).append(";");
        return sb.toString();
    }
}