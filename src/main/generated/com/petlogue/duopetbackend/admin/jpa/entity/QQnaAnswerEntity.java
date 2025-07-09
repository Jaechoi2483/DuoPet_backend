package com.petlogue.duopetbackend.admin.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQnaAnswerEntity is a Querydsl query type for QnaAnswerEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQnaAnswerEntity extends EntityPathBase<QnaAnswerEntity> {

    private static final long serialVersionUID = 1689250451L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQnaAnswerEntity qnaAnswerEntity = new QQnaAnswerEntity("qnaAnswerEntity");

    public final NumberPath<Integer> commentId = createNumber("commentId", Integer.class);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> parentCommentId = createNumber("parentCommentId", Integer.class);

    public final QQnaEntity qna;

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public QQnaAnswerEntity(String variable) {
        this(QnaAnswerEntity.class, forVariable(variable), INITS);
    }

    public QQnaAnswerEntity(Path<? extends QnaAnswerEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQnaAnswerEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQnaAnswerEntity(PathMetadata metadata, PathInits inits) {
        this(QnaAnswerEntity.class, metadata, inits);
    }

    public QQnaAnswerEntity(Class<? extends QnaAnswerEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.qna = inits.isInitialized("qna") ? new QQnaEntity(forProperty("qna")) : null;
    }

}

