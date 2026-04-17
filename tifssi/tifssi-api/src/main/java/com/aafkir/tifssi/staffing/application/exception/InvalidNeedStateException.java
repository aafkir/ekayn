package com.aafkir.tifssi.staffing.application.exception;

import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;

public class InvalidNeedStateException extends RuntimeException {

    public InvalidNeedStateException(Long needId, NeedStatus currentStatus, String reason) {
        super("Need %d cannot be won: current status is %s. %s"
                .formatted(needId, currentStatus, reason));
    }
}

