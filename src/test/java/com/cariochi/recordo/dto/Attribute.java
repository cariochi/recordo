package com.cariochi.recordo.dto;

import lombok.Data;

@Data
public class Attribute {

    private int id;
    private String name;
    private Subcategory subcategory;

}
