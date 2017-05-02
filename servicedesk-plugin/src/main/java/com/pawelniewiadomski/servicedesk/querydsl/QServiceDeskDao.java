package com.pawelniewiadomski.servicedesk.querydsl;

import com.atlassian.pocketknife.spi.querydsl.EnhancedRelationalPathBase;
import com.querydsl.core.types.dsl.NumberPath;

public class QServiceDeskDao extends EnhancedRelationalPathBase<QServiceDeskDao> {
    private static final long serialVersionUID = 2200321188083300593L;

    public final NumberPath<Integer> ID = createIntegerCol(CurrentSchema.ServiceDeskDao.ID).asPrimaryKey().build();
    public final NumberPath<Integer> PUBLIC_SIGNUP = createNumber(CurrentSchema.ServiceDeskDao.PUBLIC_SIGNUP, Integer.class);
    public final NumberPath<Long> PROJECT_ID = createNumber(CurrentSchema.ServiceDeskDao.PROJECT_ID, Long.class);

    QServiceDeskDao(String table) {
        super(QServiceDeskDao.class, table);
    }

    public interface CurrentSchema {
        interface ServiceDeskDao {
            String TABLE_NAME  = "SERVICEDESK";
            String ID = "ID";
            String PROJECT_ID = "PROJECT_ID";
            String PUBLIC_SIGNUP = "PUBLIC_SIGNUP";
        }
    }
}