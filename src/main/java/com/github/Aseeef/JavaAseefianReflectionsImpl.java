package com.github.Aseeef;

import com.github.Aseeef.cache.AseefianCache;
import com.github.Aseeef.cache.CaffeinatedCache;
import com.github.Aseeef.cache.VanillaCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaAseefianReflectionsImpl implements JavaAseefianReflections {

    public final static Map<Class<?>, Class<?>> WRAPPERS = Collections.unmodifiableMap(
            new HashMap<>() {
                {
                    put(Boolean.class, Boolean.TYPE);
                    put(Character.class, Character.TYPE);
                    put(Byte.class, Byte.TYPE);
                    put(Short.class, Short.TYPE);
                    put(Integer.class, Integer.TYPE);
                    put(Long.class, Long.TYPE);
                    put(Float.class, Float.TYPE);
                    put(Double.class, Double.TYPE);
                    put(Void.class, Void.TYPE);
                }
            }
    );

    // config
    private final JARConfig config;
    // cache constructors AND methods based on their method signature to speed up reflections
    private final AseefianCache<MethodSignature, Executable> executableCache;
    // cache fields
    private final AseefianCache<FieldSignature, Field> fieldCache;

    public JavaAseefianReflectionsImpl(JARConfig config) {
        this.config = config;
        if (config.useCaffeineCache) {
            this.executableCache = new CaffeinatedCache<>(config.executableCacheSize);
            this.fieldCache = new CaffeinatedCache<>(config.fieldCacheSize);
        } else {
            this.executableCache = new VanillaCache<>(config.executableCacheSize);
            this.fieldCache = new VanillaCache<>(config.fieldCacheSize);
        }
    }

    public @NonNull Enum<?> getEnum(Class<?> clazz, String enumName) {
        if (!clazz.isEnum()) {
            throw new ReflectiveAseefianException("The class " + clazz.getName() + " is not an enum class!", ReflectiveAseefianException.ExceptionType.ILLEGAL_ARGUMENT);
        }
        try {
            return invokeStaticMethod(clazz, "valueOf", enumName);
        } catch (IllegalArgumentException ex) {
            throw new ReflectiveAseefianException("No enum constant " + enumName + " was discovered in the class " + clazz.getName() + "!", ReflectiveAseefianException.ExceptionType.FIELD_NOT_FOUND);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeMethod(Object objectInstance, String methodName, Object... parameters) {
        Class<?> clazz = objectInstance.getClass();
        MethodSignature methodSignature = new MethodSignature(clazz, methodName, fromParametersToParameterTypes(parameters));
        Method method = findMethodBySignature(methodSignature);
        if (method.isVarArgs()) {
            parameters = convertParametersFromVarLength(method, parameters);
        }
        try {
            return (T) method.invoke(objectInstance, parameters);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new ReflectiveAseefianException(ex);
        }
    }

    public <T> T invokeMethod(@NonNull Object objectInstance, Class<?> objectType, String methodName, Object... parameters) {
        // none of the elements in the parameter may be null for this to work
        for (Object o : parameters)
            assert o != null;
        Method method = getMethodByName(objectType, methodName, fromParametersToParameterTypes(parameters));
        return invokeMethod(objectInstance, method, parameters);
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeMethod(Object objectInstance, Method method, Object... parameters) {
        method.trySetAccessible();
        if (method.isVarArgs()) {
            parameters = convertParametersFromVarLength(method, parameters);
        }
        try {
            return (T) method.invoke(objectInstance, parameters);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw new ReflectiveAseefianException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeStaticMethod(Class<?> objectType, String methodName, Object... parameters) {
        Method method = getMethodByName(objectType, methodName, fromParametersToParameterTypes(parameters));
        method.trySetAccessible();
        if (method.isVarArgs()) {
            parameters = convertParametersFromVarLength(method, parameters);
        }

        try {
            return (T) method.invoke(null, parameters);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw new ReflectiveAseefianException(ex);
        }
    }

    private Method findMethodBySignature(MethodSignature methodSignature) {
        Method method = (Method) executableCache.getIfPresent(methodSignature);
        if (method != null) {
            return method;
        } else {
            ReflectiveAseefianException ex = null;

            Queue<Class<?>> classesToCheck = new ArrayDeque<>();
            classesToCheck.offer(methodSignature.clazz);
            do {
                Class<?> currentClazz = classesToCheck.poll();
                if (currentClazz == null) break;
                try {
                    if (methodSignature.methodName == null && methodSignature.methodReturnType != null) {
                        method = getMethodByReturnType(currentClazz, methodSignature.methodReturnType);
                    } else if (methodSignature.methodName != null && methodSignature.methodReturnType == null) {
                        method = getMethodByName(currentClazz, methodSignature.methodName, methodSignature.getParameterTypes());
                    } else {
                        throw new IllegalStateException(); //should never happen
                    }
                    executableCache.put(methodSignature, method);
                    break;
                } catch (ReflectiveAseefianException err) {
                    ex = err;
                    // if the current class doesn't have this
                    // method, see if the super class does
                    if (currentClazz.getSuperclass() != null) {
                        classesToCheck.offer(currentClazz.getSuperclass());
                    }
                    // or perhaps the method is a default method in an interface
                    for (Class<?> interfaceClass : currentClazz.getInterfaces()) {
                        for (Method interfaceMethod : interfaceClass.getDeclaredMethods()) {
                            if (interfaceMethod.isDefault()) {
                                classesToCheck.offer(interfaceClass);
                                break;
                            }
                        }
                    }
                }
            } while (!classesToCheck.isEmpty());

            if (method == null) throw (ex != null ? ex : new ReflectiveAseefianException("An unknown error occurred"));
            else {
                method.trySetAccessible();
                return method;
            }
        }
    }

    private static Object[] convertParametersFromVarLength(Executable executable, Object[] parameters) {
        Object arr = Array.newInstance(executable.getParameterTypes()[executable.getParameterCount() - 1].getComponentType(), parameters.length - executable.getParameterCount() + 1);
        int k = 0;
        for (int i = executable.getParameterCount() - 1; i < parameters.length; i++) {
            Array.set(arr, k, parameters[i]);
            k++;
        }
        parameters = Arrays.copyOf(parameters, executable.getParameterCount());
        parameters[executable.getParameterCount() - 1] = arr;
        return parameters;
    }

    private @NonNull Executable getExecutable(Class<?> objectType, String methodName, Class<?>... classes) {
        try {
            if (methodName.equals("*cnstr*")) {
                return objectType.getConstructor(classes);
            } else {
                return objectType.getMethod(methodName, classes);
            }
        } catch (NoSuchMethodException ex) {
            throw new ReflectiveAseefianException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @NonNull Constructor<T> getConstructor(@NonNull Class<T> objectType, Class<?>... parameterTypes) {
        return (Constructor<T>) getExecutable(new MethodSignature(objectType, "*cnstr*", parameterTypes));
    }

    public @NonNull Method getMethodByName(@NonNull Class<?> objectType, @NonNull String methodName, Class<?>... parameterTypes) {
        if (methodName.equals("*cnstr*"))
            throw new ReflectiveAseefianException("Invalid method name.");
        return (Method) getExecutable(new MethodSignature(objectType,  methodName, parameterTypes));
    }

    public @NonNull Method getMethodByReturnType(@NonNull Class<?> objectType, @NonNull Class<?> executedReturnType, Class<?>... parameterTypes) {
        return (Method) getExecutable(new MethodSignature(objectType, executedReturnType, parameterTypes));
    }

    private @NonNull Executable getExecutable(MethodSignature methodSignature) {

        Executable method = executableCache.getIfPresent(methodSignature);
        if (method != null) {
            return method;
        } else {
            if (methodSignature.methodName != null && methodSignature.methodName.equals("*cnstr*")) {
                method = findMatchingExecutable(methodSignature.clazz.getDeclaredConstructors(), methodSignature.parameterTypes);
            } else {
                method = findMatchingExecutable(methodSignature.clazz.getDeclaredMethods(), methodSignature.methodReturnType, methodSignature.methodName, methodSignature.parameterTypes);
            }
            if (method != null) executableCache.put(methodSignature, method);
        }

        if (method != null) {
            return method;
        } else {
            List<String> list = Arrays.stream(methodSignature.parameterTypes).map(o -> o == null ? "null" : o.getSimpleName()).collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            for (String l : list) {
                sb.append(l).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            throw new ReflectiveAseefianException("An error happened while invoking the method/constructor. Does a suitable candidate exist for [" + methodSignature.methodReturnType + "] " + methodSignature.clazz.getSimpleName() + "#" + methodSignature.methodName + "(" + sb + ")?!", ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);
        }
    }


    public <T> T newInstance(@NonNull Class<T> clazz, Object... parameters) {
        Constructor<T> constructor = getConstructor(clazz, fromParametersToParameterTypes(parameters));
        if (constructor.isVarArgs()) {
            parameters = convertParametersFromVarLength(constructor, parameters);
        }
        try {
            return (T) constructor.newInstance(parameters);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            throw new ReflectiveAseefianException(ex);
        }
    }

    private static Class<?>[] fromParametersToParameterTypes(Object[] parameters) {
        return Arrays.stream(parameters).map(p -> p == null ? null : p.getClass()).toArray(Class[]::new);
    }

    // specifically for searching methods, can't find constructors
    private static @Nullable Executable findMatchingExecutable(Method[] executables, @Nullable Class<?> expectedReturnType, @Nullable String methodName, Class<?>[] argClasses) {
        if (expectedReturnType == null && methodName == null) {
            return findMatchingExecutable(executables, argClasses);
        }
        Stream<Method> stream = Arrays.stream(executables);
        if (expectedReturnType != null) {
            stream = stream.filter(method -> method.getReturnType() == expectedReturnType);
        }
        if (methodName != null) {
            stream = stream.filter(method -> method.getName().equals(methodName));
        }
        return findMatchingExecutable(stream.toArray(Method[]::new), argClasses);
    }

    /**
     * Finds an {@link Executable} (which is generally a method or a constructor) that fits the type objects in args
     *
     * @param executables the method or constructor array
     * @param argClasses        the type objects which to match to
     * @return the nullable executable that was found
     */
    private static @Nullable Executable findMatchingExecutable(Executable[] executables, Class<?>[] argClasses) {

        List<Executable> executableList = Arrays.stream(executables).parallel().filter(executable -> {
            executable.trySetAccessible(); //in case private
            // if parameter count doesn't equal arg length then no match
            // UNLESS the executable has variable length (ei myMethod(String... varLenStr))
            if (executable.getParameterCount() == argClasses.length || (executable.isVarArgs() && argClasses.length > executable.getParameterCount())) {
                boolean match = true;
                Class<?>[] parameterTypes = executable.getParameterTypes();
                for (int i = 0; i < argClasses.length; i++) {
                    int index = Math.min(i, executable.getParameterCount() - 1); // need to do this because of var args (ei a parameter in parameter like method(String... varargString))
                    boolean isPrimativeWrapper = argClasses[index] != null && WRAPPERS.containsKey(argClasses[index]);
                    // only time the types shouldnt match is because one is primitive type the other is boxed
                    // OR we have a variable length string
                    if (argClasses[i] != null && (!parameterTypes[index].isAssignableFrom(argClasses[index]))) {
                        match = parameterTypes[index].isPrimitive() && isPrimativeWrapper && parameterTypes[index] == WRAPPERS.get(argClasses[index]);
                        if (executable.isVarArgs() && i >= executable.getParameterCount() - 1) {
                            match = parameterTypes[index].getComponentType().isInstance(argClasses[index]);
                        }
                    }
                    // otherwise if they don't match, then this executable just isn't a match so break
                    if (!match) return false;
                }
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        if (executableList.size() == 0)
            return null;
        else if (executableList.size() == 1) {
            return executableList.get(0);
        }
        // size > 1
        else {
            // for classes like say the NMS CraftArmorStand, there are multiple zero param "getHandle()" methods:
            // Entity getHandle
            // LivingEntity getHandle
            // CraftArmorStand getHandle
            // so this just accounts for that before through an ambigious call exception
            Executable exList = executableList.get(0);
            for (Executable executable : executableList) {
                if (!Arrays.equals(exList.getParameterTypes(), executable.getParameterTypes())) {
                    throw new IllegalStateException("Ambiguous call to method" + executableList.get(0).getName() + ": " + executableList);
                }
            }
            return executableList.get(0);
        }

    }

    public Field getFieldByTypeIndex(Class<?> clazz, Class<?> fieldType, int fieldTypeIndex) {
        Field field;
        try {
            field = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> f.getType() == fieldType)
                    .toArray(Field[]::new)[fieldTypeIndex];
        } catch (IndexOutOfBoundsException ex) {
            // no such field
            throw new ReflectiveAseefianException(ex, ReflectiveAseefianException.ExceptionType.FIELD_NOT_FOUND);
        }
        field.trySetAccessible();
        return field;
    }

    public <K, V> K setStaticField(String field, V value, Class<?> clazz) {
        return setFieldInternal(null, field, value, clazz);
    }

    public <K, V> K setField(K obj, @NonNull String field, @Nullable V value) {
        return setFieldInternal(obj, field, value, obj.getClass());
    }

    public <K, V> K setField(K obj, @NonNull String field, @Nullable V value, @NonNull Class<?> clazz) {
        return setFieldInternal(obj, field, value, clazz);
    }

    private <K, V> K setFieldInternal(K obj, @NonNull String field, @Nullable V value, @NonNull Class<?> clazz) {
        Exception ex = null;
        FieldSignature fs = new FieldSignature(clazz, field);
        Field currentField = fieldCache.getIfPresent(fs);
        // if current field is null, attempt to search through super classes
        if (currentField == null) {
            Field previousFound = null;
            while (clazz != null) {
                try {
                    currentField = clazz.getDeclaredField(field);
                    ex = null;
                    clazz = clazz.getSuperclass();

                    //todo: in java this is completely legal
                    if (previousFound != null) {
                        throw new IllegalStateException("Ambiguous field name '" + field + "'. This field name exists in both " + previousFound.getDeclaringClass() + " and its super class " + currentField.getDeclaringClass() + "!");
                    }

                } catch (NoSuchFieldException e) {
                    ex = e;
                    clazz = clazz.getSuperclass();
                }
                previousFound = currentField;
            }
        }
        if (currentField == null) {
            // print error to figure out why still null
            ex.printStackTrace();
        } else {
            try {
                currentField.trySetAccessible();
                currentField.set(obj, value);
            } catch (IllegalAccessException ex2) {
                ex2.printStackTrace();
            }
        }
        return obj;
    }

    /**
     * Gets the specified static field from the specified class. If the supplied class doesn't have the field, then
     * we will check if a super class of the object has the field. Thus, for performance reasons, it
     * may be beneficial to make sure you are supplying to correct class.
     *
     * @param field the name of the field
     * @param clazz the class that has the field
     * @return the value of the field
     */
    public <E> E getStaticField(@NonNull String field, @NonNull Class<?> clazz) {
        return getFieldInternal(null, field, clazz);
    }

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
    public <T, E> E getField(T obj, @NonNull String field, Class<?> clazz) {
        return getFieldInternal(obj, field, clazz);
    }

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
    public <T, E> E getField(T obj, @NonNull String field) {
        return getFieldInternal(obj, field, obj.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T, E> E getFieldInternal(T obj, @NonNull String fieldValue, Class<?> clazz) {
        NoSuchFieldException ex = null;
        FieldSignature fs = new FieldSignature(clazz, fieldValue);
        Field field = fieldCache.getIfPresent(fs);
        if (field == null) {
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(fieldValue);
                    fieldCache.put(fs, field);
                    break;
                } catch (NoSuchFieldException e) {
                    ex = e;
                    clazz = clazz.getSuperclass();
                }
            }
        }
        try {
            if (field != null) {
                field.trySetAccessible();
                return (E) field.get(obj);
            } else {
                throw (ex == null ? new NoSuchFieldException("Field not found!") : ex);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ReflectiveAseefianException(e);
        }
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public class MethodSignature {
        Class<?> clazz;
        @Nullable Class<?> methodReturnType;
        @Nullable String methodName;
        Class<?>[] parameterTypes;
        private MethodSignature(Class<?> clazz, @NonNull Class<?> methodReturnType, Class<?>[] parameterTypes) {
            this.clazz = clazz;
            this.methodReturnType = methodReturnType;
            this.parameterTypes = parameterTypes;
        }
        private MethodSignature(Class<?> clazz, @NonNull String methodName, Class<?>[] parameterTypes) {
            this.clazz = clazz;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
        }
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public class FieldSignature {
        @Nullable Class<?> parentClass;
        @Nullable Class<?> fieldType;
        String fieldName;
        private FieldSignature(Class<?> parentClass, @NonNull Class<?> fieldType) {
            this.parentClass = parentClass;
            this.fieldType = fieldType;
        }
        private FieldSignature(Class<?> parentClass, @NonNull String fieldName) {
            this.parentClass = parentClass;
            this.fieldName = fieldName;
        }
    }

}