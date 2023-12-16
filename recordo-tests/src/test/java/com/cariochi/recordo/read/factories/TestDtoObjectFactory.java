package com.cariochi.recordo.read.factories;

import com.cariochi.recordo.main.dto.TestDto;
import com.cariochi.recordo.read.Param;
import com.cariochi.recordo.read.Read;
import com.cariochi.recordo.read.RecordoObjectFactory;
import java.util.List;

@RecordoObjectFactory
public interface TestDtoObjectFactory {

    @Read("/read/dto.json")
    TestDto testDto();

    default TestDto defaultTestDto() {
        return TestDto.builder()
                .id(101)
                .text("DEFAULT")
                .build();
    }

    default List<TestDto> defaultTestDtoList() {
        return List.of(defaultTestDto());
    }

    @Read("/read/dto-list.json")
    List<TestDto> testDtoList();

    @Read("/read/dto.json")
    TestDto testDto(@Param("id") Integer id);

    TestDtoObjectFactory text(@Param("text") String text);

    TestDtoObjectFactory allChildrenStrings(@Param("children[*].strings[*]") String value);

}
