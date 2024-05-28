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

    public @NonNull Method getMethodByName(@NonNull Class<?> objectType, @NonNull String methodName, Class<?>... parameterTypes);

    /**
     * Find the method using the return type, and parameter types of the method.
     * Unlike {@link JavaAseefianReflections#getMethodsByReturnTypeAndParams} if no match is found, an error will be thrown.
     * Unless "ambiguous calls" in {@link JARConfig} are permitted, this method will throw an error if more than one matching method is found.
     * @param objectType - the class where the method lives
     * @param methodReturnType - the method's return type
     * @param parameterTypes the parameters with method accepts
     * @return the matched method
     */
    public @NonNull Method getMethodByParamAndReturnType(@NonNull Class<?> objectType, @NonNull Class<?> methodReturnType, Class<?>... parameterTypes);

    /**
     * Get a list of methods in the order they occur in the source code using the class, return type, and parameter types of the method.
     * @param objectType - the class where the method lives
     * @param methodReturnType - the method's return type
     * @param parameterTypes the parameters with method accepts
     * @return The (possibly empty) list of matching methods.
     */
    public @NonNull Method[] getMethodsByReturnTypeAndParams(@NonNull Class<?> objectType, @NonNull Class<?> methodReturnType, Class<?>... parameterTypes);

    public <T> @NonNull Constructor<T> getConstructor(@NonNull Class<T> objectType, Class<?>... parameterTypes);

    /**
     * Creates a new instance of the given class. The method uses the supplied arguments to
     * attempt to find an appropriate constructor.
     * @param clazz - the class which to instantiate
     * @param parameters - parameters for the constructor which to invoke
     */
    public <T> T newInstance(@NonNull Class<T> clazz, Object... parameters);

    /**
     * Get all fields (including static fields) in the order in which they occur that are of the specified type
     * @param clazz - the class where the field lives
     * @param fieldType - the type of the field
     * @param exactType - is the provided class (clazz) the exact type or is it interface/superclass type for the target(s)?
     * @return An ordered array of fields that match the specified field type
     */
    public Field[] getFieldsByType(Class<?> clazz, Class<?> fieldType, boolean exactType);

    /**
     * Get the fields (possibly a static fields) with the specified type.
     * Unlike {@link JavaAseefianReflections#getFieldsByType(Class, Class, boolean)} if no match is found, an error will be thrown.
     * Unless "ambiguous calls" in {@link JARConfig} are permitted, this method will throw an error if more than one matching method is found.
     * @param clazz - the class where the field lives
     * @param fieldType - the type of the field
     * @param exactType - is the provided class (clazz) the exact type or is it interface/superclass type for the target(s)?
     * @return An ordered array of fields that match the specified field type
     */
    public Field getFieldByType(Class<?> clazz, Class<?> fieldType, boolean exactType);

    /**
     * Gets a field in a class from the field name.
     * @param clazz the class which this field is in
     * @param name the string name we are searching for
     * @return the field with this name
     */
    public Field getFieldByName(Class<?> clazz, String name) throws NoSuchFieldException;

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

    /**
     * Gets the specified static field from the specified class. If the supplied class doesn't have the field, then
     * we will check if a super class of the object has the field. Thus, for performance reasons, it
     * may be beneficial to make sure you are supplying to correct class.
     *
     * @param field the name of the field
     * @param clazz the class that has the field
     * @return the value of the field
     */
    public <E> E getStaticField(@NonNull String field, @NonNull Class<?> clazz);

    /**
     * Gets the specified field from the specified class. If the supplied class doesn't have the field, then
     * we will check if a super class of the object has the field. Thus, for performance reasons, it
     * may be beneficial to make sure you are supplying to correct class.
     *
     * @param obj   the object that has the field
     * @param field the name of the field
     * @param clazz the class that has the field
     * @return the value of the field
     */
    public <T, E> E getField(T obj, @NonNull String field, Class<?> clazz);

    /**
     * Gets the value of the specified field. If the object doesn't have the field, then
     * we will check if a super class of the object has the field. And in cases like these,
     * checking the super classes may be slow, and thus you can use {@link JavaAseefianReflections#getField(Object, String, Class)}.
     * However, thanks to caching, most likely this should not be needed.
     *
     * @param obj   the object that has the field
     * @param field the name of the field
     * @return the value of the field
     */
    public <T, E> E getField(T obj, @NonNull String field);


}
