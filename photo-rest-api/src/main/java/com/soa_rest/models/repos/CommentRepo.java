package com.soa_rest.models.repos;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.soa_rest.models.entities.Comment;

public interface CommentRepo extends CrudRepository<Comment, Long> {
    // find by photo id
    List<Comment> findByPhotoId(long photoId);

    // find by photo id and order by id desc
    List<Comment> findByPhotoIdOrderByDateDesc(long photoId);

    List<Comment> findTop2ByPhotoIdOrderByDateDesc(long photoId);

    // deletebyphotoid
    void deleteByPhotoId(long photoId);
}
