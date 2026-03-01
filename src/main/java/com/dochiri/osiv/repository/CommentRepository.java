package com.dochiri.osiv.repository;

import com.dochiri.osiv.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
