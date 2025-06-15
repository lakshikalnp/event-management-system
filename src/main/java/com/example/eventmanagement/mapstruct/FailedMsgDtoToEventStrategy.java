package com.example.eventmanagement.mapstruct;

import com.example.eventmanagement.dto.request.FailedMessageRequestDto;
import com.example.eventmanagement.entity.FailedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FailedMsgDtoToEventStrategy implements MappingStrategy<FailedMessageRequestDto, FailedMessage>{
    private final FailedMessageMapper failedMessageMapper;

    @Override
    public FailedMessage map(FailedMessageRequestDto source) {
        return failedMessageMapper.toEntity(source);
    }
}
