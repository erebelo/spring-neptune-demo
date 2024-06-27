package com.erebelo.springneptunedemo.domain.node;

import com.erebelo.springneptunedemo.domain.relationship.FollowRelationship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserNode {

    private String id;
    private String username;
    private String name;
    private Set<FollowRelationship> following = new HashSet<>();
    private Set<FollowRelationship> followers = new HashSet<>();

}
