package com.github.Aseeef;

import lombok.Setter;
import lombok.experimental.Accessors;

@Setter @Accessors(chain = true)
public class JARConfig {

    protected int executableCacheSize = 10000;
    protected int fieldCacheSize = 2000;
    protected boolean useCaffeineCache = false;
    protected boolean allowAccessingInheritedFields = false;
    protected boolean allowAccessingInheritedMethods = false;
    // specifically involving super classes with different implementation for the same method
    // because reflections usually dont care about what you cast the object you just passed into
    // and this might result in unintended behavior - enabling this will speed up performance since we wont search super classes once a field is found
    protected boolean allowAmbiguousCalls = false; //todo rename to prevent ambig calls
    // whether we should search the super classes to see if this method/field is exists
    // Disable this to save performance
    protected boolean searchSuperClasses = true;

}
