package com.petlogue.duopetbackend.admin.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQnaEntity is a Querydsl query type for QnaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQnaEntity extends EntityPathBase<QnaEntity> {

    private static final long serialVersionUID = -741610827L;

    public static final QQnaEntity qnaEntity = new QQnaEntity("qnaEntity");

    public final StringPath category = createString("category");

    public final StringPath contentBody = createString("contentBody");

    public final NumberPath<Integer> contentId = createNumber("contentId", Integer.class);

    public final StringPath contentType = createString("contentType");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final StringPath originalFilename = createString("originalFilename");

    public final StringPath renameFilename = createString("renameFilename");

    public final StringPath tags = createString("tags");

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QQnaEntity(String variable) {
        super(QnaEntity.class, forVariable(variable));
    }

    public QQnaEntity(Path<? extends QnaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQnaEntity(PathMetadata metadata) {
        super(QnaEntity.class, metadata);
    }

}

