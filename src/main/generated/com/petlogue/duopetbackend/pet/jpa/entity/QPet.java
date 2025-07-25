package com.petlogue.duopetbackend.pet.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPet is a Querydsl query type for Pet
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPet extends EntityPathBase<Pet> {

    private static final long serialVersionUID = -95129251L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPet pet = new QPet("pet");

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final StringPath animalType = createString("animalType");

    public final StringPath breed = createString("breed");

    public final StringPath gender = createString("gender");

    public final StringPath neutered = createString("neutered");

    public final StringPath originalFilename = createString("originalFilename");

    public final NumberPath<Long> petId = createNumber("petId", Long.class);

    public final StringPath petName = createString("petName");

    public final DatePath<java.time.LocalDate> registrationDate = createDate("registrationDate", java.time.LocalDate.class);

    public final StringPath renameFilename = createString("renameFilename");

    public final com.petlogue.duopetbackend.user.entity.QUser user;

    public final NumberPath<java.math.BigDecimal> weight = createNumber("weight", java.math.BigDecimal.class);

    public QPet(String variable) {
        this(Pet.class, forVariable(variable), INITS);
    }

    public QPet(Path<? extends Pet> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPet(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPet(PathMetadata metadata, PathInits inits) {
        this(Pet.class, metadata, inits);
    }

    public QPet(Class<? extends Pet> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.petlogue.duopetbackend.user.entity.QUser(forProperty("user")) : null;
    }

}

