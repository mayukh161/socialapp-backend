package org.social.socialapp.model;

import lombok.Getter;

@Getter
public enum ScoreType {
    BOT_REPLY(1),
    HUMAN_LIKE(20),
    HUMAN_COMMENT(50);

    private final long score;

    ScoreType(long score) {
        this.score = score;
    }
}
