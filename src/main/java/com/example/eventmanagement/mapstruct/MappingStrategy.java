package com.example.eventmanagement.mapstruct;

public interface MappingStrategy <S, T>{
    T map(S source);
}
