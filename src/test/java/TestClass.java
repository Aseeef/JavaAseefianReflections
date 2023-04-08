import lombok.SneakyThrows;

import java.util.Arrays;

public class TestClass {

    public String doSomething(Object o1, Object o2) {
        return o1.toString() + o2.toString();
    }

    public String doSomething(String s1, String s2) {
        return s1 + s2;
    }

    public String doSomething(String s1, Object... os) {
        return s1 + Arrays.toString(os);
    }

    @SneakyThrows
    public static void main(String[] args) {
        throw new NoSuchFieldException("test");
    }

}
