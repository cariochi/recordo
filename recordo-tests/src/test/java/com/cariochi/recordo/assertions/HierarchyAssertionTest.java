package com.cariochi.recordo.assertions;

import com.cariochi.recordo.assertions.dto.Category;
import com.cariochi.recordo.core.RecordoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;

@ExtendWith(RecordoExtension.class)
class HierarchyAssertionTest {

    @Test
    void should_assert_hierarchy() {
        assertAsJson(hierarchy())
                .excluding("subcategories.category", "subcategories.attributes.subcategory")
                .isEqualTo("/hierarchy_test/hierarchy.json");
    }

    @Test
    void should_include_full_attributes() {
        assertAsJson(hierarchy())
                .including("name", "subcategories.name", "subcategories.attributes")
                .excluding("subcategories.attributes.subcategory")
                .isEqualTo("/hierarchy_test/hierarchy_include_full_attributes.json");
    }

    @Test
    void should_include_categories_without_attributes() {
        assertAsJson(hierarchy())
                .including("name", "subcategories")
                .excluding("subcategories.category", "subcategories.attributes")
                .isEqualTo("/hierarchy_test/hierarchy_without_attributes.json");
    }

    private Category hierarchy() {
        return new Category("Category 1")
                .subcategory("Subcategory 1").attribute("Attribute 11").attribute("Attribute 12").next()
                .subcategory("Subcategory 2").attribute("Attribute 21").attribute("Attribute 22").next();
    }
}
