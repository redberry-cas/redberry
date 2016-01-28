package cc.redberry.groovy;

import cc.redberry.core.context.CC;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;


/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class GroovyGlobalRunListener extends RunListener {

    public GroovyGlobalRunListener() {
    }

    @Override
    public void testStarted(Description description) throws Exception {
        RedberryStatic.Reset();
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        System.out.println("Test failed with name manager seed: " + CC.getNameManager().getSeed());
    }
}