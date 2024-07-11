package com.erebelo.springneptunedemo.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectMapperUtilTest {

    @Test
    void testObjectMapperConfiguration() {
        var objectMapper = ObjectMapperUtil.objectMapper;

        assertThat(objectMapper.getRegisteredModuleIds()).contains("jackson-datatype-jsr310");
        assertThat(objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
        assertThat(objectMapper.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion()).isEqualTo(JsonInclude.Include.NON_NULL);

        assertThat(objectMapper.getDateFormat()).isInstanceOf(SimpleDateFormat.class);
        SimpleDateFormat dateFormat = (SimpleDateFormat) objectMapper.getDateFormat();
        assertThat(dateFormat.toPattern()).isEqualTo("yyyy-MM-dd");
    }

    @Test
    void testObjectMapperSingleton() {
        var objectMapper1 = ObjectMapperUtil.objectMapper;
        var objectMapper2 = ObjectMapperUtil.objectMapper;

        assertThat(objectMapper1).isSameAs(objectMapper2);
    }
}
