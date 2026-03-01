package com.dochiri.osiv.service;

import com.dochiri.osiv.domain.Comment;
import com.dochiri.osiv.domain.CommentIdReference;
import com.dochiri.osiv.domain.Post;
import com.dochiri.osiv.domain.PostIdReference;
import com.dochiri.osiv.repository.CommentRepository;
import com.dochiri.osiv.repository.CommentIdReferenceRepository;
import com.dochiri.osiv.repository.PostRepository;
import com.dochiri.osiv.repository.PostIdReferenceRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class OsivDemoService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostIdReferenceRepository postIdReferenceRepository;
    private final CommentIdReferenceRepository commentIdReferenceRepository;

    @Transactional(readOnly = true)
    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PostByIdReferenceResult> findAllPostsByIdReference() {
        List<PostIdReference> posts = postIdReferenceRepository.findAll();
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream()
                .map(PostIdReference::getId)
                .toList();

        Map<Long, List<CommentByIdReferenceResult>> commentsByPostId = commentIdReferenceRepository
                .findByPostIdInOrderByPostIdAscIdAsc(postIds)
                .stream()
                .collect(groupingBy(
                        CommentIdReference::getPostId,
                        mapping(comment -> new CommentByIdReferenceResult(comment.getId(), comment.getContent()), toList())
                ));

        return posts.stream()
                .map(post -> new PostByIdReferenceResult(
                        post.getId(),
                        post.getTitle(),
                        commentsByPostId.getOrDefault(post.getId(), List.of())))
                .toList();
    }

    @Transactional
    public Long createCommentByEntityLoad(Long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        Comment comment = new Comment(content, post);
        commentRepository.save(comment);
        return comment.getId();
    }

    @Transactional
    public Long createCommentByIdReference(Long postId, String content) {
        Post postIdReference = postRepository.getReferenceById(postId);

        Comment comment = new Comment(content, postIdReference);
        commentRepository.saveAndFlush(comment);
        return comment.getId();
    }

    public record PostByIdReferenceResult(Long postId, String title, List<CommentByIdReferenceResult> comments) {
    }

    public record CommentByIdReferenceResult(Long commentId, String content) {
    }
}
