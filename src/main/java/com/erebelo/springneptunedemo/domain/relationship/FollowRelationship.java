package com.erebelo.springneptunedemo.domain.relationship;

import com.erebelo.springneptunedemo.domain.node.UserNode;
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
public class FollowRelationship {

    private String id;
    private UserNode user;
    private LocalDateTime sinceAt;

}
