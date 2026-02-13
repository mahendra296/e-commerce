package com.mestro.common.config;

import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(ModelMapper.class)
public class ModelMapperConfig {

    @Bean
    @ConditionalOnMissingBean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
