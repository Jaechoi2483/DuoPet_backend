package com.petlogue.duopetbackend.user.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShelterEntity is a Querydsl query type for ShelterEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShelterEntity extends EntityPathBase<ShelterEntity> {

    private static final long serialVersionUID = -1185834868L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShelterEntity shelterEntity = new QShelterEntity("shelterEntity");

    public final StringPath address = createString("address");

    public final NumberPath<Integer> capacity = createNumber("capacity", Integer.class);

    public final StringPath email = createString("email");

    public final StringPath operatingHours = createString("operatingHours");

    public final StringPath originalFilename = createString("originalFilename");

    public final StringPath phone = createString("phone");

    public final StringPath renameFilename = createString("renameFilename");

    public final NumberPath<Long> shelterId = createNumber("shelterId", Long.class);

    public final StringPath shelterName = createString("shelterName");

    public final QUserEntity user;

    public final StringPath website = createString("website");

    public QShelterEntity(String variable) {
        this(ShelterEntity.class, forVariable(variable), INITS);
    }

    public QShelterEntity(Path<? extends ShelterEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShelterEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShelterEntity(PathMetadata metadata, PathInits inits) {
        this(ShelterEntity.class, metadata, inits);
    }

    public QShelterEntity(Class<? extends ShelterEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

