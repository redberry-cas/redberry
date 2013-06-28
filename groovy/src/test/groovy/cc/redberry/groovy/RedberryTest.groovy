package cc.redberry.groovy

import org.junit.Test

class RedberryTest {

    @Test
    public void testOr() throws Exception {
        use(Redberry) {
            def x = 'x = y'.t,
                y = 'y = x'.t,
                z = 'z = x + y'.t,
                t = 'x*y**2'.t

            def and = x & y & z,
                or = x | y | z

            assert and >> t == 'x**3'.t
            assert or >> t == 'x**2*y'.t

            t = 'x + y + z'.t
            assert and >> t == '3*x + y'.t
            assert or >> t == '2*x + 2*y'.t
        }
    }

    @Test
    public void testGet() {
        use(Redberry) {
            def a = 'Sin[Sin[x]*Sin[y]]'.t
            assert a[0] == 'Sin[x]*Sin[y]'.t
            assert (a[0, 0, 0] == 'x'.t || a[0, 0, 0] == 'y'.t)

            a = 'a*b*c*F_mn*G_ab'.t
            assert a[0..3] == ('a*b*c'.t.toArray() as List)
            assert a[0..5] == (a.toArray() as List)
            assert a[3..5] == ('F_mn*G_ab'.t.toArray() as List)
        }
    }
}
