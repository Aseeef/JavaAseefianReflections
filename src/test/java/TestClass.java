import com.github.Aseeef.JavaAseefianReflections;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Random;

public class TestClass {

    public String doSomething(String s1, Object... os) {
        return s1 + "" + Arrays.toString(os);
    }

    public String doSomething4(String s1, Object os1, Object... os) {
        return s1 + "[" + os1.toString() + "]" + Arrays.toString(os);
    }

    public String doSomething4(String s1, Object... os) {
        return s1 + Arrays.toString(os);
    }

    public String doSomething2(String s1, int... numbers) {
        return s1 + "" + Arrays.stream(numbers).asLongStream().sum();
    }

    public String doSomething3(Object o1, String o2) {
        return o1.toString() + o2.toString();
    }

    public String doSomething3(String o1, Object o2) {
        return o1.toString() + o2.toString();
    }

    public void errorThrowingMethod() {
        throw new NullPointerException();
    }

    public static int getRandomNumber() {
        return new Random().nextInt();
    }

    @SneakyThrows
    public static void main(String[] args) {
        JavaAseefianReflections jar = JavaAseefianReflections.init();
        TestClass tc = new TestClass();
        String actual3 = jar.invokeMethod(tc, "doSomething2", "a", 2, 2, 2, 2, 10, new Integer(100));
    }

}
