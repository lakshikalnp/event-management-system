package com.example.eventmanagement.mapstruct;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MappingContext {


    private final Map<Class<?>, MappingStrategy<?, ?>> strategyMap = new HashMap<>();

    public MappingContext(List<MappingStrategy<?, ?>> strategies) {
        // Manual and safe registration of each strategy
        strategies.forEach(strategy -> {
            if (strategy instanceof EventCreateRequestDtoToEventStrategy) {
                strategyMap.put(EventCreateRequestDto.class, strategy);
            }
            // Add others here: User, Attendance, etc.
        });
    }

    @SuppressWarnings("unchecked")
    public <S, T> MappingStrategy<S, T> getStrategy(Class<S> sourceType, Class<T> targetType) {
        return (MappingStrategy<S, T>) strategyMap.get(sourceType);
    }
}
