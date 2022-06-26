import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class testUnsafe {

    //http://ifeve.com/sun-misc-unsafe/
    @Test
    public void test1() throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        A o1 = new A(); // constructor
        System.out.println(o1.a());

        A o2 = A.class.newInstance(); // reflection
        System.out.println(o2.a());

        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);

        A o3 = (A) unsafe.allocateInstance(A.class); // unsafe
        System.out.println(o3.a());
    }
}

class A {
    private long a; // not initialized value

    public A() {
        this.a = 1; // initialization
    }

    public long a() { return this.a; }
}
