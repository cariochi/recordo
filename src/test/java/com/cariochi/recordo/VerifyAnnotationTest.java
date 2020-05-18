package com.cariochi.recordo;

import com.cariochi.recordo.junit5.RecordoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Collections.shuffle;

@ExtendWith(RecordoExtension.class)
public class VerifyAnnotationTest {

    private TestPojo object;
    private List<TestPojo> list;

    @Test
    @Verify(value = "object", file = "expected/custom-result.json")
    void extensible() {
        object = actualObject(1);
    }

    @Test
    @Verify(
            value = "object",
            extensible = false
    )
    void not_extensible() {
        object = actualObject(1);
    }

    @Test
    @Verify(
            value = "object",
            included = {"id", "text", "children.id", "children.text"},
            extensible = false
    )
    void included() {
        object = actualObject(1);
    }

    @Test
    @Verify(
            value = "object",
            excluded = {"strings", "date", "children.strings", "children.date", "children.children"},
            extensible = false
    )
    void excluded() {
        object = actualObject(1);
    }

    @Test
    @Verify("list")
    void list_extensible() {
        list = actualList();
    }

    @Test
    @Verify(
            value = "list",
            extensible = false
    )
    void list_not_extensible() {
        list = actualList();
        shuffle(list.get(0).getChildren());
        shuffle(list.get(1).getChildren());
        shuffle(list);
    }

    @Test
    @Verify(
            value = "list",
            included = {"id", "text", "children.id", "children.text"},
            extensible = false
    )
    void list_included() {
        list = actualList();
    }

    @Test
    @Verify(
            value = "list",
            excluded = {"strings", "date", "children.strings", "children.date", "children.children"},
            extensible = false
    )
    void list_excluded() {
        list = actualList();
    }

    @Test
    @Verify(
            value = "list",
            strictOrder = true
    )
    void list_strict_order() {
        list = actualList();
        reverse(list.get(0).getChildren());
    }

    @Test
    @Verify("object")
    @Verify("list")
    void multiple() {
        object = actualObject(1);
        list = actualList();
    }

    /**
     * "This method is for {@link com.cariochi.recordo.verify.VerifyInterceptorTest}."
     */
    @Verify("object")
    void null_object() {
        object = null;
    }

    private TestPojo actualObject(int id) {
        return TestPojo.pojo(id).withChild(TestPojo.pojo(id + 1)).withChild(TestPojo.pojo(id + 2));
    }

    private List<TestPojo> actualList() {
        return asList(actualObject(1), actualObject(4));
    }

}
