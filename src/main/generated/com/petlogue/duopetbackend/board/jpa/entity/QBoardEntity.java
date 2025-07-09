package com.petlogue.duopetbackend.board.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBoardEntity is a Querydsl query type for BoardEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoardEntity extends EntityPathBase<BoardEntity> {

    private static final long serialVersionUID = 1271404864L;

    public static final QBoardEntity boardEntity = new QBoardEntity("boardEntity");

    public final StringPath category = createString("category");

    public final StringPath contentBody = createString("contentBody");

    public final NumberPath<Long> contentId = createNumber("contentId", Long.class);

    public final StringPath contentType = createString("contentType");

    public final DateTimePath<java.util.Date> createdAt = createDateTime("createdAt", java.util.Date.class);

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final StringPath originalFilename = createString("originalFilename");

    public final StringPath renameFilename = createString("renameFilename");

    public final StringPath tags = createString("tags");

    public final StringPath title = createString("title");

    public final DateTimePath<java.util.Date> updateAt = createDateTime("updateAt", java.util.Date.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QBoardEntity(String variable) {
        super(BoardEntity.class, forVariable(variable));
    }

    public QBoardEntity(Path<? extends BoardEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBoardEntity(PathMetadata metadata) {
        super(BoardEntity.class, metadata);
    }

}

