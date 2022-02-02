package com.cariochi.recordo.books.dto;

import lombok.*;
import lombok.experimental.FieldNameConstants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@With
public class Book {

    private Long id;
    private Author author;
    private String title;
}
