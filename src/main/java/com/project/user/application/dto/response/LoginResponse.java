package com.project.user.application.dto.response;
import java.io.Serializable; // 1. 임포트 추가
import lombok.Getter;

@Getter
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String email;
    private String nickname;
    private boolean needsProfile;

    public LoginResponse(Long id, String email, String nickname, boolean b) {
    }
}
