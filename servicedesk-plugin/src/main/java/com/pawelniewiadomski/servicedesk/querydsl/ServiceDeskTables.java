package com.pawelniewiadomski.servicedesk.querydsl;

public class ServiceDeskTables {
    public static final String AO_PREFIX = "AO_54307E_";

    public static String withAoPrefixUpperCase(final String tableName) {
        return (AO_PREFIX + tableName).toUpperCase();
    }

    public static final QViewportDao VIEWPORT = new QViewportDao(
            withAoPrefixUpperCase(QViewportDao.CurrentSchema.ViewportDao.TABLE_NAME));
}
