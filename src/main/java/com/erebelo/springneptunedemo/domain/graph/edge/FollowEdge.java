package com.erebelo.springneptunedemo.domain.graph.edge;

import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
