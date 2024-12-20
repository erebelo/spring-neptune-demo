package com.erebelo.springneptunedemo.domain.response.edge;

import com.erebelo.springneptunedemo.domain.response.node.LazyUserResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FollowResponse {

    private String id;
    private String status;
    private LocalDate startPeriod;
    private LocalDate endPeriod;
    private LazyUserResponse in;
    private LazyUserResponse out;

}
