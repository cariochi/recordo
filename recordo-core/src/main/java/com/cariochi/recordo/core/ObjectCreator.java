package com.cariochi.recordo.core;

import com.cariochi.reflecto.types.ReflectoType;
import org.junit.jupiter.api.extension.ExtensionContext;

public interface ObjectCreator {

    boolean isSupported(ReflectoType type);

    <T> T create(ReflectoType type, ExtensionContext context);

}
