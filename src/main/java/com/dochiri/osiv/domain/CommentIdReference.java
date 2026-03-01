package com.dochiri.osiv.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_id_reference")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentIdReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    private String content;

    public CommentIdReference(Long postId, String content) {
        this.postId = postId;
        this.content = content;
    }
}
