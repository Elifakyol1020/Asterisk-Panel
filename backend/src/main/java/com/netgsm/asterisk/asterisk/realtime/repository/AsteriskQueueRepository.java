package com.netgsm.asterisk.asterisk.realtime.repository;

import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskQueue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsteriskQueueRepository extends JpaRepository<AsteriskQueue, String> { }
