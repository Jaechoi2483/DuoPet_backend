package com.petlogue.duopetbackend.health.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPetMedicalVisit is a Querydsl query type for PetMedicalVisit
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPetMedicalVisit extends EntityPathBase<PetMedicalVisit> {

    private static final long serialVersionUID = -1955644692L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPetMedicalVisit petMedicalVisit = new QPetMedicalVisit("petMedicalVisit");

    public final NumberPath<java.math.BigDecimal> cost = createNumber("cost", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath diagnosis = createString("diagnosis");

    public final StringPath hospitalName = createString("hospitalName");

    public final com.petlogue.duopetbackend.pet.jpa.entity.QPet pet;

    public final StringPath treatment = createString("treatment");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath veterinarian = createString("veterinarian");

    public final DatePath<java.time.LocalDate> visitDate = createDate("visitDate", java.time.LocalDate.class);

    public final NumberPath<Long> visitId = createNumber("visitId", Long.class);

    public final StringPath visitReason = createString("visitReason");

    public QPetMedicalVisit(String variable) {
        this(PetMedicalVisit.class, forVariable(variable), INITS);
    }

    public QPetMedicalVisit(Path<? extends PetMedicalVisit> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPetMedicalVisit(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPetMedicalVisit(PathMetadata metadata, PathInits inits) {
        this(PetMedicalVisit.class, metadata, inits);
    }

    public QPetMedicalVisit(Class<? extends PetMedicalVisit> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.pet = inits.isInitialized("pet") ? new com.petlogue.duopetbackend.pet.jpa.entity.QPet(forProperty("pet"), inits.get("pet")) : null;
    }

}

