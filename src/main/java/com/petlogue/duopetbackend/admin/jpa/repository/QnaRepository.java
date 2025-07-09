package com.petlogue.duopetbackend.admin.jpa.repository;


import com.petlogue.duopetbackend.admin.jpa.entity.QnaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QnaRepository extends JpaRepository<QnaEntity, Integer> {


    Page<QnaEntity> findByContentType(String contentType, Pageable pageable);

}
