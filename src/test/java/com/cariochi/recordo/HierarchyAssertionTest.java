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
        Category hierarchy = new Category("Category 1")
                .subcategory("Subcategory 1")
                .attribute("Attribute 11").next()
                .attribute("Attribute 12").next()
                .next()
                .subcategory("Subcategory 2")
                .attribute("Attribute 21").next()
                .attribute("Attribute 22").next()
                .next();

        assertion
                .excluded("subcategories.category", "subcategories.attributes.subcategory")
                .assertAsExpected(hierarchy);
    }
}
