package com.petlogue.duopetbackend.info.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QShelterEntity is a Querydsl query type for ShelterEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShelterEntity extends EntityPathBase<ShelterEntity> {

    private static final long serialVersionUID = 1229116681L;

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

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath website = createString("website");

    public QShelterEntity(String variable) {
        super(ShelterEntity.class, forVariable(variable));
    }

    public QShelterEntity(Path<? extends ShelterEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QShelterEntity(PathMetadata metadata) {
        super(ShelterEntity.class, metadata);
    }

}

