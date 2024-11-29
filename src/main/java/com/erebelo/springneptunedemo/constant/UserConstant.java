package com.erebelo.springneptunedemo.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserConstant {

    public static final String USER_VERTEX_LABEL = "User";
    public static final String FOLLOW_EDGE_LABEL = "FOLLOW";
    public static final String USERNAME_PROPERTY = "username";
    public static final String NAME_PROPERTY = "name";
    public static final String ADDRESS_STATE_PROPERTY = "address_state";
    public static final String REGEX_LIKE_CASE_INSENSITIVE = "(?i)";
    public static final String REGEX_CASE_INSENSITIVE = "^(?i)%s$";

    public static final String USERS_NOT_FOUND_ERROR_MESSAGE = "Users not found";
    public static final String USER_NOT_FOUND_ERROR_MESSAGE = "User not found by id: ";
    public static final String USER_ALREADY_EXISTS_ERROR_MESSAGE = "User already exists by username: ";
    public static final String EXISTING_EDGE_ERROR_MESSAGE = "Existing edge found from user id: %s to user id: %s";
    public static final String NO_EXISTING_EDGE_ERROR_MESSAGE = "No existing edge found from user id: %s to user id: "
            + "%s";
    public static final String USER_CONSTRAINT_ERROR_MESSAGE = "A constraint error occurred while creating the user";
    public static final String EDGE_CONSTRAINT_ERROR_MESSAGE = "A constraint error occurred while creating the edge";
    public static final String JSON_PROCESSING_ERROR_MESSAGE = "Failed to parse ResponseException message";
    public static final String GREMLIN_QUERY_ERROR_MESSAGE = "Gremlin query failed";

}
