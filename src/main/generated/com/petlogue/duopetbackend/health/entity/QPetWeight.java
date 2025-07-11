package com.petlogue.duopetbackend.health.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPetWeight is a Querydsl query type for PetWeight
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPetWeight extends EntityPathBase<PetWeight> {

    private static final long serialVersionUID = -242316937L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPetWeight petWeight = new QPetWeight("petWeight");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> measuredDate = createDate("measuredDate", java.time.LocalDate.class);

    public final StringPath memo = createString("memo");

    public final com.petlogue.duopetbackend.pet.entity.QPet pet;

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> weightId = createNumber("weightId", Long.class);

    public final NumberPath<java.math.BigDecimal> weightKg = createNumber("weightKg", java.math.BigDecimal.class);

    public QPetWeight(String variable) {
        this(PetWeight.class, forVariable(variable), INITS);
    }

    public QPetWeight(Path<? extends PetWeight> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPetWeight(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPetWeight(PathMetadata metadata, PathInits inits) {
        this(PetWeight.class, metadata, inits);
    }

    public QPetWeight(Class<? extends PetWeight> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.pet = inits.isInitialized("pet") ? new com.petlogue.duopetbackend.pet.entity.QPet(forProperty("pet"), inits.get("pet")) : null;
    }

}

