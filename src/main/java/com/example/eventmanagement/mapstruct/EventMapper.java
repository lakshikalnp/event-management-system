package com.example.eventmanagement.mapstruct;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.enumeration.Visibility;
import com.example.eventmanagement.exception.ResourceNotFoundException;
import com.example.eventmanagement.repository.UserRepository;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring")
@NoArgsConstructor
public abstract class EventMapper {

    @Autowired
    protected UserRepository userRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "host", source = "userId", qualifiedByName = "mapUserIdToUser")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "visibility", source = "visibility", qualifiedByName = "mapVisibility")
    public abstract Event toEntity(EventCreateRequestDto dto);

    @Named("mapVisibility")
    protected Visibility mapVisibility(String visibility) {
        return Visibility.valueOf(visibility.toUpperCase());
    }

    @Named("mapUserIdToUser")
    protected User mapUserIdToUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

}
