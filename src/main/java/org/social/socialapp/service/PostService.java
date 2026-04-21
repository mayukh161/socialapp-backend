package org.social.socialapp.service;

import lombok.RequiredArgsConstructor;
import org.social.socialapp.model.Post;
import org.social.socialapp.model.ScoreType;
import org.social.socialapp.repo.PostRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepo postRepo;
    private final RedisService redisService;

    public Post createPost(Post post) {
        return postRepo.save(post);
    }

    public Long likePost(String postId) {
        postRepo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + postId));
        redisService.calculateViralityScore(postId, ScoreType.HUMAN_LIKE);
        return redisService.calculateLikes(postId);
    }
}
