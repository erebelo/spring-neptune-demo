package com.erebelo.springneptunedemo.mapper;

import static org.mapstruct.ReportingPolicy.WARN;

import com.erebelo.springneptunedemo.domain.graph.edge.FollowEdge;
import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.erebelo.springneptunedemo.domain.request.FollowRequest;
import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.edge.FollowResponse;
import com.erebelo.springneptunedemo.domain.response.edge.LazyFollowResponse;
import com.erebelo.springneptunedemo.domain.response.node.LazyUserResponse;
import com.erebelo.springneptunedemo.domain.response.node.UserResponse;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = WARN)
public interface UserMapper {

    List<UserResponse> nodeListToResponseList(List<UserNode> nodeList);
    UserResponse nodeToResponse(UserNode node);

    List<LazyFollowResponse> edgeListToLazyFollowResponseList(List<FollowEdge> edgeList, @Context String direction);
    @Mapping(target = "user", expression = "java(direction.equalsIgnoreCase(\"IN\") ? nodeToLazyResponse(edge.getIn()) : nodeToLazyResponse(edge.getOut()))")
    LazyFollowResponse edgeToLazyFollowResponse(FollowEdge edge, @Context String direction);

    FollowResponse edgeToResponse(FollowEdge edge);
    LazyUserResponse nodeToLazyResponse(UserNode node);

    UserNode requestToNode(UserRequest request);

    FollowEdge requestToEdge(FollowRequest request);

}
