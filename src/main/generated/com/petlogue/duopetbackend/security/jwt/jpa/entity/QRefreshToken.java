package com.petlogue.duopetbackend.security.jwt.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRefreshToken is a Querydsl query type for RefreshToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRefreshToken extends EntityPathBase<RefreshToken> {

    private static final long serialVersionUID = -337170360L;

    public static final QRefreshToken refreshToken1 = new QRefreshToken("refreshToken1");

    public final DateTimePath<java.util.Date> createdAt = createDateTime("createdAt", java.util.Date.class);

    public final StringPath deviceInfo = createString("deviceInfo");

    public final DateTimePath<java.util.Date> expiresAt = createDateTime("expiresAt", java.util.Date.class);

    public final StringPath ipAddress = createString("ipAddress");

    public final StringPath refreshToken = createString("refreshToken");

    public final NumberPath<Long> tokenId = createNumber("tokenId", Long.class);

    public final StringPath tokenStatus = createString("tokenStatus");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QRefreshToken(String variable) {
        super(RefreshToken.class, forVariable(variable));
    }

    public QRefreshToken(Path<? extends RefreshToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRefreshToken(PathMetadata metadata) {
        super(RefreshToken.class, metadata);
    }

}

