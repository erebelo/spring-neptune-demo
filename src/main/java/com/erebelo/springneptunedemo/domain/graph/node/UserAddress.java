package com.erebelo.springneptunedemo.domain.graph.node;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAddress {

    private String addressLine;
    private String zipCode;
    private String city;
    private String state;
    private String country;

}
