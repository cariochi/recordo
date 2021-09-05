package com.cariochi.recordo.typeref;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;

@RequiredArgsConstructor
public class TypeRef<T> {

    @Getter
    private final Type type;

}
