package com.example.eventmanagement.specification;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.User;
import com.example.eventmanagement.enumeration.Visibility;
import com.example.eventmanagement.util.SecurityUtil;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class EventSpecification {

    private EventSpecification () {}

    public static Specification<Event> hasLocation(String location) {
        return (root, query, cb) -> {
            if (location == null || location.isBlank()) return null;
            return cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
        };
    }

    public static Specification<Event> hasVisibility(String visibility) {
        return (root, query, cb) -> {
            if (visibility == null) return null;
            return cb.equal(root.get("visibility"), Visibility.valueOf(visibility));
        };
    }

    public static Specification<Event> betweenDate(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return null;
            ZonedDateTime startOfDay = date.atStartOfDay(ZoneId.of("UTC"));
            ZonedDateTime endOfDay = date.plusDays(1).atStartOfDay(ZoneId.of("UTC")).minusNanos(1);
            return cb.and(
                    cb.lessThanOrEqualTo(root.get("startTime"), endOfDay),
                    cb.greaterThanOrEqualTo(root.get("endTime"), startOfDay)
            );
        };
    }

    public static Specification<Event> hasAccessToEvent() {
        return (root, query, cb) -> {
            User authUser = SecurityUtil.getAuthUser();
            Predicate isPublic = cb.equal(root.get("visibility"), Visibility.PUBLIC.name());
            Predicate isOwner = cb.equal(root.get("host").get("email"), authUser.getEmail());
            return cb.or(isPublic, isOwner);
        };
    }

}
