package org.social.socialapp.controller;

import lombok.RequiredArgsConstructor;
import org.social.socialapp.model.Comment;
import org.social.socialapp.model.Post;
import org.social.socialapp.service.CommentService;
import org.social.socialapp.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        return ResponseEntity.ok(postService.createPost(post));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable String postId, @RequestBody Comment comment) {
        return ResponseEntity.ok(commentService.addComment(postId, comment));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable String postId) {
        Long likes = postService.likePost(postId);
        return ResponseEntity.ok("No. of likes: " + likes);
    }
}
