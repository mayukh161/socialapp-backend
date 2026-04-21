#Social App Backend

##Tech Stack Used:
Java 17, Spring Boot 4.0.5, Docker, PostgreSQL, Redis

##Setup:
1. 'docker-compose up -d' - starts postgres and redis
2. Run the spring boot application
3. Import 'Social app.postman_collection.json' in Postman to test the endpoints

##Endpoints:
1. 'POST /api/posts' - create a post
2. 'POST /api/posts/{postId}/comments' - add a comment
3. 'POST /api/posts/{postId}/like' - like a post

##How I handled the Atomic Locks:
###Horizontal Cap
The approach of incrementing the counter when a bot comments on a post and then checking if it exceeds 100 cannot be used
under concurrency. Multiple requests can be read at the same time and all can hit the database py passing the check.

I used Redis Lua Script to handle this. The check condition and the increment both run as one operation so no extra request
can pass the condition. I have tested with 200 concurrent requests in Postman and both Redis and Postgres have stopped at
exactly 100 requests.

###Vertical Cap
The depth level is calculated from the parent comment before being saved. If depth level goes above 20 then the request 
is rejected with a 400 status code and nothing is written in the database.

###Cooldown Cap
When a bot comments on a user's post a Redis key is created with a 10 minute TTL(Time To Live). Before every bot comments 
on a post the API checks whether that key exists and if it does then the request is blocked with a 429 status code. Since
Redis handles TTL keys atomically so no race condition occurs.

##Notification Scheduler:
When a bot interacts with a user's post then API will check if the user has been notified in the last 15 minutes.

● If YES - a notification is pushed into a Redis List 'user:{id}:pending_notifs'.<br>
● If NO - logs "Push Notification Sent to User" and sets a 15-minute cooldown.

A scheduler runs every 5 minutes and scans all users with pending notifications. It pops all notifications from their list
and logs a summarized message:- 'Bot X and [N] others interacted with your post.'

This prevents the users from being spammed by individual notifications.
