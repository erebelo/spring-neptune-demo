package com.erebelo.springneptunedemo.domain.response.node;

import com.erebelo.springneptunedemo.domain.response.edge.LazyFollowResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {

    private String id;
    private String username;
    private String name;
    private UserAddressResponse address;
    private List<LazyFollowResponse> followers;
    private List<LazyFollowResponse> following;

}
