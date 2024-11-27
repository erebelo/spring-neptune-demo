package com.erebelo.springneptunedemo.filter;

import static com.erebelo.springneptunedemo.util.ObjectMapperUtil.objectMapper;

import com.erebelo.springneptunedemo.exception.ExceptionResponse;
import com.erebelo.springneptunedemo.exception.model.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This filter modifies the `username` attribute in the request body for
 * user-related requests by adding the '@' prefix and converting it to lowercase
 * if the username is valid but does not start with '@'.
 * <p>
 * For PATCH requests: If the `username` is present, ensures it is valid (not
 * blank or containing whitespace).
 * <p>
 * For POST/PUT requests: Ensures the `username` is mandatory and valid.
 * <p>
 * The filter throws a `BadRequestException` if validation fails for `username`
 * (either if it's blank, contains whitespace, or is missing).
 */
public class UserRequestFilter extends OncePerRequestFilter {

    private static final String USER_API_PATH = "/spring-neptune-demo/users";
    private static final String USERNAME_PROPERTY = "username";
    private static final char AT_SIGN = '@';

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws IOException {
        try {
            String requestBody = readRequestBody(request);
            String modifiedBody = modifyUsernameAttribute(requestBody, HttpMethod.PATCH.matches(request.getMethod()));

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

            response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String regexPattern = "^" + USER_API_PATH + "/[^/]+$";

        boolean isPostUsers = HttpMethod.POST.matches(method) && path.equals(USER_API_PATH);
        boolean isPutOrPatchUsers = (HttpMethod.PUT.matches(method) || HttpMethod.PATCH.matches(method))
                && path.matches(regexPattern);

        return !(isPostUsers || isPutOrPatchUsers);
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }
        return requestBody.toString();
    }

    private String modifyUsernameAttribute(String body, boolean isPatchRequest) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(body);
        JsonNode usernameNode = jsonNode.get(USERNAME_PROPERTY);

        if (isPatchRequest) {
            if (usernameNode != null && isUsernameInvalid(usernameNode.asText())) {
                throw new BadRequestException("[username cannot be blank or contain any whitespace characters]");
            }
        } else if (usernameNode == null || isUsernameInvalid(usernameNode.asText())) {
            throw new BadRequestException(
                    "[username is mandatory and cannot be blank or contain any whitespace characters]");
        }

        // Need to check if username is not null as it may be null for
        // PATCH request method
        if (usernameNode != null) {
            String username = usernameNode.asText();
            if (username.charAt(0) != AT_SIGN) {
                username = AT_SIGN + username;
            }
            ((ObjectNode) jsonNode).put(USERNAME_PROPERTY, username.toLowerCase());
        }

        return objectMapper.writeValueAsString(jsonNode);
    }

    private boolean isUsernameInvalid(String username) {
        return username.isEmpty() || username.contains(" ") || username.equals("null");
    }
}
