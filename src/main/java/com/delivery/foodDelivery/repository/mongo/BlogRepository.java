package com.delivery.foodDelivery.repository.mongo;

import com.delivery.foodDelivery.entity.Blog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends MongoRepository<Blog, String> {
    List<Blog> findByIsDraftFalseOrderByCreatedAtDesc();
    List<Blog> findByIsDraftFalse();
}
