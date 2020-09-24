package com.cariochi.recordo.dto;

import lombok.Data;

@Data
public class Attribute {

    private String name;
    private Subcategory subcategory;

    public Subcategory next() {
        return subcategory;
    }
}
