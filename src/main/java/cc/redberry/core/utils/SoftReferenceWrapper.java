package cc.redberry.core.utils;

import java.lang.ref.SoftReference;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
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
