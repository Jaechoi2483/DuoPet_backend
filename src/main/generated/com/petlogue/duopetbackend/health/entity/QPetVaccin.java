package com.petlogue.duopetbackend.health.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPetVaccin is a Querydsl query type for PetVaccin
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPetVaccin extends EntityPathBase<PetVaccin> {

    private static final long serialVersionUID = -274822737L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPetVaccin petVaccin = new QPetVaccin("petVaccin");

    public final DatePath<java.time.LocalDate> administeredDate = createDate("administeredDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final com.petlogue.duopetbackend.pet.entity.QPet pet;

    public final DatePath<java.time.LocalDate> scheduledDate = createDate("scheduledDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> vaccinationId = createNumber("vaccinationId", Long.class);

    public final StringPath vaccineName = createString("vaccineName");

    public QPetVaccin(String variable) {
        this(PetVaccin.class, forVariable(variable), INITS);
    }

    public QPetVaccin(Path<? extends PetVaccin> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPetVaccin(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPetVaccin(PathMetadata metadata, PathInits inits) {
        this(PetVaccin.class, metadata, inits);
    }

    public QPetVaccin(Class<? extends PetVaccin> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.pet = inits.isInitialized("pet") ? new com.petlogue.duopetbackend.pet.entity.QPet(forProperty("pet"), inits.get("pet")) : null;
    }

}

