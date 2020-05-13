package com.cariochi.recordo.interceptor;

import com.cariochi.recordo.VerifyAnnotationTest;
import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.utils.Files;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyInterceptorTest {

    @Mock
    private Files files;

    private final VerifyInterceptor verifyProcessor = new VerifyInterceptor("", new JacksonConverter());

    @BeforeEach
    @SneakyThrows
    void setUp() {
        writeField(verifyProcessor, "files", files, true);
    }

    @Test
    @SneakyThrows
    void object_extensible() {
        mockExpectedFile("{\n" +
                         "   \"id\" : 1,\n" +
                         "   \"text\" : \"Test Object 1\",\n" +
                         "   \"children\" : [ {\n" +
                         "     \"id\" : 2,\n" +
                         "     \"text\" : \"Test Object 2\"\n" +
                         "   }, {\n" +
                         "     \"id\" : 3,\n" +
                         "     \"text\" : \"Test Object 3\"\n" +
                         "   } ]\n" +
                         " }\n");

        run("extensible");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    @SneakyThrows
    void object_not_extensible() {
        mockExpectedFile("{\n" +
                         "   \"id\" : 1,\n" +
                         "   \"text\" : \"Test Object 1\",\n" +
                         "   \"children\" : [ {\n" +
                         "     \"id\" : 2,\n" +
                         "     \"text\" : \"Test Object 2\"\n" +
                         "   }, {\n" +
                         "     \"id\" : 3,\n" +
                         "     \"text\" : \"Test Object 3\"\n" +
                         "   } ]\n" +
                         " }\n");

        assertThrows(AssertionError.class, () -> run("not_extensible"));

        verify(files, times(1)).writeToFile(any(), any());
    }

    @Test
    void object_included_fields() {
        mockExpectedFile("{\n" +
                         "   \"id\" : 1,\n" +
                         "   \"text\" : \"Test Object 1\",\n" +
                         "   \"children\" : [ {\n" +
                         "     \"id\" : 2,\n" +
                         "     \"text\" : \"Test Object 2\"\n" +
                         "   }, {\n" +
                         "     \"id\" : 3,\n" +
                         "     \"text\" : \"Test Object 3\"\n" +
                         "   } ]\n" +
                         " }\n");

        run("included");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    void object_excluded_fields() {
        mockExpectedFile("{\n" +
                         "   \"id\" : 1,\n" +
                         "   \"text\" : \"Test Object 1\",\n" +
                         "   \"children\" : [ {\n" +
                         "     \"id\" : 2,\n" +
                         "     \"text\" : \"Test Object 2\"\n" +
                         "   }, {\n" +
                         "     \"id\" : 3,\n" +
                         "     \"text\" : \"Test Object 3\"\n" +
                         "   } ]\n" +
                         " }\n");

        run("excluded");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    void list_extensible() {
        mockExpectedFile("[\n" +
                         "    {\n" +
                         "       \"id\" : 1,\n" +
                         "       \"text\" : \"Test Object 1\",\n" +
                         "       \"children\" : [ {\n" +
                         "         \"id\" : 2,\n" +
                         "         \"text\" : \"Test Object 2\"\n" +
                         "       }, {\n" +
                         "         \"id\" : 3,\n" +
                         "         \"text\" : \"Test Object 3\"\n" +
                         "       } ]\n" +
                         "     },\n" +
                         "     {\n" +
                         "       \"id\" : 4,\n" +
                         "       \"text\" : \"Test Object 4\",\n" +
                         "       \"children\" : [ {\n" +
                         "         \"id\" : 5,\n" +
                         "         \"text\" : \"Test Object 5\"\n" +
                         "       }, {\n" +
                         "         \"id\" : 6,\n" +
                         "         \"text\" : \"Test Object 6\"\n" +
                         "       } ]\n" +
                         "     }\n" +
                         " ]\n");

        run("list_extensible");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    void list_not_extensible() {
        mockExpectedFile("[\n" +
                         "    {\n" +
                         "       \"id\" : 1,\n" +
                         "       \"text\" : \"Test Object 1\",\n" +
                         "       \"children\" : [ {\n" +
                         "         \"id\" : 2,\n" +
                         "         \"text\" : \"Test Object 2\"\n" +
                         "       }, {\n" +
                         "         \"id\" : 3,\n" +
                         "         \"text\" : \"Test Object 3\"\n" +
                         "       } ]\n" +
                         "     },\n" +
                         "     {\n" +
                         "       \"id\" : 4,\n" +
                         "       \"text\" : \"Test Object 4\",\n" +
                         "       \"children\" : [ {\n" +
                         "         \"id\" : 5,\n" +
                         "         \"text\" : \"Test Object 5\"\n" +
                         "       }, {\n" +
                         "         \"id\" : 6,\n" +
                         "         \"text\" : \"Test Object 6\"\n" +
                         "       } ]\n" +
                         "     }\n" +
                         " ]\n");

        assertThrows(AssertionError.class, () -> run("list_not_extensible"));

        verify(files, times(1)).writeToFile(any(), any());
    }

    @Test
    void list_included_fields() {
        mockExpectedFile("[\n" +
                         "    {\n" +
                         "       \"id\" : 1,\n" +
                         "       \"text\" : \"Test Object 1\",\n" +
                         "       \"children\" : [ {\n" +
                         "         \"id\" : 2,\n" +
                         "         \"text\" : \"Test Object 2\"\n" +
                         "       }, {\n" +
                         "         \"id\" : 3,\n" +
                         "         \"text\" : \"Test Object 3\"\n" +
                         "       } ]\n" +
                         "     },\n" +
                         "     {\n" +
                         "       \"id\" : 4,\n" +
                         "       \"text\" : \"Test Object 4\",\n" +
                         "       \"children\" : [ {\n" +
                         "         \"id\" : 5,\n" +
                         "         \"text\" : \"Test Object 5\"\n" +
                         "       }, {\n" +
                         "         \"id\" : 6,\n" +
                         "         \"text\" : \"Test Object 6\"\n" +
                         "       } ]\n" +
                         "     }\n" +
                         " ]\n");

        run("list_included");

        verify(files, never()).writeToFile(any(), any());
    }


    @Test
    void list_strict_order() {
        mockExpectedFile("[ {\n" +
                         "    \"id\" : 1,\n" +
                         "    \"text\" : \"Test Object 1\",\n" +
                         "    \"strings\" : [ \"1\", \"2\", \"3\" ],\n" +
                         "    \"date\" : \"2020-01-02T00:00:00Z\",\n" +
                         "    \"children\" : [ {\n" +
                         "      \"id\" : 2,\n" +
                         "      \"text\" : \"Test Object 2\",\n" +
                         "      \"strings\" : [ \"2\", \"3\", \"4\" ],\n" +
                         "      \"date\" : \"2020-01-03T00:00:00Z\",\n" +
                         "      \"children\" : [ ]\n" +
                         "    }, {\n" +
                         "      \"id\" : 3,\n" +
                         "      \"text\" : \"Test Object 3\",\n" +
                         "      \"strings\" : [ \"3\", \"4\", \"5\" ],\n" +
                         "      \"date\" : \"2020-01-04T00:00:00Z\",\n" +
                         "      \"children\" : [ ]\n" +
                         "    }]\n" +
                         "  }, {\n" +
                         "    \"id\" : 4,\n" +
                         "    \"text\" : \"Test Object 4\",\n" +
                         "    \"strings\" : [ \"4\", \"5\", \"6\" ],\n" +
                         "    \"date\" : \"2020-01-05T00:00:00Z\",\n" +
                         "    \"children\" : [ {\n" +
                         "      \"id\" : 5,\n" +
                         "      \"text\" : \"Test Object 5\",\n" +
                         "      \"strings\" : [ \"5\", \"6\", \"7\" ],\n" +
                         "      \"date\" : \"2020-01-06T00:00:00Z\",\n" +
                         "      \"children\" : [ ]\n" +
                         "    }, {\n" +
                         "      \"id\" : 6,\n" +
                         "      \"text\" : \"Test Object 6\",\n" +
                         "      \"strings\" : [ \"6\", \"7\", \"8\" ],\n" +
                         "      \"date\" : \"2020-01-07T00:00:00Z\",\n" +
                         "      \"children\" : [ ]\n" +
                         "    } ]\n" +
                         "  } ]\n");

        assertThrows(AssertionError.class, () -> run("list_strict_order"));

        verify(files, times(1)).writeToFile(any(), any());
    }


    @SneakyThrows
    private void run(String methodName) {
        final Method method = VerifyAnnotationTest.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        final Object testInstance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
        verifyProcessor.beforeTest(testInstance, method);
        method.invoke(testInstance);
        verifyProcessor.afterTest(testInstance, method);
    }

    public void mockExpectedFile(String json) {
        when(files.readFromFile(any())).thenReturn(Optional.of(json));
    }
}
