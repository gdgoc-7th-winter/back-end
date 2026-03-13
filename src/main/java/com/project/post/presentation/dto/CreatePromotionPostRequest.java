package com.project.post.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePromotionPostRequest {

    private String title;
    private String content;
    private String thumbnailUrl;
    private String category;
}
