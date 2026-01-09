package com.example.anonymous_board.dto;

import com.example.anonymous_board.domain.PostImage;
import lombok.Getter;

@Getter
public class PostImageResponse {
    private final Long id; // 게시글 이미지 ID
    private final String url; // 게시글 이미지 URL

    public PostImageResponse(PostImage postImage) {
        this.id = postImage.getId();
        this.url = postImage.getImageUrl();
    }
}
