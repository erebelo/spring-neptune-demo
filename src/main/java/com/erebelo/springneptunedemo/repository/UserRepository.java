package com.erebelo.springneptunedemo.repository;

import com.erebelo.springneptunedemo.domain.node.UserNode;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<UserNode> findAll();

    Optional<UserNode> findById(String id);

    UserNode insert(UserNode user);

    UserNode update(UserNode user);

    void saveRelationships(String id1, String id2);

    void removeRelationship(String id1, String id2);

    void deleteById(String id);

}
