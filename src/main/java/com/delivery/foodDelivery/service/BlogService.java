package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.entity.Blog;
import com.delivery.foodDelivery.repository.mongo.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogService {
    private final BlogRepository blogRepository;

    public Blog createBlog(Blog blog) {
        return blogRepository.save(blog);
    }

    public List<Blog> getAllPublishedBlogs() {
        return blogRepository.findByIsDraftFalseOrderByCreatedAtDesc();
    }

    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    public void deleteBlog(String id) {
        blogRepository.deleteById(id);
    }

    public Blog getBlogById(String id) {
        return blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Blog not found"));
    }
}
