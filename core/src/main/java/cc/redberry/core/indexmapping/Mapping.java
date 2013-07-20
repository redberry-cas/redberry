package cc.redberry.core.indexmapping;

import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.utils.ArraysUtils;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.lang.ref.SoftReference;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Mapping {
    final int[] from, to;
    final boolean sign;

    public static final Mapping EMPTY = new Mapping();

    Mapping() {
        this.from = this.to = new int[0];
        this.sign = false;
    }

    public Mapping(int[] to, int[] from) {
        this(from, to, false);
    }

    public Mapping(int[] from, int[] to, boolean sign) {
        this.from = from.clone();
        this.to = to.clone();
        ArraysUtils.quickSort(this.from, this.to);
        this.sign = sign;
    }

    Mapping(IndexMappingBuffer buffer) {
        TIntObjectHashMap<IndexMappingBufferRecord> map = buffer.getMap();
        this.from = new int[map.size()];
        this.to = new int[map.size()];
        TIntObjectIterator<IndexMappingBufferRecord> iterator = map.iterator();
        int i = 0;
        IndexMappingBufferRecord record;
        while (iterator.hasNext()) {
            iterator.advance();
            record = iterator.value();
            this.from[i] = record.getFromRawState() | iterator.key();
            this.to[i] = record.getToRawState() | record.getIndexName();
            ++i;
        }
        ArraysUtils.quickSort(this.from, this.to);
        this.sign = buffer.getSign();
    }

    private Mapping(int[] from, int[] to, boolean sign, boolean noSort) {
        this.from = from;
        this.to = to;
        this.sign = sign;
    }

    public boolean isEmpty() {return from.length == 0;}

    public boolean getSign() {return sign;}

    public Mapping addSign(boolean sign) {
        return new Mapping(from, to, sign ^ this.sign, true);
    }

    public int[] getFrom() {
        return from;
    }

    public int[] getTo() {
        return to;
    }

    public Mapping inverseStates() {
        int[] _from = new int[from.length], _to = new int[from.length];
        for (int i = 0; i < from.length; ++i) {
            _from[i] = IndicesUtils.inverseIndexState(from[i]);
            _to[i] = IndicesUtils.inverseIndexState(to[i]);
        }
        ArraysUtils.quickSort(_from, _to);
        return new Mapping(_from, _to, sign, true);
    }


    /*LAZY INITIALIZATION*/
    private SoftReference<int[][]> cachedData = null;

    private SoftReference<int[][]> ensureDataInitialized() {
        int[][] names;
        if (cachedData == null)
            names = null;
        else
            names = cachedData.get();

        if (names != null)
            return cachedData;

        names = new int[3][];

        int[] preparedTo = new int[from.length];
        names[0] = new int[from.length];
        int rawState;
        for (int i = 0; i < from.length; ++i) {
            rawState = IndicesUtils.getRawStateInt(from[i]);
            names[0][i] = rawState ^ from[i];
            preparedTo[i] = rawState ^ to[i];
        }
        ArraysUtils.quickSort(names[0], preparedTo);
        names[1] = IndicesUtils.getIndicesNames(preparedTo);
        names[2] = preparedTo;
        return new SoftReference<>(names);
    }

    public int[] getFromNames() {
        return (cachedData = ensureDataInitialized()).get()[0];
    }

    public int[] getToNames() {
        return (cachedData = ensureDataInitialized()).get()[1];
    }

    public int[] getToPrepared() {
        return (cachedData = ensureDataInitialized()).get()[2];
    }

    public int[][] getData() {
        return (cachedData = ensureDataInitialized()).get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mapping mapping = (Mapping) o;

        if (sign != mapping.sign) return false;
        if (!Arrays.equals(from, mapping.from)) return false;
        if (!Arrays.equals(to, mapping.to)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(from);
        result = 31 * result + Arrays.hashCode(to);
        result = 31 * result + (sign ? 1 : 0);
        return result;
    }
}
