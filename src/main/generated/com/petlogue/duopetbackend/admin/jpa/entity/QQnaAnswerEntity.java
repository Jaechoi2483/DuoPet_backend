package com.petlogue.duopetbackend.admin.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQnaAnswerEntity is a Querydsl query type for QnaAnswerEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQnaAnswerEntity extends EntityPathBase<QnaAnswerEntity> {

    private static final long serialVersionUID = 1689250451L;

    public static final QQnaAnswerEntity qnaAnswerEntity = new QQnaAnswerEntity("qnaAnswerEntity");

    public final NumberPath<Integer> commentId = createNumber("commentId", Integer.class);

    public final StringPath content = createString("content");

    public final NumberPath<Integer> contentId = createNumber("contentId", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> parentCommentId = createNumber("parentCommentId", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public QQnaAnswerEntity(String variable) {
        super(QnaAnswerEntity.class, forVariable(variable));
    }

    public QQnaAnswerEntity(Path<? extends QnaAnswerEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQnaAnswerEntity(PathMetadata metadata) {
        super(QnaAnswerEntity.class, metadata);
    }

}

