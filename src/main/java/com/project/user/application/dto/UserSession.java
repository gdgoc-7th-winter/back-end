package com.project.user.application.dto;

import com.project.user.domain.enums.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String email;
    private Authority authority;
    private boolean needsProfile;
}
