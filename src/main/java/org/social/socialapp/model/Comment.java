package org.social.socialapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JsonIgnore
    private Post post;

    private String author_id;

    @Enumerated(EnumType.STRING)
    private AuthorType type;

    private String content;

    @ManyToOne
    private Comment parentComment;

    private int depth_level;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
