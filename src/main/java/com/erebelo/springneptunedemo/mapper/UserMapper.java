package com.erebelo.springneptunedemo.mapper;

import com.erebelo.springneptunedemo.domain.graph.edge.FollowEdge;
import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
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

    FollowEdge requestToEdge(FollowRequest request);

    FollowResponse edgeToResponse(FollowEdge edge);

    List<UserFollowResponse> edgeListToUserFollowResponseList(List<FollowResponse> edgeList, @Context String direction);

    @Mapping(target = "user", expression = "java(direction.equalsIgnoreCase(\"IN\") ? edge.getIn() : edge.getOut())")
    UserFollowResponse edgeToUserFollowResponse(FollowResponse edge, @Context String direction);

}
