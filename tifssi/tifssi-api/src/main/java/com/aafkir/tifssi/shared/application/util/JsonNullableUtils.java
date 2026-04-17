package com.aafkir.tifssi.shared.application.util;

import org.openapitools.jackson.nullable.JsonNullable;

public final class JsonNullableUtils {

    private JsonNullableUtils() {
    }

    public static <T> boolean isDefined(JsonNullable<T> value) {
        return value != null && !JsonNullable.undefined().equals(value);
    }

    public static <T> T unwrap(JsonNullable<T> value) {
        return value == null ? null : value.orElse(null);
    }
}

