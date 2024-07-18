package com.erebelo.springneptunedemo.controller.validation;

import com.erebelo.springneptunedemo.exception.ExceptionResponse;
import com.erebelo.springneptunedemo.exception.model.BadRequestException;
import com.erebelo.springneptunedemo.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class UserRequestFilter extends OncePerRequestFilter {

    private static final String USER_API_PATH = "/spring-neptune-demo/users";
    private static final String USERNAME_PROPERTY = "username";
    private static final char AT_SIGN = '@';

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws IOException {
        try {
            String requestBody = readRequestBody(request);
            String modifiedBody = modifyNameAttribute(requestBody);

            // Wrap the request with the modified body
            CustomHttpServletRequest modifiedRequest = new CustomHttpServletRequest(request, modifiedBody);

            // Proceed with the filter chain using the modified request
            filterChain.doFilter(modifiedRequest, response);
        } catch (Exception e) {
            ExceptionResponse exceptionResponse = new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "UserRequestFilter error: " + e.getMessage(), System.currentTimeMillis());

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

            if (e instanceof BadRequestException) {
                exceptionResponse.setStatus(HttpStatus.BAD_REQUEST);
                response.setStatus(HttpStatus.BAD_REQUEST.value());
            }

            response.getWriter().write(ObjectMapperUtil.objectMapper.writeValueAsString(exceptionResponse));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        boolean isPostToUsers = HttpMethod.POST.matches(method) && path.equals(USER_API_PATH);
        boolean isPutToUser = HttpMethod.PUT.matches(method) && path.matches(USER_API_PATH + "/\\d+");

        return !(isPostToUsers || isPutToUser);
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }
        return requestBody.toString();
    }

    private String modifyNameAttribute(String body) throws IOException {
        ObjectMapper objectMapper = ObjectMapperUtil.objectMapper;
        JsonNode jsonNode = objectMapper.readTree(body);
        BadRequestException exception = new BadRequestException("[username is mandatory]");

        if (!jsonNode.has(USERNAME_PROPERTY)) {
            throw exception;
        }

        String name = jsonNode.get(USERNAME_PROPERTY).asText().trim();
        if (name.isBlank()) {
            throw exception;
        }

        if (name.charAt(0) != AT_SIGN) {
            ((ObjectNode) jsonNode).put(USERNAME_PROPERTY, AT_SIGN + name);
        }

        return objectMapper.writeValueAsString(jsonNode);
    }
}
