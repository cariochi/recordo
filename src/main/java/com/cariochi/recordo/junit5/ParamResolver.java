package com.cariochi.recordo.junit5;

public interface ParamResolver {

    boolean supports(RecordoContext context);

    Object resolveParameter(RecordoContext context);
}
