/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.utils;

import java.lang.ref.SoftReference;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class SoftReferenceWrapper<T> {
    private SoftReference<T> reference;

    public SoftReferenceWrapper() {
        this.reference = null;
    }

    public SoftReferenceWrapper(SoftReference<T> reference) {
        this.reference = reference;
    }

    public SoftReferenceWrapper(T referent) {
        this.reference = new SoftReference<T>(referent);
    }

    public SoftReference<T> getReference() {
        return reference;
    }

    public void resetReference(SoftReference<T> reference) {
        this.reference = reference;
    }

    public void resetReferent(T referent) {
        this.reference = new SoftReference<T>(referent);
    }

    public T getReferent() {
        if (reference == null)
            return null;
        return reference.get();
    }
}
