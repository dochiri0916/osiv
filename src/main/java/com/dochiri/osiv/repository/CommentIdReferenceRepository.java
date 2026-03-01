package com.dochiri.osiv.repository;

import com.dochiri.osiv.domain.CommentIdReference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentIdReferenceRepository extends JpaRepository<CommentIdReference, Long> {

    List<CommentIdReference> findByPostIdInOrderByPostIdAscIdAsc(List<Long> postIds);
}
