package com.erebelo.springneptunedemo.domain.relationship;

import com.erebelo.springneptunedemo.domain.node.UserNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FollowRelationship2 {

    private String id;
    private UserNode in;
    private UserNode out;
    private LocalDateTime sinceAt;

}
