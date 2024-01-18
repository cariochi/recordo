package com.cariochi.recordo.read;

import com.cariochi.objecto.References;
import com.cariochi.recordo.core.Recordo;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FactoryWithReferencesTest {

    private final TestFactory testFactory = Recordo.create(TestFactory.class);

    @Test
    void test() {
        final Dto dto = testFactory.createDto();
        assertThat(dto.getChildren())
                .extracting(Dto::getParent)
                .containsOnly(dto);
    }

    @Data
    public static class Dto {

        private String name;

        private Dto parent;

        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private List<Dto> children;

    }

    @RecordoObjectFactory
    public interface TestFactory {

        @Read("/factoryWithReferencesTest/dto.json")
        @References("children[*].parent")
        Dto createDto();

    }

}
