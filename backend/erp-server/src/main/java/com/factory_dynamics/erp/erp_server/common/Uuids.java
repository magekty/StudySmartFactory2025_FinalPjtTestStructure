package com.factory_dynamics.erp.erp_server.common;

import java.util.UUID;

public final class Uuids {
    private Uuids() {}
    public static String newId() {
        return UUID.randomUUID().toString();
    }
}