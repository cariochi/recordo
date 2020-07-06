package com.cariochi.recordo.utils.reflection;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassLoaders {

    public void checkClassLoaded(String className) throws ClassNotFoundException {
        Class.forName(className, false, ClassLoaders.class.getClassLoader());
    }

}
