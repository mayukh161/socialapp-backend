package org.social.socialapp.service;

import lombok.RequiredArgsConstructor;
import org.social.socialapp.model.AuthorType;
import org.social.socialapp.model.Comment;
import org.social.socialapp.model.Post;
import org.social.socialapp.model.ScoreType;
import org.social.socialapp.repo.CommentRepo;
import org.social.socialapp.repo.PostRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepo commentRepo;
    private final PostRepo postRepo;
    private final RedisService redisService;

    public Comment addComment(String postId, Comment comment) {
        //get post associated with comment
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        comment.setPost(post);
        //checking if parent comment is there for depth level logic
        if (comment.getParentComment() !=null) {
            Comment parentComment = commentRepo.findById(comment.getParentComment().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found"));

            comment.setParentComment(parentComment);
            comment.setDepth_level(parentComment.getDepth_level() + 1);
        } else {
            comment.setDepth_level(0);
        }

        //Bot Guardrail setup
        if (comment.getType() == AuthorType.BOT) {

            //Vertical cap
            if (comment.getDepth_level() > 20) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Depth level of comments has exceeded 20");
            }

            //Horizontal cap
            if (!redisService.checkBotCount(postId)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Bot replies have exceeded limit");
            }

            //Cooldown cap
            String botId = comment.getAuthor_id();
            String userId = post.getAuthor_id();

            if (redisService.isCooldownActive(botId, userId)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Cooldown is active for this user");
            }
            redisService.setCooldown(botId, userId);

            //Calculate virality score for bot reply
            redisService.calculateViralityScore(postId, ScoreType.BOT_REPLY);

            //Notification setup
            if (redisService.hasRecentNotification(userId)) {
                redisService.pushPendingNotification(userId, "Bot: " + botId + " replied to your post");
            } else {
                System.out.println("Push notification has been sent to user: " + userId);
                redisService.setNotificationCooldown(userId);
            }
        } else {
            //Calculate virality score for human comment
            redisService.calculateViralityScore(postId, ScoreType.HUMAN_COMMENT);
        }

        return commentRepo.save(comment);
    }
}
