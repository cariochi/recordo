package com.cariochi.recordo;

import com.cariochi.recordo.dto.TestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;
import static com.cariochi.recordo.assertions.JsonCondition.equalAsJsonTo;
import static com.cariochi.recordo.dto.TestDto.dto;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Collections.shuffle;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(RecordoExtension.class)
public class AssertionTest {

    @Test
    void extensible() {

        assertAsJson(testDto(1))
                .extensible(true)
                .isEqualTo("/verify_annotation_test/dto.json");

        assertThat(testDto(1))
                .is(equalAsJsonTo("/verify_annotation_test/dto.json").extensible(true));

    }

    @Test
    void not_extensible() {
        assertAsJson(testDto(1))
                .isEqualTo("/verify_annotation_test/dto.json");
    }

    @Test
    void included() {
        assertAsJson(testDto(1))
                .including("id", "text", "children.id", "children.text")
                .isEqualTo("/verify_annotation_test/short_dto.json");
    }

    @Test
    void excluded() {
        assertAsJson(testDto(1))
                .excluding(
                        "strings",
                        "date",
                        "parent",
                        "children.strings",
                        "children.date",
                        "children.parent",
                        "children.children"
                )
                .isEqualTo("/verify_annotation_test/short_dto.json");
    }

    @Test
    void list_extensible() {
        assertAsJson(list())
                .extensible(true)
                .isEqualTo("/verify_annotation_test/list.json");
    }

    @Test
    void list_not_extensible() {
        assertAsJson(list())
                .isEqualTo("/verify_annotation_test/list.json");
    }

    @Test
    void list_included() {
        assertAsJson(list())
                .including("id", "text", "children.id", "children.text")
                .isEqualTo("/verify_annotation_test/short_list.json");
    }

    @Test
    void list_included_full_children() {
        assertAsJson(list())
                .including("id", "text", "children")
                .isEqualTo("/verify_annotation_test/short_list_with_full_children.json");

        assertThat(list()).is(
                equalAsJsonTo("/verify_annotation_test/short_list_with_full_children.json")
                        .including("id", "text", "children")
        );
    }

    @Test
    void list_excluded() {
        assertAsJson(list())
                .excluding("strings", "date", "children.strings", "children.date", "children.children")
                .isEqualTo("/verify_annotation_test/short_list.json");
    }

    @Test
    void list_strict_order() {
        final List<TestDto> list = list();
        reverse(list.get(0).getChildren());

        assertAsJson(list)
                .isEqualTo("/verify_annotation_test/list_strict_order.json");
    }

    @Test
    void list_not_strict_order() {
        final List<TestDto> list = list();
        shuffle(list.get(0).getChildren());
        shuffle(list.get(1).getChildren());
        shuffle(list);

        assertAsJson(list)
                .withStrictOrder(false)
                .isEqualTo("/verify_annotation_test/list.json");
    }

    @Test
    void multiple() {
        assertAsJson(testDto(1))
                .isEqualTo("/verify_annotation_test/dto.json");

        assertAsJson(list())
                .isEqualTo("/verify_annotation_test/list.json");
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
