package com.api.netflixbackend.dto.request;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String email;
    private String password;
    private String fullName;
    private String role;
    private Boolean active;
}
