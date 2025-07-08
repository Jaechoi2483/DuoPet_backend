package com.petlogue.duopetbackend.admin.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFaqEntity is a Querydsl query type for FaqEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFaqEntity extends EntityPathBase<FaqEntity> {

    private static final long serialVersionUID = 2093978215L;

    public static final QFaqEntity faqEntity = new QFaqEntity("faqEntity");

    public final StringPath answer = createString("answer");

    public final NumberPath<Integer> faqId = createNumber("faqId", Integer.class);

    public final StringPath question = createString("question");

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public QFaqEntity(String variable) {
        super(FaqEntity.class, forVariable(variable));
    }

    public QFaqEntity(Path<? extends FaqEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFaqEntity(PathMetadata metadata) {
        super(FaqEntity.class, metadata);
    }

}

