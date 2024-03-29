package com.maveric.hr360.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class ResponseList {

    private String questionId;

    private List<Response> responses;


}
