package com.erebelo.springneptunedemo.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.SimpleDateFormat;
import org.junit.jupiter.api.Test;

class ObjectMapperUtilTest {

    private static final String JAVA_TIME_MODULE = "jackson-datatype-jsr310";
    private static final String ISO_LOCAL_DATE_FORMAT = "yyyy-MM-dd";

    @Test
    void testObjectMapperConfiguration() {
        ObjectMapper objectMapper = ObjectMapperUtil.objectMapper;

        assertThat(objectMapper.getRegisteredModuleIds()).contains(JAVA_TIME_MODULE);
        assertThat(objectMapper.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion())
                .isEqualTo(JsonInclude.Include.ALWAYS);
        assertThat(objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))
                .isFalse();

        assertThat(objectMapper.getDateFormat()).isInstanceOf(SimpleDateFormat.class);
        SimpleDateFormat dateFormat = (SimpleDateFormat) objectMapper.getDateFormat();
        assertThat(dateFormat.toPattern()).isEqualTo(ISO_LOCAL_DATE_FORMAT);
    }

    @Test
    void testObjectMapperSingleton() {
        ObjectMapper objectMapper1 = ObjectMapperUtil.objectMapper;
        ObjectMapper objectMapper2 = ObjectMapperUtil.objectMapper;

        assertThat(objectMapper1).isSameAs(objectMapper2);
    }
}
