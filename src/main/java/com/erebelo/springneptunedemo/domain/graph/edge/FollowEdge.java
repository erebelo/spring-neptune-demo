package com.erebelo.springneptunedemo.domain.graph.edge;

import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FollowEdge {

    private String id;
    private String status;
    private LocalDate startPeriod;
    private LocalDate endPeriod;
    private UserNode in;
    private UserNode out;

}
