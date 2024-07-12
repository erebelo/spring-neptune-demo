package com.erebelo.springneptunedemo.mapper;

import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.erebelo.springneptunedemo.domain.graph.relationship.FollowRelationship;
import com.erebelo.springneptunedemo.domain.request.FollowRequest;
import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.FollowResponse;
import com.erebelo.springneptunedemo.domain.response.LazyUserResponse;
import com.erebelo.springneptunedemo.domain.response.UserFollowResponse;
import com.erebelo.springneptunedemo.domain.response.UserResponse;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.ReportingPolicy.WARN;

@Mapper(componentModel = "spring", unmappedTargetPolicy = WARN)
public interface UserMapper {

    List<UserResponse> nodeListToResponseList(List<UserNode> nodeList);

    UserResponse nodeToResponse(UserNode node);

    LazyUserResponse nodeToLazyResponse(UserNode node);

    UserNode requestToNode(UserRequest request);

    FollowRelationship requestToRelationship(FollowRequest request);

    FollowResponse relationshipToResponse(FollowRelationship relationship);

    List<UserFollowResponse> relationshipListToUserFollowResponseList(List<FollowResponse> relationshipList, @Context String direction);

    @Mapping(target = "user", expression = "java(direction.equalsIgnoreCase(\"IN\") ? relationship.getIn() : relationship.getOut())")
    UserFollowResponse relationshipToUserFollowResponse(FollowResponse relationship, @Context String direction);

}
