package com.erebelo.springneptunedemo.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectMapperUtilTest {

    private static final String JAVA_TIME_MODULE = "jackson-datatype-jsr310";
    private static final String ISO_LOCAL_DATE_FORMAT = "yyyy-MM-dd";

    @Test
    void testObjectMapperConfiguration() {
        var objectMapper = ObjectMapperUtil.objectMapper;

        assertThat(objectMapper.getRegisteredModuleIds()).contains(JAVA_TIME_MODULE);
        assertThat(objectMapper.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion()).isEqualTo(JsonInclude.Include.ALWAYS);
        assertThat(objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();

        assertThat(objectMapper.getDateFormat()).isInstanceOf(SimpleDateFormat.class);
        SimpleDateFormat dateFormat = (SimpleDateFormat) objectMapper.getDateFormat();
        assertThat(dateFormat.toPattern()).isEqualTo(ISO_LOCAL_DATE_FORMAT);
    }

    @Test
    void testObjectMapperSingleton() {
        var objectMapper1 = ObjectMapperUtil.objectMapper;
        var objectMapper2 = ObjectMapperUtil.objectMapper;

        assertThat(objectMapper1).isSameAs(objectMapper2);
    }
}
