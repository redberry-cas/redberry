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
package cc.redberry.core.utils;

import cc.redberry.core.tensor.Tensor;
import java.util.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class THashMap<K extends Tensor, V> extends AbstractMap<K, V> {

    private final HashMap<TensorWrapperWithEquals, V> map;

    public THashMap() {
        map = new HashMap<>();
    }

    public THashMap(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    public THashMap(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }

    @Override
    public V put(K key, V value) {
        return map.put(new TensorWrapperWithEquals(key), value);
    }

    @Override
    public V get(Object key) {
        if (key == null)
            return map.get(key);
        if (!(key instanceof Tensor))
            return null;
        return map.get(new TensorWrapperWithEquals((Tensor) key));
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null)
            return map.containsKey(key);
        if (!(key instanceof Tensor))
            return false;
        return map.containsKey(new TensorWrapperWithEquals((Tensor) key));
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked") final THashMap<K, V> other = (THashMap<K, V>) obj;
        return map.equals(other.map);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return super.keySet();
    }

    private final class KeySet extends AbstractSet<K> {

        final Set<TensorWrapperWithEquals> innerSet = THashMap.this.map.keySet();

        public KeySet() {
        }

        @Override
        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        private final class KeySetIterator implements Iterator<K> {

            final Iterator<TensorWrapperWithEquals> innerIterator = innerSet.iterator();

            public KeySetIterator() {
            }

            @Override
            public boolean hasNext() {
                return innerIterator.hasNext();
            }

            @SuppressWarnings("unchecked")
            @Override
            public K next() {
                return (K) innerIterator.next().tensor;
            }

            @Override
            public void remove() {
                innerIterator.remove();
            }
        }

        @Override
        public int size() {
            return innerSet.size();
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {

        final Set<Entry<TensorWrapperWithEquals, V>> innerSet = THashMap.this.map.entrySet();

        public EntrySet() {
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        private final class EntrySetIterator implements Iterator<Entry<K, V>> {

            private final Iterator<Entry<TensorWrapperWithEquals, V>> innerIterator = innerSet.iterator();

            public EntrySetIterator() {
            }

            @Override
            public boolean hasNext() {
                return innerIterator.hasNext();
            }

            @SuppressWarnings("unchecked")
            @Override
            public Entry<K, V> next() {
                Entry<TensorWrapperWithEquals, V> e = innerIterator.next();
                return new EntryImpl((K) e.getKey().tensor, e.getValue());
            }

            private class EntryImpl implements Entry<K, V> {

                final K k;
                final V v;

                public EntryImpl(K k, V v) {
                    this.k = k;
                    this.v = v;
                }

                @Override
                public K getKey() {
                    return k;
                }

                @Override
                public V getValue() {
                    return v;
                }

                @Override
                public V setValue(V value) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }

            @Override
            public void remove() {
                innerIterator.remove();
            }
        }

        @Override
        public int size() {
            return innerSet.size();
        }
    }
}
