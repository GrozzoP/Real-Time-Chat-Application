package com.grosso.chat.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(Long id, String username, String email) {}