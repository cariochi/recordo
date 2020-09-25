package com.cariochi.recordo;

import com.cariochi.recordo.dto.Category;
import com.cariochi.recordo.given.Assertion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(RecordoExtension.class)
public class HierarchyAssertionTest {

    @Test
    void should_assert_hierarchy(
            @Given("/hierarchy_test/hierarchy.json") Assertion<Category> assertion
    ) {
        assertion
                .excluded("subcategories.category", "subcategories.attributes.subcategory")
                .assertAsExpected(hierarchy());
    }

    @Test
    void should_include_full_attributes(
            @Given("/hierarchy_test/hierarchy_include_full_attributes.json") Assertion<Category> assertion
    ) {
        assertion
                .included("name", "subcategories.name", "subcategories.attributes")
                .excluded("subcategories.attributes.subcategory")
                .assertAsExpected(hierarchy());
    }

    @Test
    void should_include_categories_without_attributes(
            @Given("/hierarchy_test/hierarchy_without_attributes.json") Assertion<Category> assertion
    ) {
        assertion
                .included("name", "subcategories")
                .excluded("subcategories.category", "subcategories.attributes")
                .assertAsExpected(hierarchy());
    }

    private Category hierarchy() {
        return new Category("Category 1")
                .subcategory("Subcategory 1").attribute("Attribute 11").attribute("Attribute 12").next()
                .subcategory("Subcategory 2").attribute("Attribute 21").attribute("Attribute 22").next();
    }
}
