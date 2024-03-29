package com.maveric.hr360.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Label extends FontDetails{

    private boolean show;
    private String position;
    private String color;

}
