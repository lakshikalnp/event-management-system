package com.example.eventmanagement.mapstruct;

import com.example.eventmanagement.dto.request.EventCreateRequestDto;
import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.enumeration.Visibility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "host", source = "userId", qualifiedByName = "mapUserIdToUser")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "visibility", source = "visibility", qualifiedByName = "mapVisibility")
    Event toEvent(EventCreateRequestDto dto);

    @Named("mapVisibility")
    default Visibility mapVisibility(String visibility) {
        return Visibility.valueOf(visibility.toUpperCase());
    }

    @Named("mapUserIdToUser")
    default User mapUserIdToUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }
}
