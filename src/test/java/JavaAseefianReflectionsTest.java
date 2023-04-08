import com.github.Aseeef.JARConfig;
import com.github.Aseeef.JavaAseefianReflections;
import com.github.Aseeef.JavaAseefianReflectionsImpl;
import com.github.Aseeef.ReflectiveAseefianException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

        String expected2 = tc.doSomething2("a", 2, 2, 2, 2, 10, new Integer(100));
        String actual2 = jar.invokeMethod(tc, "doSomething2", "a", 2, 2, 2, 2, 10, new Integer(100));
        assertEquals(expected2, actual2);

        String expected3 = tc.doSomething2(null, 2, 2, 2, 2, 10, new Integer(100));
        String actual3 = jar.invokeMethod(tc, "doSomething2", null, 2, 2, 2, 2, 10, new Integer(100));
        assertEquals(expected3, actual3);

        String expected4 = tc.doSomething("a", null, "b", "c", null);
        String actual4 = jar.invokeMethod(tc, "doSomething", "a", null, "b", "c", null);
        assertEquals(expected4, actual4);

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
    }

    @Test
    void invokeStaticMethod() {
        List<String> expected1 = List.of("a", "b", "c", "d");
        List<String> actual = jar.invokeStaticMethod(List.class, "of", "a", "b", "c", "d");
        assertEquals(expected1, actual);

        ReflectiveAseefianException exception1 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeStaticMethod(TestClass.class, "doSomething", "hi", "bro!");
        });
        assertEquals(exception1.getExceptionType(), ReflectiveAseefianException.ExceptionType.AMBIGUOUS_CALL);

        ReflectiveAseefianException exception2 = assertThrows(ReflectiveAseefianException.class, () -> {
            jar.invokeStaticMethod(String.class, "format", 100, "hello");
        });
        assertEquals(exception2.getExceptionType(), ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);
    }

    @Test
    void getConstructor() {
    }

    @Test
    void getMethodByName() {
    }

    @Test
    void getMethodByReturnType() {
    }

    @Test
    void newInstance() {
    }

    @Test
    void getFieldByTypeIndex() {
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