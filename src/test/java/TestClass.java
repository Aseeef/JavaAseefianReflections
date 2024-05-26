import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TestClass extends SuperTestClass {

    String[] testArgs;
    Set<Double> doubleSet;
    List<Integer> integerList;

    public TestClass(String... testArgs) {
        this.testArgs = testArgs;
    }

    public String doSomething(String s1, Object... os) {
        return s1 + "" + Arrays.toString(os);
    }

    public String doSomething4(String s1, Object os1, Object... os) {
        return s1 + "[" + os1.toString() + "]" + Arrays.toString(os);
    }

    public String doSomething4(String s1, Object... os) {
        return s1 + Arrays.toString(os);
    }

    public double boxedParameters(Integer i, Double d) {
        return i.doubleValue() + d.doubleValue();
    }

    public String testMethodForInvoke(String s1, int... numbers) {
        return s1 + "" + Arrays.stream(numbers).asLongStream().sum();
    }

    public boolean testNestedInterfaceCalls(InnerInterface i) {
        return true;
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

}
