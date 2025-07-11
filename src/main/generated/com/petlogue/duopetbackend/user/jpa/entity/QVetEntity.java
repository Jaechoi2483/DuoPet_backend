package com.petlogue.duopetbackend.user.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVetEntity is a Querydsl query type for VetEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVetEntity extends EntityPathBase<VetEntity> {

    private static final long serialVersionUID = 14607692L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QVetEntity vetEntity = new QVetEntity("vetEntity");

    public final StringPath address = createString("address");

    public final StringPath email = createString("email");

    public final StringPath licenseNumber = createString("licenseNumber");

    public final StringPath name = createString("name");

    public final StringPath originalFilename = createString("originalFilename");

    public final StringPath phone = createString("phone");

    public final StringPath renameFilename = createString("renameFilename");

    public final StringPath specialization = createString("specialization");

    public final QUserEntity user;

    public final NumberPath<Long> vetId = createNumber("vetId", Long.class);

    public final StringPath website = createString("website");

    public QVetEntity(String variable) {
        this(VetEntity.class, forVariable(variable), INITS);
    }

    public QVetEntity(Path<? extends VetEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QVetEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QVetEntity(PathMetadata metadata, PathInits inits) {
        this(VetEntity.class, metadata, inits);
    }

    public QVetEntity(Class<? extends VetEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

