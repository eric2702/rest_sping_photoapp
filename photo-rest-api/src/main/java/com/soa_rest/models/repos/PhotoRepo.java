package com.soa_rest.models.repos;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.soa_rest.models.entities.Photo;

public interface PhotoRepo extends CrudRepository<Photo, Long> {
    List<Photo> findByNameContains(String name);

    List<Photo> findAllByOrderByDateDesc();
}
