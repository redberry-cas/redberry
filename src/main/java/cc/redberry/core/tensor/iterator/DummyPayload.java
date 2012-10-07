package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;

/**
 * Created with IntelliJ IDEA.
 * User: dbolotin
 * Date: 07.10.12
 * Time: 21:05
 * To change this template use File | Settings | File Templates.
 */
public class DummyPayload<T extends Payload<T>> implements Payload<T> {
    @Override
    public Tensor onLeaving(StackPosition<T> stackPosition) {
        return null;
    }
}
