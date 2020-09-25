package com.cariochi.recordo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Category {

    private String name;
    private List<Subcategory> subcategories = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }

    public Subcategory subcategory(String name) {
        final Subcategory subcategory = new Subcategory();
        subcategory.setCategory(this);
        subcategory.setName(name);
        subcategory.setId(subcategories.size() + 1);
        subcategories.add(subcategory);
        return subcategory;
    }

}
