package com.github.Aseeef;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface JavaAseefianReflections {

    static JavaAseefianReflections init(JARConfig config) {
        return new JavaAseefianReflectionsImpl(config);
    }

    static JavaAseefianReflections init() {
        JARConfig defaultConfig = new JARConfig();
        return new JavaAseefianReflectionsImpl(defaultConfig);
    }

    /**
     * Get an enum object from the class of the enum and the string value of the enum
     *
     * @param clazz    the class this enum belongs to
     * @param enumName the case-insensitive string id of the enum
     * @return the enum
     */
    public @NonNull Enum<?> getEnum(Class<?> clazz, String enumName);

    /**
     * Invoke a method on the specific object.
     * {@apiNote Though this method is convenient, it may be substantially slower than
     * {@link JavaAseefianReflections#invokeMethod(Object, Class, String, Object...)} in situations where
     * the object being passed in is a subclass and the method is from the superclass.}
     *
     * @param objectInstance the instance of the object on which to call the method on
     * @param methodName     the name of the method which to call
     * @param parameters     the parameters which to pass into the method
     * @return the result of the method call
     */
    public <T> T invokeMethod(Object objectInstance, String methodName, Object... parameters);

    /**
     * Invoke a method on the specific object.
     * {@apiNote Though this method is convenient, it may be substantially slower than
     * {@link JavaAseefianReflections#invokeMethod(Object, Class, String, Object...)} in situations where
     * the object being passed in is a subclass and the method is from the superclass.}
     *
     * @param objectInstance the instance of the object on which to call the method on
     * @param objectType     the class of the object upon which to invoke the method
     * @param methodName     the name of the method which to call
     * @param parameters     the parameters which to pass into the method
     * @return the result of the method call
     */
    public <T> T invokeMethod(@NonNull Object objectInstance, Class<?> objectType, String methodName, Object... parameters);

    public <T> T invokeMethod(Object objectInstance, Method method, Object... parameters);

    public <T> T invokeStaticMethod(Class<?> objectType, String methodName, Object... parameters);

    public <T> @NonNull Constructor<T> getConstructor(@NonNull Class<T> objectType, Class<?>... parameterTypes);

    public @NonNull Method getMethodByName(@NonNull Class<?> objectType, @NonNull String methodName, Class<?>... parameterTypes);

    public @NonNull Method getMethodByReturnType(@NonNull Class<?> objectType, @NonNull Class<?> executedReturnType, Class<?>... parameterTypes);

    /**
     * Creates a new instance of the given class. The method uses the supplied arguments to
     * attempt to find an appropriate constructor.
     * @param clazz - the class which to instantiate
     * @param parameters - parameters for the constructor which to invoke
     */
    public <T> T newInstance(@NonNull Class<T> clazz, Object... parameters);

    /**
     * Gets a field by type and index
     * @param clazz the class which this field is in (must be the actual class,
     *              searching superclasses/interfaces is not supported with this method at the moment
     * @param fieldType - the expected field type. Make sure to be mindful of the fact that Integer.class is not Integer.TYPE.
     * @param fieldTypeIndex - the index these field is at. For example, if this field is the second declare String,
     *                       then the index would be two (regardless of other fields). Index ids don't care about field
     *                       modifiers like static, final, etc
     * @return the field
     */
    public Field getFieldByTypeIndex(Class<?> clazz, Class<?> fieldType, int fieldTypeIndex);

    /**
     * Set the value of a static field via reflections
     *
     * @param field the string value of the field
     * @param value the value which to set the field to
     * @return simply returns the {@param obj}
     */
    public <K, V> K setStaticField(String field, V value, Class<?> clazz);

    /**
     * Set the value of a field via reflections. For performance reasons, it might be
     * slightly beneficial to use {@link JavaAseefianReflections#setField(Object, String, Object, Class)} instead
     * in some cases especially when it is the super class that has the supplied field.
     *
     * @param obj   the object whose field to set
     * @param field the string value of the field
     * @param value the value which to set the field to
     * @return simply returns the {@param obj}
     */
    public <K, V> K setField(K obj, @NonNull String field, @Nullable V value);

    /**
     * Set the value of a field via reflections
     *
     * @param obj   the object whose field to set
     * @param field the string value of the field
     * @param value the value which to set the field to
     * @param clazz the class that has this field
     * @return simply returns the {@param obj}
     */
    public <K, V> K setField(K obj, @NonNull String field, @Nullable V value, @NonNull Class<?> clazz);

    public <E> E getStaticField(@NonNull String field, @NonNull Class<?> clazz);

    public <T, E> E getField(T obj, @NonNull String field, Class<?> clazz);

    public <T, E> E getField(T obj, @NonNull String field);


}
