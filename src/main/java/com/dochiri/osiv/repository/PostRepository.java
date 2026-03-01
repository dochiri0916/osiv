package com.dochiri.osiv.repository;

import com.dochiri.osiv.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
