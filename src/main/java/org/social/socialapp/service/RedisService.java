package org.social.socialapp.service;

import lombok.RequiredArgsConstructor;
import org.social.socialapp.model.ScoreType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate stringRedisTemplate;

    public void calculateViralityScore(String postId, ScoreType scoreType) {
        //virality point calculator
        stringRedisTemplate.opsForValue()
                .increment("post:" + postId + ":virality_score", scoreType.getScore());
    }

    //Horizontal cap
    public boolean checkBotCount(String postId) {
        String key = "post:" + postId + ":bot_count";

        String luaScript =
                "local botCount = tonumber(redis.call('GET', KEYS[1]) or 0) " +
                "if (botCount >= 100) then " +
                "   return 0 " +
                "else " +
                "   redis.call('INCR', KEYS[1]) " +
                "   return 1 " +
                "end";

        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);
        Long result = stringRedisTemplate.execute(script, List.of(key));
        return Long.valueOf(1).equals(result);
    }

    //Cooldown cap
    public boolean isCooldownActive(String botId, String userId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey("cooldown:bot" + botId + ":user" +  userId));
    }

    public void setCooldown(String botId, String userId) {
        stringRedisTemplate.opsForValue().set(
                "cooldown:bot" + botId + ":user" + userId, "1",
                10, TimeUnit.MINUTES
        );
    }

    //Notifications setup
    public void setNotificationCooldown(String userId) {
        stringRedisTemplate.opsForValue().set(
                "notif_cooldown:" + userId , "1",
                15, TimeUnit.MINUTES
        );
    }

    public boolean hasRecentNotification(String userId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey("notif_cooldown:" + userId)
        );
    }

    public void pushPendingNotification (String userId, String message) {
        stringRedisTemplate.opsForList()
                .rightPush("user:" + userId + ":pending_notifs", message);
    }

    //Pop notifications
    public List<String> popAllNotifications(String userId) {
        String key = "user:" + userId + ":pending_notifs";
        List<String> notifications = stringRedisTemplate.opsForList()
                .range(key, 0, -1);
        stringRedisTemplate.delete(key);
        return notifications != null ? notifications : new ArrayList<>();
    }

    //Calculate likes on a post
    public Long calculateLikes(String postId) {
        return stringRedisTemplate.opsForValue()
                .increment("post:" + postId + ":likes");
    }
}
