package com.github.Aseeef;

import com.github.Aseeef.cache.AseefianCache;
import com.github.Aseeef.cache.CaffeinatedCache;
import com.github.Aseeef.cache.VanillaCache;
import lombok.*;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaAseefianReflectionsImpl implements JavaAseefianReflections {

    public final static Map<Class<?>, Class<?>> BOXED_TO_PRIMITIVE = Collections.unmodifiableMap(
            new HashMap<Class<?>, Class<?>>() {
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
    public final static Map<Class<?>, Class<?>> PRIMITIVE_TO_BOXED = Collections.unmodifiableMap(
            new HashMap<Class<?>, Class<?>>() {
                {
                    put(Boolean.TYPE, Boolean.class);
                    put(Character.TYPE, Character.class);
                    put(Byte.TYPE, Byte.class);
                    put(Short.TYPE, Short.class);
                    put(Integer.TYPE, Integer.class);
                    put(Long.TYPE, Long.class);
                    put(Float.TYPE, Float.class);
                    put(Double.TYPE, Double.class);
                    put(Void.TYPE, Void.class);
                }
            }
    );

    // config
    private final JARConfig config;
    // cache constructors AND methods based on their method signature to speed up reflections
    private final AseefianCache<MethodSignature, Executable[]> executableCache;
    // cache fields
    private final AseefianCache<FieldSignature, Field[]> fieldCache;

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
        } catch (ReflectiveAseefianException ex) {
            if (ex.getCause() instanceof InvocationTargetException) {
                throw new ReflectiveAseefianException("No enum constant " + enumName + " was discovered in the class " + clazz.getName() + "!", ReflectiveAseefianException.ExceptionType.ENUM_NOT_FOUND);
            } else {
                throw new ReflectiveAseefianException(ex.getCause(), ex.getExceptionType());
            }
        }
    }

    public <T> T invokeMethod(@NonNull Object objectInstance, @NonNull String methodName, Object... parameters) {
        Class<?> clazz = objectInstance.getClass();
        MethodSignature methodSignature = new MethodSignature(clazz, methodName, null, fromParametersToParameterTypes(parameters));
        Method method = findMethodBySignature(methodSignature);
        return invokeMethod(objectInstance, method, parameters);
    }

    public <T> T invokeMethod(@NonNull Object objectInstance, @NonNull Class<?> objectType, @NonNull String methodName, Object... parameters) {
        // none of the elements in the parameter may be null for this to work
        for (Object o : parameters)
            assert o != null;
        Method method = getMethodByNameAndParams(objectType, methodName, fromParametersToParameterTypes(parameters));
        return invokeMethod(objectInstance, method, parameters);
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeMethod(@NonNull Object objectInstance, @NonNull Method method, Object... parameters) {
        method.setAccessible(true);
        if (method.isVarArgs()) {
            parameters = convertParametersFromVarLength(method, parameters);
        }
        try {
            return (T) method.invoke(objectInstance, parameters);
        } catch (InvocationTargetException ex) {
            throw new ReflectiveAseefianException(ex.getCause(), ReflectiveAseefianException.ExceptionType.INVOCATION_EXCEPTION);
        } catch (IllegalAccessException ex) {
            throw new ReflectiveAseefianException(ex, ReflectiveAseefianException.ExceptionType.ILLEGAL_ACCESS);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeStaticMethod(Class<?> objectType, String methodName, Object... parameters) {
        Method method = getMethodByNameAndParams(objectType, methodName, fromParametersToParameterTypes(parameters));
        method.setAccessible(true);
        if (method.isVarArgs()) {
            parameters = convertParametersFromVarLength(method, parameters);
        }

        // ensure method is static
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new ReflectiveAseefianException("The found method was not static!", ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);
        }

        try {
            return (T) method.invoke(null, parameters);
        } catch (IllegalAccessException ex) {
            throw new ReflectiveAseefianException(ex, ReflectiveAseefianException.ExceptionType.ILLEGAL_ACCESS);
        } catch (InvocationTargetException ex) {
            throw new ReflectiveAseefianException(ex, ReflectiveAseefianException.ExceptionType.INVOCATION_EXCEPTION);
        }
    }

    private @NonNull Method findMethodBySignature(MethodSignature methodSignature) {

        Executable[] methods = executableCache.getIfPresent(methodSignature);
        if (methods != null) {
            return (Method) methods[0];
        } else {
            ReflectiveAseefianException ex = null;

            Queue<Class<?>> classesToCheck = new ArrayDeque<>();
            classesToCheck.offer(methodSignature.clazz);
            Method method = null;
            do {
                Class<?> currentClazz = classesToCheck.poll();
                if (currentClazz == null) break;
                try {
                    if (methodSignature.methodName == null && methodSignature.methodReturnType != null && methodSignature.parameterTypes != null) {
                        method = getMethodByReturnTypeAndParams(currentClazz, methodSignature.methodReturnType, methodSignature.parameterTypes);
                    } else if (methodSignature.methodName != null && methodSignature.parameterTypes != null && methodSignature.methodReturnType == null) {
                        method = getMethodByNameAndParams(currentClazz, methodSignature.methodName, methodSignature.parameterTypes);
                    } else {
                        // remaining case is to get method by parameters alone or with no filters
                        // should never happen yet - not implemented
                        throw new ReflectiveAseefianException("This error should never happen!", ReflectiveAseefianException.ExceptionType.ILLEGAL_STATE); //should never happen
                    }
                    executableCache.put(methodSignature, new Executable[]{method});
                    break;
                } catch (ReflectiveAseefianException err) {
                    ex = err;
                    // only catch exceptions about the method not being found.
                    // only then we try to search the super classes
                    if (err.getExceptionType() != ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND) break;
                    // if the current class doesn't have this
                    // method, see if the super class does
                    if (currentClazz.getSuperclass() != null && config.searchSuperClasses) {
                        classesToCheck.offer(currentClazz.getSuperclass());
                    }
                    // or perhaps the method is a default method in an interface?
                    // In this case we do a depth first search to find the method
                    Deque<Class<?>> interfacesToSearch = new ArrayDeque<>(Arrays.asList(currentClazz.getInterfaces()));
                    if (!interfacesToSearch.isEmpty()) {
                        do {
                            Class<?> interfaceClass = interfacesToSearch.poll();
                            for (Method interfaceMethod : interfaceClass.getDeclaredMethods()) {
                                if (interfaceMethod.isDefault()) {
                                    classesToCheck.offer(interfaceClass);
                                    break;
                                }
                            }
                            interfacesToSearch.addAll(Arrays.asList(interfaceClass.getInterfaces()));
                        } while (!interfacesToSearch.isEmpty());
                    }
                }
            } while (!classesToCheck.isEmpty());

            if (method == null) throw (ex != null ? ex : new ReflectiveAseefianException("An unknown error occurred"));
            else {
                method.setAccessible(true);
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

    public @NonNull Method getMethodByNameAndParams(@NonNull Class<?> exactObjectType, @NonNull String methodName, Class<?>... parameterTypes) {
        // validate the method name
        validateMethodName(methodName);

        // now search and return
        return (Method) getExecutables(new MethodSignature(exactObjectType, methodName, null, parameterTypes), true)[0];
    }

    private static void validateMethodName(String methodName) {
        boolean valid;
        char[] chars = methodName.toCharArray();
        valid = Character.isJavaIdentifierStart(chars[0]);
        for (int i = 1 ; i < chars.length ; i++) {
            if (!valid) break;
            if (!Character.isJavaIdentifierPart(chars[i])) {
                valid = false;
            }
        }
        if (!valid) {
            throw new ReflectiveAseefianException("Error! Specified an invalid method name!", ReflectiveAseefianException.ExceptionType.ILLEGAL_ARGUMENT);
        }
    }

    public @NonNull Method getMethodByReturnTypeAndParams(@NonNull Class<?> exactObjectType, @NonNull Class<?> methodReturnType, Class<?>... parameterTypes) {
        return (Method) getExecutables(new MethodSignature(exactObjectType, null, methodReturnType, parameterTypes), true)[0];
    }

    public @NonNull Method[] getMethodsByReturnTypeAndParams(@NonNull Class<?> exactObjectType, @NonNull Class<?> methodReturnType, Class<?>... parameterTypes) {
        return Arrays.stream(getExecutables(new MethodSignature(exactObjectType, null, methodReturnType, parameterTypes), false)).map(Method.class::cast).toArray(Method[]::new);
    }

    /**
     * Get an executable based on its signature
     * @param methodSignature - signature of the target executable
     * @param expectingOne - whether this call is expecting to return a unique executable; if true, will throw an error if more than one executables found
     * @return The matched executables
     */
    private @NonNull Executable[] getExecutables(MethodSignature methodSignature, boolean expectingOne) {

        Executable[] method = executableCache.getIfPresent(methodSignature);
        if (method != null) {
            return method;
        } else {
            Executable[] matchedExecutables;
            if (methodSignature.methodName != null && methodSignature.methodName.equals("*cnstr*")) {
                matchedExecutables = findMatchingExecutables(methodSignature.clazz.getDeclaredConstructors(), methodSignature.parameterTypes);
            } else {
                matchedExecutables = findMatchingExecutables(methodSignature.clazz.getDeclaredMethods(), methodSignature.methodReturnType, methodSignature.methodName, methodSignature.parameterTypes);
            }

            if (matchedExecutables.length == 1 || !expectingOne)  {
                executableCache.put(methodSignature, matchedExecutables);
                return matchedExecutables;
            } else if (matchedExecutables.length == 0) {
                List<String> list = Arrays.stream(methodSignature.parameterTypes).map(o -> o == null ? "null" : o.getSimpleName()).collect(Collectors.toList());
                StringBuilder sb = new StringBuilder();
                for (String l : list) {
                    sb.append(l).append(", ");
                }
                if (sb.length() >= 2)
                    sb.delete(sb.length() - 2, sb.length());

                throw new ReflectiveAseefianException("An error happened while invoking the method/constructor. Does a suitable candidate exist for [" + methodSignature.methodReturnType + "] " + methodSignature.clazz.getSimpleName() + "#" + methodSignature.methodName + "(" + sb + ")?!", ReflectiveAseefianException.ExceptionType.METHOD_NOT_FOUND);
            }
            // meaning size > 1
            else {
                // example of a valid call like this: List.of(...)
                Executable[] nonVarArgsExecutables = Arrays.stream(matchedExecutables).filter(Executable::isVarArgs).toArray(Executable[]::new);
                if (nonVarArgsExecutables.length == 1) {
                    return matchedExecutables;
                }

                // Example of an ambiguous call:
                // public void doSomething(String s, int i1, int... is);
                // public void doSomething(String s, int i1, int i2);
                // And you call doSomething("string", 1, 2)
                // Now which do we call?
                if (config.allowAmbiguousCalls) {
                    return matchedExecutables;
                }

                throw new ReflectiveAseefianException("Ambiguous call to method '" + matchedExecutables[0].getName() + "': " + Arrays.toString(matchedExecutables), ReflectiveAseefianException.ExceptionType.AMBIGUOUS_CALL);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @NonNull Constructor<T> getConstructor(@NonNull Class<T> objectType, Class<?>... parameterTypes) {
        return (Constructor<T>) getExecutables(new MethodSignature(objectType, "*cnstr*", null, parameterTypes), true)[0];
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

    private Class<?>[] fromParametersToParameterTypes(Object[] parameters) {
        return Arrays.stream(parameters).map(p -> p == null ? null : p.getClass()).toArray(Class[]::new);
    }

    // specifically for searching methods, can't find constructors
    private Executable[] findMatchingExecutables(Method[] executables, @Nullable Class<?> expectedReturnType, @Nullable String methodName, @Nullable Class<?>[] argClasses) {
        if (expectedReturnType == null && methodName == null && argClasses != null) {
            // find executable by only the parameter types
            return findMatchingExecutables(executables, argClasses);
        }
        Stream<Method> stream = Arrays.stream(executables);
        if (expectedReturnType != null) {
            stream = stream.filter(method -> method.getReturnType() == expectedReturnType);
        }
        if (methodName != null) {
            stream = stream.filter(method -> method.getName().equals(methodName));
        }
        if (argClasses != null) {
            return findMatchingExecutables(stream.toArray(Method[]::new), argClasses);
        }
        return stream.toArray(Method[]::new);
    }

    /**
     * Finds an {@link Executable} (which is generally a method or a constructor) that fits the type objects in args
     *
     * @param executables the method or constructor array
     * @param suppliedParameterTypes        the type objects which to match to
     * @return the possible empty executable that was found
     */
    private @NonNull Executable[] findMatchingExecutables(Executable[] executables, Class<?>[] suppliedParameterTypes) {

        if (executables.length == 0)
            return new Executable[0];

        return Arrays.stream(executables).filter(executable -> {
            executable.setAccessible(true); //in case private
            // since JavaAseefianReflections#invokeMethod accept varargs parameters (Object...),
            // if the first parameter of the method we are calling is then Object[], then the normal
            // case wont find the match! Easy pitfall to fall in and this warning tells people the way out
            if (executable.getParameterCount() == 1 && !executable.isVarArgs() && executable.getParameterTypes()[0].isArray() && suppliedParameterTypes.length != 1) {
                Class<?> arrayType = executable.getParameterTypes()[0].getComponentType();
                for (Class<?> c : suppliedParameterTypes) {
                    if (!arrayType.isAssignableFrom(c)) {
                        return false;
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (Class<?> c : executable.getParameterTypes()) {
                    sb.append(c.getSimpleName()).append(", ");
                }
                if (executable.getParameterTypes().length > 0) {
                    sb.delete(sb.length() - 2, sb.length());
                }
                throw new ReflectiveAseefianException("It looks like you meant to call " + executable.getDeclaringClass() + "#" + executable.getName() + "(" + sb + ")" + ". However it was unclear if a varargs or non-varargs call was desired since the passed parameters can either be interpreted as a single array type parameter, or a sequence of (in your cast) " + suppliedParameterTypes.length + " parameters. To fix this, case your array argument to Object.", ReflectiveAseefianException.ExceptionType.AMBIGUOUS_CALL);
            }
            // if parameter count doesn't equal arg length then no match
            // UNLESS the executable has variable length (ei myMethod(String... varLenStr))
            else if (executable.getParameterCount() == suppliedParameterTypes.length || (executable.isVarArgs() && suppliedParameterTypes.length > executable.getParameterCount())) {
                Class<?>[] executableParameterTypes = executable.getParameterTypes();
                for (int i = 0; i < suppliedParameterTypes.length; i++) {
                    int index = Math.min(i, executable.getParameterCount() - 1); // need to do this because of var args (ie a parameter in parameter like method(String... varargString))
                    // Class type of the parameter's arg at index
                    Class<?> suppliedParameterType = suppliedParameterTypes[i];
                    Class<?> executableParameterType = executableParameterTypes[index];

                    // if parameter of null type it's an automatic match
                    if (suppliedParameterType == null) continue;
                    // If we are matching a var arg parameter, adjustments to argClassType need to be made!
                    if (executable.isVarArgs() && i >= executable.getParameterCount() - 1) {
                        if (executable.getParameterCount() != suppliedParameterTypes.length || !suppliedParameterType.isArray())
                            executableParameterType = executableParameterType.getComponentType();
                    }
                    // handles primitive -> boxed and boxed -> primitive convertions
                    if (suppliedParameterType.isPrimitive() && !executableParameterType.isPrimitive() && BOXED_TO_PRIMITIVE.containsKey(executableParameterType)) {
                        executableParameterType = BOXED_TO_PRIMITIVE.get(executableParameterType);
                    } else if (!suppliedParameterType.isPrimitive() && executableParameterType.isPrimitive() && PRIMITIVE_TO_BOXED.containsKey(executableParameterType)) {
                        executableParameterType = PRIMITIVE_TO_BOXED.get(executableParameterType);
                    }

                    if (executableParameterType.isArray() != suppliedParameterType.isArray()) {
                        return false;
                    }
                    if (!executableParameterType.isAssignableFrom(suppliedParameterType)) {
                        return false; // only a single failed match is enough to conclude we got the wrong executable
                    }

                }
                return true;
            }
            return false;
        }).toArray(Executable[]::new);

    }

    public Field[] getFieldsByType(Class<?> clazz, Class<?> fieldType, boolean exactType) {
        FieldSignature fieldSig = new FieldSignature(clazz, fieldType);
        Field[] fields = fieldCache.getIfPresent(fieldSig);
        if (fields != null) {
            return fields;
        }
        fields = Arrays.stream(clazz.getDeclaredFields()).parallel()
                .filter(f -> {
                    if (f.getType() == fieldType)
                        return true;
                    else if (!exactType) {
                        Deque<Class<?>> interfacesToSearch = new ArrayDeque<>(Arrays.asList(f.getType().getInterfaces()));
                        if (!interfacesToSearch.isEmpty()) {
                            do {
                                Class<?> interfaceClass = interfacesToSearch.poll();
                                if (interfaceClass.equals(fieldType))
                                    return true;
                                interfacesToSearch.addAll(Arrays.asList(interfaceClass.getInterfaces()));
                            } while (!interfacesToSearch.isEmpty());
                        }
                    }
                    return false;
                })
                .toArray(Field[]::new);
        Arrays.stream(fields).forEach(f -> f.setAccessible(true));
        fieldCache.put(fieldSig, fields);
        return fields;
    }

    @Override
    public Field getFieldByType(Class<?> clazz, Class<?> fieldType, boolean exactType) {
        Field[] fields = getFieldsByType(clazz, fieldType, exactType);
        if (fields.length == 0) {
            throw new ReflectiveAseefianException("No such field exists!", ReflectiveAseefianException.ExceptionType.FIELD_NOT_FOUND);
        } else if (fields.length > 1 && !config.allowAmbiguousCalls) {
            List<String> fieldNames = Arrays.stream(fields).map(Field::getName).collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            for (String l : fieldNames) {
                sb.append(l).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            throw new ReflectiveAseefianException("More than one field found in class " + clazz.getSimpleName() + " with type " + fieldType.getSimpleName() + ": " + sb.toString(), ReflectiveAseefianException.ExceptionType.AMBIGUOUS_CALL);
        } else {
            return fields[0];
        }
    }

    private static Field modifiersField;

    @Override
    public Field getFieldByName(Class<?> clazz, String fieldName) {
        Exception ex = null;
        FieldSignature fs = new FieldSignature(clazz, fieldName);
        Field[] matchingFieldsArr = fieldCache.getIfPresent(fs);
        // note: List<Field> MUST be one or zero
        Field currentField = matchingFieldsArr == null ? null : matchingFieldsArr[0];
        // if current field is null, attempt to search through super classes
        if (currentField == null) {
            List<Field> matchingFields = new ArrayList<>();
            while (clazz != null) {
                try {
                    currentField = clazz.getDeclaredField(fieldName);
                    matchingFields.add(currentField);

                    // Remove the final modifier if this is a final static field (otherwise it cant be modified)
                    if (config.allowModifyFinalStaticFields &&
                            (currentField.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) == (Modifier.FINAL | Modifier.STATIC)) {
                        if (modifiersField == null) {
                            modifiersField = Field.class.getDeclaredField("modifiers");
                            modifiersField.setAccessible(true);
                        }
                        modifiersField.setInt(currentField, currentField.getModifiers() & ~Modifier.FINAL);
                    }

                    ex = null;
                    break;
                } catch (NoSuchFieldException e1) {
                    if (config.searchSuperClasses) {
                        ex = e1;
                        clazz = clazz.getSuperclass();
                    } else {
                        throw new ReflectiveAseefianException(ex, ReflectiveAseefianException.ExceptionType.FIELD_NOT_FOUND);
                    }
                } catch (IllegalAccessException e2) {
                    // very unlikely but I suppose possible that an accessible
                    // field with the same name exists in a super class?
                    if (config.searchSuperClasses) {
                        ex = e2;
                        clazz = clazz.getSuperclass();
                    } else {
                        throw new ReflectiveAseefianException(ex, ReflectiveAseefianException.ExceptionType.ILLEGAL_ACCESS);
                    }
                }
            }
            matchingFieldsArr = matchingFields.toArray(Field[]::new);
        }
        // if still nothing found, throw error
        if (matchingFieldsArr.length == 0) {
            throw new ReflectiveAseefianException(ex, ReflectiveAseefianException.ExceptionType.FIELD_NOT_FOUND);
        }
        // cache
        fieldCache.put(fs, matchingFieldsArr);
        // return field
        Field field = matchingFieldsArr[0];
        field.setAccessible(true);
        return field;
    }

    public void setStaticField(Class<?> clazz, String field, Object value) {
        setFieldInternal(null, field, value, clazz);
    }

    public <K, V> K setFieldValue(K obj, @NonNull String field, @Nullable V value) {
        return setFieldInternal(obj, field, value, obj.getClass());
    }

    public <K, V> K setFieldValue(K obj, @NonNull Class<?> clazz, @NonNull String field, @Nullable V value) {
        return setFieldInternal(obj, field, value, clazz);
    }

    @SneakyThrows
    private <K, V> K setFieldInternal(K obj, @NonNull String fieldName, @Nullable V value, @NonNull Class<?> clazz) {
        Field field = getFieldByName(clazz, fieldName);
        try {
            field.set(obj, value);
        } catch (IllegalAccessException ex1) {
            throw new ReflectiveAseefianException(ex1, ReflectiveAseefianException.ExceptionType.ILLEGAL_ACCESS);
        } catch (IllegalArgumentException ex2) {
            throw new ReflectiveAseefianException(ex2, ReflectiveAseefianException.ExceptionType.ILLEGAL_ARGUMENT);
        }
        return obj;
    }

    public <E> E getStaticFieldValue(@NonNull Class<?> clazz, @NonNull String field) {
        return getFieldInternalValue(null, field, clazz);
    }

    public <T, E> E getFieldValue(T obj, Class<?> clazz, @NonNull String field) {
        return getFieldInternalValue(obj, field, clazz);
    }

    public <T, E> E getFieldValue(T obj, @NonNull String field) {
        return getFieldInternalValue(obj, field, obj.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T, E> E getFieldInternalValue(T obj, @NonNull String fieldValue, Class<?> clazz) {
        try {
            Field field = getFieldByName(clazz, fieldValue);
            return (E) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new ReflectiveAseefianException(e);
        }
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public static class MethodSignature {
        Class<?> clazz;
        @Nullable Class<?> methodReturnType;
        @Nullable String methodName;
        @Nullable Class<?>[] parameterTypes;
        private MethodSignature(Class<?> clazz, @Nullable String methodName, @Nullable Class<?> methodReturnType, @Nullable Class<?>[] parameterTypes) {
            this.clazz = clazz;
            this.methodName = methodName;
            this.methodReturnType = methodReturnType;
            this.parameterTypes = parameterTypes;
        }
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public static class FieldSignature {
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