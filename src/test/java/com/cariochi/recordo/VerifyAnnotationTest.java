package com.cariochi.recordo;

import com.cariochi.recordo.dto.TestDto;
import com.cariochi.recordo.given.Assertion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.cariochi.recordo.dto.TestDto.dto;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Collections.shuffle;

@ExtendWith(RecordoExtension.class)
public class VerifyAnnotationTest {

    @Test
    void extensible(
            @Given("/verify_annotation_test/dto.json") Assertion<TestDto> assertion
    ) {
        assertion
                .extensible(true)
                .assertAsExpected(testDto(1));
    }

    @Test
    void not_extensible(
            @Given("/verify_annotation_test/dto.json") Assertion<TestDto> assertion
    ) {
        assertion
                .assertAsExpected(testDto(1));
    }

    @Test
    void included(
            @Given("/verify_annotation_test/short_dto.json") Assertion<TestDto> assertion
    ) {
        assertion
                .included("id", "text", "children.id", "children.text")
                .assertAsExpected(testDto(1));
    }

    @Test
    void excluded(
            @Given("/verify_annotation_test/short_dto.json") Assertion<TestDto> assertion
    ) {
        final TestDto dto1 = dto(1);
        final TestDto dto2 = dto(2).withParent(dto1);
        final TestDto dto3 = dto(3).withParent(dto2);
        final TestDto actual = dto1.withChild(dto2.withChild(dto3));
        assertion
                .excluded(
                        "strings",
                        "date",
                        "parent",
                        "children.strings",
                        "children.date",
                        "children.parent",
                        "children.children.strings",
                        "children.children.date",
                        "children.children.parent"
                )
                .assertAsExpected(actual);
    }

    @Test
    void list_extensible(
            @Given("/verify_annotation_test/list.json") Assertion<List<TestDto>> assertion
    ) {
        assertion
                .extensible(true)
                .assertAsExpected(list());
    }

    @Test
    void list_not_extensible(
            @Given("/verify_annotation_test/list.json") Assertion<List<TestDto>> assertion
    ) {
        assertion.assertAsExpected(list());
    }

    @Test
    void list_included(
            @Given("/verify_annotation_test/short_list.json") Assertion<List<TestDto>> assertion
    ) {
        assertion
                .included("id", "text", "children.id", "children.text")
                .assertAsExpected(list());
    }

    @Test
    void list_excluded(
            @Given("/verify_annotation_test/short_list.json") Assertion<List<TestDto>> assertion
    ) {
        assertion
                .excluded("strings", "date", "children.strings", "children.date", "children.children")
                .assertAsExpected(list());
    }

    @Test
    void list_strict_order(
            @Given("/verify_annotation_test/list_strict_order.json") Assertion<List<TestDto>> assertion
    ) {
        final List<TestDto> list = list();
        reverse(list.get(0).getChildren());
        assertion.assertAsExpected(list);
    }

    @Test
    void list_not_strict_order(
            @Given("/verify_annotation_test/list.json") Assertion<List<TestDto>> assertion
    ) {
        final List<TestDto> list = list();
        shuffle(list.get(0).getChildren());
        shuffle(list.get(1).getChildren());
        shuffle(list);
        assertion
                .strictOrder(false)
                .assertAsExpected(list);
    }

    @Test
    void multiple(
            @Given("/verify_annotation_test/dto.json") Assertion<TestDto> objectAssertion,
            @Given("/verify_annotation_test/list.json") Assertion<List<TestDto>> listAssertion
    ) {
        objectAssertion.assertAsExpected(testDto(1));
        listAssertion.assertAsExpected(list());
    }

    private TestDto testDto(int id) {
        return dto(id)
                .withChild(dto(id + 1))
                .withChild(dto(id + 2));
    }

    private List<TestDto> list() {
        return asList(testDto(1), testDto(4));
    }

}
