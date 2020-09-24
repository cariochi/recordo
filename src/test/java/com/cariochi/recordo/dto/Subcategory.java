package com.cariochi.recordo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Subcategory {

    private String name;
    private Category category;
    private List<Attribute> attributes = new ArrayList<>();

    public Attribute attribute(String name) {
        final Attribute attribute = new Attribute();
        attribute.setSubcategory(this);
        attribute.setName(name);
        attributes.add(attribute);
        return attribute;
    }

    public Category next() {
        return category;
    }
}
