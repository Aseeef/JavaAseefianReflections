import com.github.Aseeef.JARConfig;
import com.github.Aseeef.JavaAseefianReflections;
import com.github.Aseeef.JavaAseefianReflectionsImpl;
import com.github.Aseeef.ReflectiveAseefianException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JavaAseefianReflectionsTest {

    private static JavaAseefianReflections jar;

    @BeforeAll
    static void init() {
        JARConfig config = new JARConfig();
        config.setAllowAccessingInheritedMethods(true);
        config.setAllowAccessingInheritedFields(true);
        JavaAseefianReflections jar = JavaAseefianReflections.init();
        assertNotEquals(null, jar);
        assertInstanceOf(JavaAseefianReflectionsImpl.class, jar);
        JavaAseefianReflectionsTest.jar = jar;
    }

    @Test
    void getEnum() {
        // test to make sure we can reflectively get an enum properly
        Enum<?> anEnum = jar.getEnum(SampleEnum.class, "SAMPLE3");
        assertEquals(SampleEnum.SAMPLE3, anEnum);

        // test to make sure non-existent enums throw the correct error
        ReflectiveAseefianException exception1 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.getEnum(SampleEnum.class, "NON_EXISTANT_ENUM");
        });
        assertEquals(exception1.getExceptionType(), ReflectiveAseefianException.ExceptionType.ENUM_NOT_FOUND);

        // test to make sure passing a non-enum class throws the correct error
        ReflectiveAseefianException exception2 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.getEnum(StandardCharsets.class, "UTF8");
        });
        assertEquals(exception2.getExceptionType(), ReflectiveAseefianException.ExceptionType.ILLEGAL_ARGUMENT);
    }

    @Test
    void invokeMethod() {
        List<String> arrayList = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));

        String[] expected0 = {"a", "b", "c", "d"};
        String[] actual0 = jar.invokeMethod(arrayList, "toArray", (Object) new String[0]);
        assertArrayEquals(expected0, actual0);

        Set<Integer> set = new LinkedHashSet<>();
        int randNumber = new Random().nextInt();
        jar.invokeMethod(set, "add", randNumber);
        boolean wasAdded = jar.invokeMethod(set, "contains", randNumber);
        assertTrue(wasAdded);

        TestClass tc = new TestClass();

        String expected1 = tc.doSomething("a", 2, 'c', "d");
        String actual1 = jar.invokeMethod(tc, "doSomething", "a", 2, 'c', "d");
        assertEquals(expected1, actual1);

        // Test var params, and boxed to primitive conversion
        long start1 = System.currentTimeMillis();
        String expected2 = tc.testMethodForInvoke("a", 2, 2, 2, 2, 10, new Integer(100));
        String actual2 = jar.invokeMethod(tc, "testMethodForInvoke", "a", 2, 2, 2, 2, 10, new Integer(100));
        assertEquals(expected2, actual2);
        long runtime1 = System.currentTimeMillis() - start1;

        // Test handling of null values for parameters
        long start2 = System.currentTimeMillis();
        String expected3 = tc.testMethodForInvoke(null, 2, 2, 2, 2, 10, new Integer(100));
        String actual3 = jar.invokeMethod(tc, "testMethodForInvoke", null, 2, 2, 2, 2, 10, new Integer(100));
        assertEquals(expected3, actual3);
        long runtime2 = System.currentTimeMillis() - start2;

        // Make sure cache is working
        assertTrue(runtime1 > runtime2);

        // Test primitive to boxed conversions
        double expected4 = tc.boxedParameters(1, 5.5);
        double actual4 = jar.invokeMethod(tc, "boxedParameters", 1, 5.5);
        assertEquals(expected4, actual4);

        // Test calls involving nested interface
        int expected5 = tc.stringInterfaceHash("Turtles", "are", "cool");
        int actual5 = jar.invokeMethod(tc, "stringInterfaceHash", "Turtles", "are", "cool");
        assertEquals(expected5, actual5);

        ReflectiveAseefianException error0 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeMethod(tc, "doSomething", 10, "a", "b", "c", null);
        });
        assertEquals(error0.getExceptionType(), ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);

        ReflectiveAseefianException error1 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeMethod(tc, "aNonExistentMethod");
        });
        assertEquals(error1.getExceptionType(), ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);

        ReflectiveAseefianException error2 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeMethod(tc, "errorThrowingMethod");
        });
        assertEquals(error2.getExceptionType(), ReflectiveAseefianException.ExceptionType.INVOCATION_EXCEPTION);

        // should throw error, because remove() is defined in HashSet, not LinkedHashSet
        ReflectiveAseefianException error3 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeMethod(set, LinkedHashSet.class,"remove", randNumber);
        });
        assertEquals(error3.getExceptionType(), ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);

        ReflectiveAseefianException error4 = assertThrows(ReflectiveAseefianException.class, () -> {
            //tc.doSomething4("a", null, "b", "c", null); // even java complains this is an ambitious call
            jar.invokeMethod(tc, "doSomething4", "a", null, "b", "c", null);
        });
        assertEquals(error4.getExceptionType(), ReflectiveAseefianException.ExceptionType.AMBIGUOUS_CALL);
    }

    @Test
    void invokeStaticMethod() {
        List<String> expected1 = List.of("a", "b", "c", "d");
        List<String> actual = jar.invokeStaticMethod(List.class, "of", "a", "b", "c", "d");
        assertEquals(expected1, actual);

        // the method exists but its not static
        ReflectiveAseefianException exception1 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeStaticMethod(TestClass.class, "doSomething", "hi", "bro!");
        });
        assertEquals(exception1.getExceptionType(), ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);

        // simply doesn't exist with these parameters
        ReflectiveAseefianException exception2 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeStaticMethod(String.class, "format", 100, "hello");
        });
        assertEquals(exception2.getExceptionType(), ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);
    }

    @Test
    void getMethod() throws NoSuchMethodException {

        // By return type
        Method m1 = StringBuilder.class.getMethod("toString");
        Method m2 = jar.getMethodByParamAndReturnType(StringBuilder.class, String.class);
        assertEquals(m1, m2);

        // By name


    }

    @Test @SuppressWarnings("unchecked")
    void newInstance() {
        // Test normal instantiation
        ArrayList<Integer> arrayList = jar.newInstance(ArrayList.class, List.of(1, 2, 3, 4, 5));
        assertEquals(5, arrayList.size());

        // Test error case (particularly trying to instantiate abstract class)
        ReflectiveAseefianException error1 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.newInstance(AbstractSet.class);
        });
        assertEquals(error1.getExceptionType(), ReflectiveAseefianException.ExceptionType.INSTANTIATION_EXCEPTION);

        // Test constructor with varargs parameter
        TestClass testClass = jar.newInstance(TestClass.class, "A", "var", "args", "constructor");
        assertArrayEquals(new String[]{"A", "var", "args", "constructor"}, testClass.testArgs);
    }

    @Test
    void getFields() throws NoSuchFieldException {
        // (getFieldsByType) when no such field exists
        jar.getFieldsByType(TestClass.class, ArrayList.class, true);

        // (getFieldsByType) check works with interfaces + assert the field order
        Field[] actualField = jar.getFieldsByType(TestClass.class, Collection.class, true);
        Field[] expectedField =  new Field[]{TestClass.class.getDeclaredField("doubleSet"), TestClass.class.getDeclaredField("integerList"),};
        assertArrayEquals(expectedField, actualField);

        // (getFieldsByName)
    }

    @Test
    void setStaticField() {
    }

    @Test
    void setField() {
    }

    @Test
    void testSetField() {
    }

    @Test
    void getStaticField() {
    }

    @Test
    void getField() {
    }

    @Test
    void testGetField() {
    }
}