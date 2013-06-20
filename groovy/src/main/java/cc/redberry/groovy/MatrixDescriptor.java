package cc.redberry.groovy;

import cc.redberry.core.indices.IndexType;

/**
 * Matrix signature: index type, number of upper and lower matrix indices
 */
public class MatrixDescriptor {
    private final IndexType type;
    private final int upper, lower;

    public MatrixDescriptor(IndexType type, int upper, int lower) {
        this.type = type;
        this.upper = upper;
        this.lower = lower;
    }

    public IndexType getType() {
        return type;
    }

    public int getUpper() {
        return upper;
    }

    public int getLower() {
        return lower;
    }
}
