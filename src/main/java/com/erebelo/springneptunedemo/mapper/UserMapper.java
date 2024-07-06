package com.erebelo.springneptunedemo.mapper;

import com.erebelo.springneptunedemo.domain.node.UserAddress;
import com.erebelo.springneptunedemo.domain.node.UserNode;
import com.erebelo.springneptunedemo.domain.relationship.FollowRelationship;
import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.FollowRelationshipResponse;
import com.erebelo.springneptunedemo.domain.response.UserAddressResponse;
import com.erebelo.springneptunedemo.domain.response.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.ReportingPolicy.WARN;

@Mapper(componentModel = "spring", unmappedTargetPolicy = WARN)
public interface UserMapper {

    List<UserResponse> nodeListToResponseList(List<UserNode> nodeList);

    UserResponse nodeToResponse(UserNode node);

    UserAddressResponse addressToResponse(UserAddress address);

    FollowRelationshipResponse mapFollowRelationshipToFollowRelationshipResponse(FollowRelationship relationship);

    UserNode requestToNode(UserRequest request);

}
