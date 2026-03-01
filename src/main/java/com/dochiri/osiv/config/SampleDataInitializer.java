package com.dochiri.osiv.config;

import com.dochiri.osiv.domain.Comment;
import com.dochiri.osiv.domain.CommentIdReference;
import com.dochiri.osiv.domain.Post;
import com.dochiri.osiv.domain.PostIdReference;
import com.dochiri.osiv.repository.CommentRepository;
import com.dochiri.osiv.repository.CommentIdReferenceRepository;
import com.dochiri.osiv.repository.PostRepository;
import com.dochiri.osiv.repository.PostIdReferenceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SampleDataInitializer implements CommandLineRunner {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostIdReferenceRepository postIdReferenceRepository;
    private final CommentIdReferenceRepository commentIdReferenceRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (postRepository.count() == 0) {
            Post post1 = new Post("OSIV 이해하기");
            Post post2 = new Post("JPA 지연 로딩");
            postRepository.saveAll(List.of(post1, post2));

            commentRepository.saveAll(List.of(
                    new Comment("정리 감사합니다.", post1),
                    new Comment("예시가 이해하기 좋아요.", post1),
                    new Comment("N+1도 같이 다뤄주세요.", post2)
            ));
        }

        if (postIdReferenceRepository.count() == 0) {
            PostIdReference postIdReference1 = new PostIdReference("식별자 참조 설계");
            PostIdReference postIdReference2 = new PostIdReference("OSIV 없이 안전한 조회");
            postIdReferenceRepository.saveAll(List.of(postIdReference1, postIdReference2));

            commentIdReferenceRepository.saveAll(List.of(
                    new CommentIdReference(postIdReference1.getId(), "연관 대신 ID로 느슨하게 연결"),
                    new CommentIdReference(postIdReference1.getId(), "조회 시점은 서비스에서 통제"),
                    new CommentIdReference(postIdReference2.getId(), "OSIV off에서도 동일하게 동작")
            ));
        }
    }
}
