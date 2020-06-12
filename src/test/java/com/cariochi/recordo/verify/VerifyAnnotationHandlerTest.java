package com.cariochi.recordo.verify;

import com.cariochi.recordo.VerifyAnnotationTest;
import com.cariochi.recordo.reflection.Fields;
import com.cariochi.recordo.utils.Files;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.replace;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyAnnotationHandlerTest {

    @Mock
    private Files files;

    @Spy
    private final VerifyAnnotationHandler interceptor = new VerifyAnnotationHandler();

    @BeforeEach
    void setUp() {
        Fields.of(interceptor).get("verifier").get("files").setValue(files);
    }

    @Test
    @SneakyThrows
    void object_extensible() {
        mockExpectedFile("{" +
                         "   'id' : 1," +
                         "   'text' : 'Test Object 1'," +
                         "   'children' : [ {" +
                         "     'id' : 2," +
                         "     'text' : 'Test Object 2'" +
                         "   }, {" +
                         "     'id' : 3," +
                         "     'text' : 'Test Object 3'" +
                         "   } ]" +
                         " }");

        run("extensible");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    @SneakyThrows
    void object_not_extensible() {
        mockWriteJsonToFile();
        mockExpectedFile("{" +
                         "   'id' : 1," +
                         "   'text' : 'Test Object 1'," +
                         "   'children' : [ {" +
                         "     'id' : 2," +
                         "     'text' : 'Test Object 2'" +
                         "   }, {" +
                         "     'id' : 3," +
                         "     'text' : 'Test Object 3'" +
                         "   } ]" +
                         " }");

        assertThrows(AssertionError.class, () -> run("not_extensible"));

        verify(files, times(1)).writeToFile(any(), any());
    }

    @Test
    void object_included_fields() {
        mockExpectedFile("{" +
                         "   'id' : 1," +
                         "   'text' : 'Test Object 1'," +
                         "   'children' : [ {" +
                         "     'id' : 2," +
                         "     'text' : 'Test Object 2'" +
                         "   }, {" +
                         "     'id' : 3," +
                         "     'text' : 'Test Object 3'" +
                         "   } ]" +
                         " }");

        run("included");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    void object_excluded_fields() {
        mockExpectedFile("{" +
                         "   'id' : 1," +
                         "   'text' : 'Test Object 1'," +
                         "   'children' : [ {" +
                         "     'id' : 2," +
                         "     'text' : 'Test Object 2'" +
                         "   }, {" +
                         "     'id' : 3," +
                         "     'text' : 'Test Object 3'" +
                         "   } ]" +
                         " }");

        run("excluded");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    void list_extensible() {
        mockExpectedFile("[" +
                         "    {" +
                         "       'id' : 1," +
                         "       'text' : 'Test Object 1'," +
                         "       'children' : [ {" +
                         "         'id' : 2," +
                         "         'text' : 'Test Object 2'" +
                         "       }, {" +
                         "         'id' : 3," +
                         "         'text' : 'Test Object 3'" +
                         "       } ]" +
                         "     }," +
                         "     {" +
                         "       'id' : 4," +
                         "       'text' : 'Test Object 4'," +
                         "       'children' : [ {" +
                         "         'id' : 5," +
                         "         'text' : 'Test Object 5'" +
                         "       }, {" +
                         "         'id' : 6," +
                         "         'text' : 'Test Object 6'" +
                         "       } ]" +
                         "     }" +
                         " ]");

        run("list_extensible");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    void list_not_extensible() {
        mockWriteJsonToFile();
        mockExpectedFile("[" +
                         "    {" +
                         "       'id' : 1," +
                         "       'text' : 'Test Object 1'," +
                         "       'children' : [ {" +
                         "         'id' : 2," +
                         "         'text' : 'Test Object 2'" +
                         "       }, {" +
                         "         'id' : 3," +
                         "         'text' : 'Test Object 3'" +
                         "       } ]" +
                         "     }," +
                         "     {" +
                         "       'id' : 4," +
                         "       'text' : 'Test Object 4'," +
                         "       'children' : [ {" +
                         "         'id' : 5," +
                         "         'text' : 'Test Object 5'" +
                         "       }, {" +
                         "         'id' : 6," +
                         "         'text' : 'Test Object 6'" +
                         "       } ]" +
                         "     }" +
                         " ]");

        assertThrows(AssertionError.class, () -> run("list_not_extensible"));

        verify(files, times(1)).writeToFile(any(), any());
    }

    @Test
    void list_included_fields() {
        mockExpectedFile("[" +
                         "    {" +
                         "       'id' : 1," +
                         "       'text' : 'Test Object 1'," +
                         "       'children' : [ {" +
                         "         'id' : 2," +
                         "         'text' : 'Test Object 2'" +
                         "       }, {" +
                         "         'id' : 3," +
                         "         'text' : 'Test Object 3'" +
                         "       } ]" +
                         "     }," +
                         "     {" +
                         "       'id' : 4," +
                         "       'text' : 'Test Object 4'," +
                         "       'children' : [ {" +
                         "         'id' : 5," +
                         "         'text' : 'Test Object 5'" +
                         "       }, {" +
                         "         'id' : 6," +
                         "         'text' : 'Test Object 6'" +
                         "       } ]" +
                         "     }" +
                         " ]");

        run("list_included");

        verify(files, never()).writeToFile(any(), any());
    }

    @Test
    void list_not_strict_order() {
        mockExpectedFile("[ {" +
                         "    'id' : 1," +
                         "    'text' : 'Test Object 1'," +
                         "    'strings' : [ '1', '2', '3' ]," +
                         "    'date' : '2020-01-02T00:00:00Z'," +
                         "    'children' : [ {" +
                         "      'id' : 2," +
                         "      'text' : 'Test Object 2'," +
                         "      'strings' : [ '2', '3', '4' ]," +
                         "      'date' : '2020-01-03T00:00:00Z'," +
                         "      'children' : [ ]" +
                         "    }, {" +
                         "      'id' : 3," +
                         "      'text' : 'Test Object 3'," +
                         "      'strings' : [ '3', '4', '5' ]," +
                         "      'date' : '2020-01-04T00:00:00Z'," +
                         "      'children' : [ ]" +
                         "    }]" +
                         "  }, {" +
                         "    'id' : 4," +
                         "    'text' : 'Test Object 4'," +
                         "    'strings' : [ '4', '5', '6' ]," +
                         "    'date' : '2020-01-05T00:00:00Z'," +
                         "    'children' : [ {" +
                         "      'id' : 5," +
                         "      'text' : 'Test Object 5'," +
                         "      'strings' : [ '5', '6', '7' ]," +
                         "      'date' : '2020-01-06T00:00:00Z'," +
                         "      'children' : [ ]" +
                         "    }, {" +
                         "      'id' : 6," +
                         "      'text' : 'Test Object 6'," +
                         "      'strings' : [ '6', '7', '8' ]," +
                         "      'date' : '2020-01-07T00:00:00Z'," +
                         "      'children' : [ ]" +
                         "    } ]" +
                         "  } ]");

        run("list_not_strict_order");

        verify(files, never()).writeToFile(any(), any());
    }


    @Test
    void list_strict_order() {
        mockWriteJsonToFile();
        mockExpectedFile("[ {" +
                         "    'id' : 1," +
                         "    'text' : 'Test Object 1'," +
                         "    'strings' : [ '1', '2', '3' ]," +
                         "    'date' : '2020-01-02T00:00:00Z'," +
                         "    'children' : [ {" +
                         "      'id' : 2," +
                         "      'text' : 'Test Object 2'," +
                         "      'strings' : [ '2', '3', '4' ]," +
                         "      'date' : '2020-01-03T00:00:00Z'," +
                         "      'children' : [ ]" +
                         "    }, {" +
                         "      'id' : 3," +
                         "      'text' : 'Test Object 3'," +
                         "      'strings' : [ '3', '4', '5' ]," +
                         "      'date' : '2020-01-04T00:00:00Z'," +
                         "      'children' : [ ]" +
                         "    }]" +
                         "  }, {" +
                         "    'id' : 4," +
                         "    'text' : 'Test Object 4'," +
                         "    'strings' : [ '4', '5', '6' ]," +
                         "    'date' : '2020-01-05T00:00:00Z'," +
                         "    'children' : [ {" +
                         "      'id' : 5," +
                         "      'text' : 'Test Object 5'," +
                         "      'strings' : [ '5', '6', '7' ]," +
                         "      'date' : '2020-01-06T00:00:00Z'," +
                         "      'children' : [ ]" +
                         "    }, {" +
                         "      'id' : 6," +
                         "      'text' : 'Test Object 6'," +
                         "      'strings' : [ '6', '7', '8' ]," +
                         "      'date' : '2020-01-07T00:00:00Z'," +
                         "      'children' : [ ]" +
                         "    } ]" +
                         "  } ]");

        assertThrows(AssertionError.class, () -> run("list_strict_order"));

        verify(files, times(1)).writeToFile(any(), any());
    }

    @Test
    @SneakyThrows
    void file_not_found() {
        mockWriteJsonToFile();
        doThrow(new FileNotFoundException()).when(files).readFromFile(any());
        assertThrows(AssertionError.class, () -> run("extensible"));
        verify(files, times(1)).writeToFile(any(), any());
    }

    @Test
    void null_object() {
        assertThrows(AssertionError.class, () -> run("null_object"));
        verify(files, never()).writeToFile(any(), any());
    }

    @SneakyThrows
    private void run(String methodName) {
        final Method method = VerifyAnnotationTest.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        final Object testInstance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
        method.invoke(testInstance);
        interceptor.afterTest(testInstance, method);
    }

    @SneakyThrows
    public void mockExpectedFile(String json) {
        doReturn(replace(json, "'", "\"")).when(files).readFromFile(any());
    }

    public void mockWriteJsonToFile() {
        doReturn(Optional.empty()).when(files).writeToFile(any(), any());
    }

}
