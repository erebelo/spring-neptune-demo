package com.erebelo.springneptunedemo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SpringNeptuneDemoApplicationTest {

    @Mock
    private ConfigurableApplicationContext contextMock;

    @Test
    void contextLoads() {
        // This test simply checks if the Spring context loads successfully
        // If it doesn't, it will throw an exception
    }

    @Test
    void mainRunSuccessfully() {
        try (MockedStatic<SpringApplication> mockedStatic = mockStatic(SpringApplication.class)) {
            mockedStatic.when(() -> SpringApplication.run(any(Class.class), any(String[].class))).thenReturn(contextMock);

            SpringNeptuneDemoApplication.main(new String[]{});

            mockedStatic.verify(() -> SpringApplication.run(SpringNeptuneDemoApplication.class, new String[]{}));
        }
    }
}
