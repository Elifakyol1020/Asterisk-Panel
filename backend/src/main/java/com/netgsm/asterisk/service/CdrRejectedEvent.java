package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.request.CdrInput;
/** Synchronous hook for a future durable dead-letter adapter. Source must retain rejected input. */
public record CdrRejectedEvent(CdrInput input, String code) {}

