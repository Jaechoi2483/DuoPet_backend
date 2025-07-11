package com.petlogue.duopetbackend.health.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPetHealthSchedule is a Querydsl query type for PetHealthSchedule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPetHealthSchedule extends EntityPathBase<PetHealthSchedule> {

    private static final long serialVersionUID = 2043100741L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPetHealthSchedule petHealthSchedule = new QPetHealthSchedule("petHealthSchedule");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath memo = createString("memo");

    public final com.petlogue.duopetbackend.pet.jpa.entity.QPet pet;

    public final DatePath<java.time.LocalDate> scheduleDate = createDate("scheduleDate", java.time.LocalDate.class);

    public final NumberPath<Long> scheduleId = createNumber("scheduleId", Long.class);

    public final StringPath scheduleTime = createString("scheduleTime");

    public final StringPath scheduleType = createString("scheduleType");

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QPetHealthSchedule(String variable) {
        this(PetHealthSchedule.class, forVariable(variable), INITS);
    }

    public QPetHealthSchedule(Path<? extends PetHealthSchedule> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPetHealthSchedule(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPetHealthSchedule(PathMetadata metadata, PathInits inits) {
        this(PetHealthSchedule.class, metadata, inits);
    }

    public QPetHealthSchedule(Class<? extends PetHealthSchedule> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.pet = inits.isInitialized("pet") ? new com.petlogue.duopetbackend.pet.jpa.entity.QPet(forProperty("pet"), inits.get("pet")) : null;
    }

}

