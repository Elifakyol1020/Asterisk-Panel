package com.netgsm.asterisk.asterisk.realtime.entity;

import java.io.Serializable;

public record AsteriskQueueMemberId(String queueName, String interfaceName) implements Serializable { }
