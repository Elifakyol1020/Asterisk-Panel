package com.netgsm.asterisk.repository;

import com.netgsm.asterisk.entity.CdrDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CdrJpaRepository extends JpaRepository<CdrDocument, String>, JpaSpecificationExecutor<CdrDocument> { }
