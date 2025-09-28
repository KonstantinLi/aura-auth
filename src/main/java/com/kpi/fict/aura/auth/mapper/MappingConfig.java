package com.kpi.fict.aura.auth.mapper;

import org.mapstruct.*;

@MapperConfig(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        componentModel = MappingConstants.ComponentModel.SPRING,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface MappingConfig {}