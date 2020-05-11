package com.recordo.interceptor;

import com.recordo.VerifyAnnotationTest;
import com.recordo.json.JacksonConverter;
import com.recordo.utils.Files;
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
        mockExpectedFile("""
                {
                   "id" : 1,
                   "text" : "Test Object 1",
                   "children" : [ {
                     "id" : 2,
                     "text" : "Test Object 2"
                   }, {
                     "id" : 3,
                     "text" : "Test Object 3"
                   } ]
                 }
                """);

        run("extensible");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    @SneakyThrows
    void object_not_extensible() {
        mockExpectedFile("""
                {
                   "id" : 1,
                   "text" : "Test Object 1",
                   "children" : [ {
                     "id" : 2,
                     "text" : "Test Object 2"
                   }, {
                     "id" : 3,
                     "text" : "Test Object 3"
                   } ]
                 }
                 """);

        assertThrows(AssertionError.class, () -> run("not_extensible"));

        verify(files, times(1)).writeToFile(any(), any());
    }

    @Test
    void object_included_fields() {
        mockExpectedFile("""
                {
                   "id" : 1,
                   "text" : "Test Object 1",
                   "children" : [ {
                     "id" : 2,
                     "text" : "Test Object 2"
                   }, {
                     "id" : 3,
                     "text" : "Test Object 3"
                   } ]
                 }
                 """);

        run("included");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    void object_excluded_fields() {
        mockExpectedFile("""
                {
                   "id" : 1,
                   "text" : "Test Object 1",
                   "children" : [ {
                     "id" : 2,
                     "text" : "Test Object 2"
                   }, {
                     "id" : 3,
                     "text" : "Test Object 3"
                   } ]
                 }
                """);

        run("excluded");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    void list_extensible() {
        mockExpectedFile("""
                [
                    {
                       "id" : 1,
                       "text" : "Test Object 1",
                       "children" : [ {
                         "id" : 2,
                         "text" : "Test Object 2"
                       }, {
                         "id" : 3,
                         "text" : "Test Object 3"
                       } ]
                     },
                     {
                       "id" : 4,
                       "text" : "Test Object 4",
                       "children" : [ {
                         "id" : 5,
                         "text" : "Test Object 5"
                       }, {
                         "id" : 6,
                         "text" : "Test Object 6"
                       } ]
                     }                         
                 ]
                """);

        run("list_extensible");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    void list_not_extensible() {
        mockExpectedFile("""
                [
                    {
                       "id" : 1,
                       "text" : "Test Object 1",
                       "children" : [ {
                         "id" : 2,
                         "text" : "Test Object 2"
                       }, {
                         "id" : 3,
                         "text" : "Test Object 3"
                       } ]
                     },
                     {
                       "id" : 4,
                       "text" : "Test Object 4",
                       "children" : [ {
                         "id" : 5,
                         "text" : "Test Object 5"
                       }, {
                         "id" : 6,
                         "text" : "Test Object 6"
                       } ]
                     }                         
                 ]
                """);

        assertThrows(AssertionError.class, () -> run("list_not_extensible"));

        verify(files, times(1)).writeToFile(any(), any());
    }

    @Test
    void list_included_fields() {
        mockExpectedFile("""
                [
                    {
                       "id" : 1,
                       "text" : "Test Object 1",
                       "children" : [ {
                         "id" : 2,
                         "text" : "Test Object 2"
                       }, {
                         "id" : 3,
                         "text" : "Test Object 3"
                       } ]
                     },
                     {
                       "id" : 4,
                       "text" : "Test Object 4",
                       "children" : [ {
                         "id" : 5,
                         "text" : "Test Object 5"
                       }, {
                         "id" : 6,
                         "text" : "Test Object 6"
                       } ]
                     }                         
                 ]
                 """);

        run("list_included");

        verify(files, never()).writeToFile(any(), any());
    }


    @Test
    void list_strict_order() {
        mockExpectedFile("""
                [ {
                    "id" : 1,
                    "text" : "Test Object 1",
                    "strings" : [ "1", "2", "3" ],
                    "date" : "2020-01-02T00:00:00Z",
                    "children" : [ {
                      "id" : 2,
                      "text" : "Test Object 2",
                      "strings" : [ "2", "3", "4" ],
                      "date" : "2020-01-03T00:00:00Z",
                      "children" : [ ]
                    }, {
                      "id" : 3,
                      "text" : "Test Object 3",
                      "strings" : [ "3", "4", "5" ],
                      "date" : "2020-01-04T00:00:00Z",
                      "children" : [ ]
                    }]
                  }, {
                    "id" : 4,
                    "text" : "Test Object 4",
                    "strings" : [ "4", "5", "6" ],
                    "date" : "2020-01-05T00:00:00Z",
                    "children" : [ {
                      "id" : 5,
                      "text" : "Test Object 5",
                      "strings" : [ "5", "6", "7" ],
                      "date" : "2020-01-06T00:00:00Z",
                      "children" : [ ]
                    }, {
                      "id" : 6,
                      "text" : "Test Object 6",
                      "strings" : [ "6", "7", "8" ],
                      "date" : "2020-01-07T00:00:00Z",
                      "children" : [ ]
                    } ]
                  } ]
                """);

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
