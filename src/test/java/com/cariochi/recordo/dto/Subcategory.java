package com.cariochi.recordo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Subcategory {

    private int id;
    private String name;
    private Category category;
    private List<Attribute> attributes = new ArrayList<>();

    public Subcategory attribute(String name) {
        final Attribute attribute = new Attribute();
        attribute.setSubcategory(this);
        attribute.setName(name);
        attribute.setId(id * 100 + attributes.size() + 1);
        attributes.add(attribute);
        return this;
    }

    public Category next() {
        return category;
    }
}
