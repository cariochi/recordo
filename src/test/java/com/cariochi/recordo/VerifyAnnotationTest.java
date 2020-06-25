package com.cariochi.recordo;

import com.cariochi.recordo.annotation.Resources;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.junit5.RecordoExtension;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Collections.shuffle;

@FieldNameConstants
@ExtendWith(RecordoExtension.class)
@Resources("/verify_annotation_test")
public class VerifyAnnotationTest {

    private TestObject object;
    private List<TestObject> list;

    @Test
    @Verify(value = "/object.json", field = Fields.object, extensible = true)
    void extensible() {
        object = actualObject(1);
    }

    @Test
    @Verify(value = "/object.json", field = Fields.object)
    void not_extensible() {
        object = actualObject(1);
    }

    @Test
    @Verify(
            value = "/short_object.json",
            field = Fields.object,
            included = {"id", "text", "children.id", "children.text"}
    )
    void included() {
        object = actualObject(1);
    }

    @Test
    @Verify(
            value = "/short_object.json",
            field = Fields.object,
            excluded = {"strings", "date", "children.strings", "children.date", "children.children"}
    )
    void excluded() {
        object = actualObject(1);
    }

    @Test
    @Verify(value = "/list.json", field = Fields.list, extensible = true)
    void list_extensible() {
        list = actualList();
    }

    @Test
    @Verify(value = "/list.json", field = Fields.list)
    void list_not_extensible() {
        list = actualList();
    }

    @Test
    @Verify(
            value = "/short_list.json",
            field = Fields.list,
            included = {"id", "text", "children.id", "children.text"}
    )
    void list_included() {
        list = actualList();
    }

    @Test
    @Verify(
            value = "/short_list.json",
            field = Fields.list,
            excluded = {"strings", "date", "children.strings", "children.date", "children.children"}
    )
    void list_excluded() {
        list = actualList();
    }

    @Test
    @Verify(value = "/list_strict_order.json", field = Fields.list)
    void list_strict_order() {
        list = actualList();
        reverse(list.get(0).getChildren());
    }

    @Test
    @Verify(value = "/list.json", field = Fields.list, strictOrder = false)
    void list_not_strict_order() {
        list = actualList();
        shuffle(list.get(0).getChildren());
        shuffle(list.get(1).getChildren());
        shuffle(list);
    }

    @Test
    @Verify(value = "/object.json", field = Fields.object)
    @Verify(value = "/list.json", field = Fields.list)
    void multiple() {
        object = actualObject(1);
        list = actualList();
    }

    private TestObject actualObject(int id) {
        return TestObject.pojo(id).withChild(TestObject.pojo(id + 1)).withChild(TestObject.pojo(id + 2));
    }

    private List<TestObject> actualList() {
        return asList(actualObject(1), actualObject(4));
    }

}
