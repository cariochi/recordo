package com.cariochi.recordo;

import com.cariochi.recordo.dto.TestDto;
import com.cariochi.recordo.verify.Expected;
import com.cariochi.recordo.verify.Verify;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Collections.shuffle;

@ExtendWith(RecordoExtension.class)
public class VerifyAnnotationTest {

    @Test
    void extensible(
            @Verify(value = "/verify_annotation_test/dto.json", extensible = true) Expected<TestDto> expected
    ) {
        expected.assertEquals(dto(1));
    }

    @Test
    void not_extensible(
            @Verify("/verify_annotation_test/dto.json") Expected<TestDto> expected
    ) {
        expected.assertEquals(dto(1));
    }

    @Test
    void included(
            @Verify(
                    value = "/verify_annotation_test/short_dto.json",
                    included = {"id", "text", "children.id", "children.text"}
            ) Expected<TestDto> expected
    ) {
        expected.assertEquals(dto(1));
    }

    @Test
    void excluded(
            @Verify(
                    value = "/verify_annotation_test/short_dto.json",
                    excluded = {"strings", "date", "children.strings", "children.date", "children.children"}
            ) Expected<TestDto> expected
    ) {
        expected.assertEquals(dto(1));
    }

    @Test
    void list_extensible(
            @Verify(value = "/verify_annotation_test/list.json", extensible = true) Expected<List<TestDto>> expected
    ) {
        expected.assertEquals(list());
    }

    @Test
    void list_not_extensible(
            @Verify("/verify_annotation_test/list.json") Expected<List<TestDto>> expected
    ) {
        expected.assertEquals(list());
    }

    @Test
    void list_included(
            @Verify(value = "/verify_annotation_test/short_list.json", included = {"id", "text", "children.id",
                    "children.text"})
                    Expected<List<TestDto>> expected
    ) {
        expected.assertEquals(list());
    }

    @Test
    void list_excluded(
            @Verify(
                    value = "/verify_annotation_test/short_list.json",
                    excluded = {"strings", "date", "children.strings", "children.date", "children.children"}
            ) Expected<List<TestDto>> expected
    ) {
        expected.assertEquals(list());
    }

    @Test
    void list_strict_order(
            @Verify("/verify_annotation_test/list_strict_order.json") Expected<List<TestDto>> expected
    ) {
        final List<TestDto> list = list();
        reverse(list.get(0).getChildren());
        expected.assertEquals(list);
    }

    @Test
    void list_not_strict_order(
            @Verify(value = "/verify_annotation_test/list.json", strictOrder = false) Expected<List<TestDto>> expected
    ) {
        final List<TestDto> list = list();
        shuffle(list.get(0).getChildren());
        shuffle(list.get(1).getChildren());
        shuffle(list);
        expected.assertEquals(list);
    }

    @Test
    void multiple(
            @Verify("/verify_annotation_test/dto.json") Expected<TestDto> expectedObject,
            @Verify("/verify_annotation_test/list.json") Expected<List<TestDto>> expectedList
    ) {
        expectedObject.assertEquals(dto(1));
        expectedList.assertEquals(list());
    }

    private TestDto dto(int id) {
        return TestDto.dto(id).withChild(TestDto.dto(id + 1)).withChild(TestDto.dto(id + 2));
    }

    private List<TestDto> list() {
        return asList(dto(1), dto(4));
    }

}
