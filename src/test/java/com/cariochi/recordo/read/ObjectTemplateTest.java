package com.cariochi.recordo.read;

import com.cariochi.recordo.Read;
import com.cariochi.recordo.RecordoExtension;
import com.cariochi.recordo.dto.TestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.Map;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;

@ExtendWith(RecordoExtension.class)
class ObjectTemplateTest {

    private static final Instant DATE = Instant.parse("2020-01-02T00:00:00Z");

    @Read("/read/dto.template.json")
    private ObjectTemplate<TestDto> testDtoTemplate;

    @Test
    void should_create_object_from_template() {
        TestDto dto = testDtoTemplate
                .with("id", 100)
                .with("date", DATE)
                .create();
        assertAsJson(dto).isEqualTo("/read/created_from_template.json");
    }

    @Test
    void should_create_object_from_template_2() {
        TestDto dto = testDtoTemplate.createWith(Map.of(
                "id", 100,
                "date", DATE
        ));
        assertAsJson(dto).isEqualTo("/read/created_from_template.json");
    }

    @Test
    void should_create_object_from_template(
            @Read("/read/dto.template.json") ObjectTemplate<TestDto> template
    ) {
        TestDto dto = template
                .with("id", 100)
                .with("date", DATE)
                .create();
        assertAsJson(dto).isEqualTo("/read/created_from_template.json");
    }

}
