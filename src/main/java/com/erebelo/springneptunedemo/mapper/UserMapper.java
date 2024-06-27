package com.erebelo.springneptunedemo.mapper;

import com.erebelo.springneptunedemo.domain.node.UserNode;
import com.erebelo.springneptunedemo.domain.relationship.FollowRelationship;
import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.FollowRelationshipResponse;
import com.erebelo.springneptunedemo.domain.response.UserLazyResponse;
import com.erebelo.springneptunedemo.domain.response.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.ReportingPolicy.WARN;

@Mapper(componentModel = "spring", unmappedTargetPolicy = WARN)
public interface UserMapper {

    List<UserLazyResponse> lazyNodeListToResponseList(List<UserNode> nodeList);

    UserLazyResponse lazyNodeToResponse(UserNode node);

    UserResponse nodeToResponse(UserNode node);

    FollowRelationshipResponse mapFollowRelationshipToFollowRelationshipResponse(FollowRelationship relationship);

    UserNode requestToNode(UserRequest request);

}
