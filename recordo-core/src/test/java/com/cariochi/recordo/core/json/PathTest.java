package com.cariochi.recordo.core.json;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PathTest {

    @Test
    void testParse() {
        Path path = new Path("children[*].user.name");
        assertThat(path).isEqualTo(new Path(List.of("children", "[*]", "user", "name")));
    }

    @ParameterizedTest
    @CsvSource({
            "children[*].user.name, children[*].user, true",
            "children[0].user.name, children[*].user, true",
            "children[*].user.name, children[0].user, true",
            "children[0].user.name, children[0].user, true",
            "children[1].user.name, children[0].user, false"
    })
    void testStartWith(String path1, String path2, boolean expected) {
        assertThat(new Path(path1).startWith(new Path(path2))).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "children[*].user.name, children[*].user.name, true",
            "children[0].user.name, children[*].user.name, true",
            "children[*].user.name, children[0].user.name, true",
            "children[0].user.name, children[0].user.name, true",
            "children[1].user.name, children[0].user.name, false"
    })
    void testMatches(String path1, String path2, boolean expected) {
        assertThat(new Path(path1).mathes(new Path(path2))).isEqualTo(expected);
    }
}
