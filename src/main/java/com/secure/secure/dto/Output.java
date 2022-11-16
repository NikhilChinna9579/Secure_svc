package com.secure.secure.dto;

import com.secure.secure.entity.Comment;

import java.util.Date;
import java.util.List;

public class Output {
    public record GroupsList(
            String id,
            String name,
            String description){
    }

    public record postList(
            String postId,
            String message,
            String fileName,
            Long fileSize,
            String createdBy,
            Date postedTime,
            List<Comment> comments){
    }
}
