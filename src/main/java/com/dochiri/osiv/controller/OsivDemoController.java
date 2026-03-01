package com.dochiri.osiv.controller;

import com.dochiri.osiv.service.OsivDemoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/osiv")
@RequiredArgsConstructor
public class OsivDemoController {

    private final OsivDemoService osivDemoService;

    @GetMapping("/posts")
    public List<PostResponse> findPosts() {
        return osivDemoService.findAllPosts().stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getComments().stream()
                                .map(comment -> new CommentResponse(comment.getId(), comment.getContent()))
                                .toList()))
                .toList();
    }

    @GetMapping("/posts-id-reference")
    public List<PostResponse> findPostsByIdReference() {
        return osivDemoService.findAllPostsByIdReference().stream()
                .map(post -> new PostResponse(
                        post.postId(),
                        post.title(),
                        post.comments().stream()
                                .map(comment -> new CommentResponse(comment.commentId(), comment.content()))
                                .toList()))
                .toList();
    }

    @PostMapping("/comments/load")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedCommentResponse createCommentByEntityLoad(@RequestBody CreateCommentRequest request) {
        Long commentId = osivDemoService.createCommentByEntityLoad(request.postId(), request.content());
        return new CreatedCommentResponse(commentId, "entity-load");
    }

    @PostMapping("/comments/id-reference")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedCommentResponse createCommentByIdReference(@RequestBody CreateCommentRequest request) {
        Long commentId = osivDemoService.createCommentByIdReference(request.postId(), request.content());
        return new CreatedCommentResponse(commentId, "id-reference");
    }

    public record PostResponse(Long postId, String title, List<CommentResponse> comments) {
    }

    public record CommentResponse(Long commentId, String content) {
    }

    public record CreateCommentRequest(Long postId, String content) {
    }

    public record CreatedCommentResponse(Long commentId, String strategy) {
    }
}
