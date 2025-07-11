package com.petlogue.duopetbackend.info.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QHospitalEntity is a Querydsl query type for HospitalEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHospitalEntity extends EntityPathBase<HospitalEntity> {

    private static final long serialVersionUID = -2094895364L;

    public static final QHospitalEntity hospitalEntity = new QHospitalEntity("hospitalEntity");

    public final StringPath address = createString("address");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final StringPath gender = createString("gender");

    public final StringPath hospitalName = createString("hospitalName");

    public final StringPath loginId = createString("loginId");

    public final StringPath originalFilename = createString("originalFilename");

    public final StringPath phone = createString("phone");

    public final StringPath renameFilename = createString("renameFilename");

    public final StringPath role = createString("role");

    public final StringPath status = createString("status");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath userName = createString("userName");

    public QHospitalEntity(String variable) {
        super(HospitalEntity.class, forVariable(variable));
    }

    public QHospitalEntity(Path<? extends HospitalEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHospitalEntity(PathMetadata metadata) {
        super(HospitalEntity.class, metadata);
    }

}

