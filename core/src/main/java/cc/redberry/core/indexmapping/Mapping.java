package cc.redberry.core.indexmapping;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Mapping {

    public static final Mapping EMPTY = new Mapping();

    Mapping() {
    }

    Mapping(IndexMappingBuffer buffer) {

    }

    public boolean isEmpty() {return false;}

    public boolean getSign() {return false;}

    public Mapping inverseStates() {return this; }

    public Mapping addSign(boolean sign) {return this; }
}
