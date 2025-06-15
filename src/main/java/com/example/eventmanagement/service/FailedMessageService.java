package com.example.eventmanagement.service;

import com.example.eventmanagement.dto.request.FailedMessageRequestDto;
import com.example.eventmanagement.entity.FailedMessage;
import com.example.eventmanagement.mapstruct.MappingContext;
import com.example.eventmanagement.repository.FailedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailedMessageService {

    private final FailedMessageRepository failedMessageRepository;
    private final MappingContext mappingContext;

    public void saveFailedMessage(FailedMessageRequestDto failedMessageRequestDto) {
        log.info("FailedMessageService.saveFailedMessage failedMessageRequestDto: {}", failedMessageRequestDto.toString());
        FailedMessage failedMessage = mappingContext
                .getStrategy(FailedMessageRequestDto.class, FailedMessage.class)
                .map(failedMessageRequestDto);
        failedMessageRepository.saveAndFlush(failedMessage);
        log.info("FailedMessageService.saveFailedMessage saving success.");
    }
}
