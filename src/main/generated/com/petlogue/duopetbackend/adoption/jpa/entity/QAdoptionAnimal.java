package com.petlogue.duopetbackend.adoption.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdoptionAnimal is a Querydsl query type for AdoptionAnimal
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdoptionAnimal extends EntityPathBase<AdoptionAnimal> {

    private static final long serialVersionUID = 282568797L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdoptionAnimal adoptionAnimal = new QAdoptionAnimal("adoptionAnimal");

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final NumberPath<Long> animalId = createNumber("animalId", Long.class);

    public final StringPath animalType = createString("animalType");

    public final StringPath apiSource = createString("apiSource");

    public final StringPath breed = createString("breed");

    public final StringPath colorCd = createString("colorCd");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final StringPath desertionNo = createString("desertionNo");

    public final StringPath gender = createString("gender");

    public final DatePath<java.time.LocalDate> happenDate = createDate("happenDate", java.time.LocalDate.class);

    public final StringPath happenPlace = createString("happenPlace");

    public final StringPath imageUrl = createString("imageUrl");

    public final DatePath<java.time.LocalDate> intakeDate = createDate("intakeDate", java.time.LocalDate.class);

    public final StringPath name = createString("name");

    public final StringPath neutered = createString("neutered");

    public final StringPath originalFilename = createString("originalFilename");

    public final StringPath processState = createString("processState");

    public final StringPath profileImage = createString("profileImage");

    public final DatePath<java.time.LocalDate> publicNoticeEnd = createDate("publicNoticeEnd", java.time.LocalDate.class);

    public final StringPath publicNoticeNo = createString("publicNoticeNo");

    public final DatePath<java.time.LocalDate> publicNoticeStart = createDate("publicNoticeStart", java.time.LocalDate.class);

    public final StringPath renameFilename = createString("renameFilename");

    public final com.petlogue.duopetbackend.info.jpa.entity.QShelterEntity shelter;

    public final StringPath specialMark = createString("specialMark");

    public final StringPath status = createString("status");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Double> weight = createNumber("weight", Double.class);

    public QAdoptionAnimal(String variable) {
        this(AdoptionAnimal.class, forVariable(variable), INITS);
    }

    public QAdoptionAnimal(Path<? extends AdoptionAnimal> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdoptionAnimal(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdoptionAnimal(PathMetadata metadata, PathInits inits) {
        this(AdoptionAnimal.class, metadata, inits);
    }

    public QAdoptionAnimal(Class<? extends AdoptionAnimal> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shelter = inits.isInitialized("shelter") ? new com.petlogue.duopetbackend.info.jpa.entity.QShelterEntity(forProperty("shelter")) : null;
    }

}

