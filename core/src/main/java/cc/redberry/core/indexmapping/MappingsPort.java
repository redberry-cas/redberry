package cc.redberry.core.indexmapping;

import cc.redberry.concurrent.OutputPortUnsafe;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class MappingsPort implements OutputPortUnsafe<Mapping> {
    private final OutputPortUnsafe<IndexMappingBuffer> innerPort;

    MappingsPort(OutputPortUnsafe<IndexMappingBuffer> innerPort) {
        this.innerPort = innerPort;
    }

    @Override
    public Mapping take() {
        IndexMappingBuffer temp = innerPort.take();
        if (temp == null) return null;
        return new Mapping(temp);
    }
}
