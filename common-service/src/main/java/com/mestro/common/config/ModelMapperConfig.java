package com.mestro.common.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
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
        ModelMapper modelMapper = new ModelMapper();

        // Use strict matching to avoid ambiguous mappings
        modelMapper
                .getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);

        return modelMapper;
    }
}
