package com.erebelo.springneptunedemo.controller.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CustomHttpServletRequest extends HttpServletRequestWrapper {

    private final String body;

    public CustomHttpServletRequest(HttpServletRequest request, String body) {
        super(request);
        this.body = body;
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            private final InputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }
        };
    }
}
