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

}
