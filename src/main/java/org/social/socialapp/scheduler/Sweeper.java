package org.social.socialapp.scheduler;

import lombok.RequiredArgsConstructor;
import org.social.socialapp.service.RedisService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Sweeper {
    private final RedisService redisService;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedRate = 300000)
    public void sweepPendingNotifications() {
        //Find the users with pending notifications
        Set<String> keys = stringRedisTemplate.keys("user:*:pending_notifs");

        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            //Extracting the actual User ID from the pattern
            String userID = key.split(":")[1];
            List<String> notifications = redisService.popAllNotifications(userID);

            if (notifications.isEmpty()) continue;

            int notificationCount = notifications.size();
            String firstNotification = notifications.get(0);

            if (notificationCount == 1) {
                System.out.println("Summarized push notification: " + firstNotification);
            } else {
                String authorName = firstNotification.split(" ")[1];
                System.out.println("Summarized push notification: Bot " + authorName + " and " + (notificationCount - 1) + " others interacted with your post");
            }
        }
    }

}
