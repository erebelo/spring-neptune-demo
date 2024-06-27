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
//@Node("User")
public class UserNode {

//    @Id
//    @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    private String username;
    private String name;

//    @Relationship(type = "FOLLOW", direction = OUTGOING)
    private Set<FollowRelationship> following = new HashSet<>();

//    @Relationship(type = "FOLLOW", direction = INCOMING)
    private Set<FollowRelationship> followers = new HashSet<>();

}
