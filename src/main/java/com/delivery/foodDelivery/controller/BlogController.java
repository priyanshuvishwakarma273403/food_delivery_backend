package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.entity.Blog;
import com.delivery.foodDelivery.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;

    @PostMapping
    public ResponseEntity<ApiResponse<Blog>> createBlog(@RequestBody Blog blog) {
        return ResponseEntity.ok(ApiResponse.success(blogService.createBlog(blog)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Blog>>> getAllBlogs() {
        return ResponseEntity.ok(ApiResponse.success(blogService.getAllPublishedBlogs()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Blog>> getBlogById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(blogService.getBlogById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBlog(@PathVariable String id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok(ApiResponse.success("Blog deleted successfully", null));
    }
}
