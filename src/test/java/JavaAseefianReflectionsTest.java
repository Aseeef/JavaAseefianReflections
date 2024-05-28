import com.github.Aseeef.JARConfig;
import com.github.Aseeef.JavaAseefianReflections;
import com.github.Aseeef.JavaAseefianReflectionsImpl;
import com.github.Aseeef.ReflectiveAseefianException;
import com.github.Aseeef.cache.AseefianCache;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Executable;
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
        config.setAllowModifyFinalStaticFields(true);
        JavaAseefianReflections jar = JavaAseefianReflections.init(config);
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
        String expected2 = tc.testMethodForInvoke("a", 2, 2, 2, 2, 10, new Integer(100));
        String actual2 = jar.invokeMethod(tc, "testMethodForInvoke", "a", 2, 2, 2, 2, 10, new Integer(100));
        assertEquals(expected2, actual2);

        // Test handling of null values for parameters
        String expected3 = tc.testMethodForInvoke(null, 2);
        String actual3 = jar.invokeMethod(tc, "testMethodForInvoke", null, 2);
        assertEquals(expected3, actual3);

        // Make sure executable cache is working
        AseefianCache<JavaAseefianReflectionsImpl.MethodSignature, Executable[]> executableCache = jar.getFieldValue(jar, "executableCache");
        Class<?>[] parameterTypes = jar.invokeMethod(jar, "fromParametersToParameterTypes", (Object) new Object[]{"a", 2, 2, 2, 2, 10, new Integer(100)});
        JavaAseefianReflectionsImpl.MethodSignature sig = jar.newInstance(JavaAseefianReflectionsImpl.MethodSignature.class, tc.getClass(), "testMethodForInvoke", null, parameterTypes);
        assertNotNull(executableCache.getIfPresent(sig));

        // Test primitive to boxed conversions
        double expected4 = tc.boxedParameters(1, 5.5);
        double actual4 = jar.invokeMethod(tc, "boxedParameters", 1, 5.5);
        assertEquals(expected4, actual4);

        // Test calls involving nested interface
        int expected5 = tc.stringInterfaceHash("Turtles", "are", "cool");
        int actual5 = jar.invokeMethod(tc, "stringInterfaceHash", "Turtles", "are", "cool");
        assertEquals(expected5, actual5);

        // Same call as above to test cache
        int expected6 = tc.stringInterfaceHash("Turtles", "are", "cool");
        int actual6 = jar.invokeMethod(tc, "stringInterfaceHash", "Turtles", "are", "cool");
        assertEquals(expected6, actual6);

        // Method name exists, but parameters are wrong
        ReflectiveAseefianException error0 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeMethod(tc, "doSomething", 10, "a", "b", "c", null);
        });
        assertEquals(error0.getExceptionType(), ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);

        // Method name does not exist
        ReflectiveAseefianException error1 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeMethod(tc, "aNonExistentMethod");
        });
        assertEquals(error1.getExceptionType(), ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);

        // Method name cannot exist (invalid characters)
        ReflectiveAseefianException error2 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeMethod(tc, "!invalidMethodName!");
        });
        assertEquals(error2.getExceptionType(), ReflectiveAseefianException.ExceptionType.ILLEGAL_ARGUMENT);

        // Method exists, but the method invocation itself throws an error
        ReflectiveAseefianException error3 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeMethod(tc, "errorThrowingMethod");
        });
        assertEquals(error3.getExceptionType(), ReflectiveAseefianException.ExceptionType.INVOCATION_EXCEPTION);

        // should throw error, because remove() is defined in HashSet, not LinkedHashSet
        ReflectiveAseefianException error4 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeMethod(set, LinkedHashSet.class,"remove", randNumber);
        });
        assertEquals(error4.getExceptionType(), ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);

        // An invocation when its confusing to figure out which method was intended
        ReflectiveAseefianException error5 = assertThrows(ReflectiveAseefianException.class, () -> {
            //tc.doSomething4("a", null, "b", "c", null); // even java complains this is an ambitious call
            jar.invokeMethod(tc, "doSomething4", "a", null, "b", "c", null);
        });
        assertEquals(error5.getExceptionType(), ReflectiveAseefianException.ExceptionType.AMBIGUOUS_CALL);

        // Similar to above
        ReflectiveAseefianException error6 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeMethod(jar, "fromParametersToParameterTypes", new Object[]{"a", 2, 2, 2, 2, 10, new Integer(100)});
        });
        assertEquals(error6.getExceptionType(), ReflectiveAseefianException.ExceptionType.AMBIGUOUS_CALL);
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
    void getMethods() throws NoSuchMethodException {

        // Methods By return type and params
        int expected1 = 2;
        Method[] actual1 = jar.getMethodsByReturnTypeAndParams(TestClass.class, String.class, String.class, Object[].class);
        System.out.println(Arrays.toString(actual1));
        assertEquals(expected1, actual1.length);

        // Method by return type and params
        Method expected2 = StringBuilder.class.getMethod("toString");
        Method actual2 = jar.getMethodByReturnTypeAndParams(StringBuilder.class, String.class);
        assertEquals(expected2, actual2);

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
        assertArrayEquals(new String[]{"A", "var", "args", "constructor"}, jar.getFieldValue(testClass, "testArgs"));
    }

    @Test
    void getFields() throws NoSuchFieldException {
        // (getFieldsByType) when no such field exists
        jar.getFieldsByType(TestClass.class, ArrayList.class, true);

        // (getFieldsByType) check works with interfaces + assert the field order
        Field[] actualField1 = jar.getFieldsByType(TestClass.class, Collection.class, false);
        Field[] expectedField1 = new Field[]{TestClass.class.getDeclaredField("doubleSet"), TestClass.class.getDeclaredField("integerList"),};
        assertArrayEquals(expectedField1, actualField1);

        // (getFieldByType)
        Field actualField2 = jar.getFieldByType(TestClass.class, String[].class, true);
        Field expectedField23 = TestClass.class.getDeclaredField("testArgs");
        assertEquals(expectedField23, actualField2);

        // (getFieldByName)
        Field actualField3 = jar.getFieldByName(TestClass.class, "testArgs");
        assertEquals(expectedField23, actualField3);

        // Throw error if getFieldByType has multiple return values
        ReflectiveAseefianException error1 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.getFieldByType(TestClass.class, Collection.class, false);
        });
        assertEquals(ReflectiveAseefianException.ExceptionType.AMBIGUOUS_CALL, error1.getExceptionType());

        // Throw error if non-existant
        ReflectiveAseefianException error2 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.getFieldByType(TestClass.class, StringBuilder.class, false);
        });
        assertEquals(ReflectiveAseefianException.ExceptionType.FIELD_NOT_FOUND, error2.getExceptionType());
        ReflectiveAseefianException error3 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.getFieldByName(TestClass.class, "nonExistentField");
        });
        assertEquals(ReflectiveAseefianException.ExceptionType.FIELD_NOT_FOUND, error3.getExceptionType());

    }

    @Test
    void setAndGetStaticFieldValue() {
    }

    @Test @SneakyThrows
    void setAndGetFieldValue() {
        // Test to ensure you can modify a final field
        TestClass tc = jar.newInstance(TestClass.class, "Original", "String", "Array");
        String[] modifiedArray = new String[]{"Modified", "String", "Array"};
        jar.setFieldValue(tc, "testArgs", modifiedArray);
        assertArrayEquals(modifiedArray, jar.getFieldValue(tc, "testArgs"));

        // Test to ensure super class fields are findable and changeable
        assertEquals(42, (Integer) jar.getFieldValue(tc, "meaningOfLife"));
        jar.setFieldValue(tc, "meaningOfLife", 43);
        assertEquals(43, (Integer) jar.getFieldValue(tc, "meaningOfLife"));

        // Test to ensure super class static fields are findable and changeable
        // and also checking to ensure static final fields can be changed
        assertEquals(21, (Integer) jar.getStaticFieldValue(tc.getClass(), "theAnswerTo9Plus10"));
        jar.setStaticField(tc.getClass(), "theAnswerTo9Plus10", 19);
        assertEquals(19, (Integer) jar.getStaticFieldValue(tc.getClass(), "theAnswerTo9Plus10"));

        // Test to ensure you can modify a field if you specify class too
        assertEquals("Hi", jar.getFieldValue(tc, SuperTestClass.class, "initializeMe"));
        jar.setFieldValue(tc, SuperTestClass.class, "initializeMe", "sup dude");
        assertEquals("sup dude", jar.getFieldValue(tc, SuperTestClass.class, "initializeMe"));
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