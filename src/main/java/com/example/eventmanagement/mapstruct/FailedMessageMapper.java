package com.example.eventmanagement.mapstruct;

import com.example.eventmanagement.dto.request.FailedMessageRequestDto;
import com.example.eventmanagement.entity.FailedMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface FailedMessageMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    FailedMessage toEntity(FailedMessageRequestDto dto);
}
