package com.pawelniewiadomski.servicedesk.querydsl;

import com.atlassian.pocketknife.spi.querydsl.EnhancedRelationalPathBase;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

public class QViewportDao extends EnhancedRelationalPathBase<QViewportDao> {
    private static final long serialVersionUID = 1899816444L;

    public final NumberPath<Integer> ID = createIntegerCol(CurrentSchema.ViewportDao.ID).asPrimaryKey().build();
    public final StringPath DESCRIPTION = createString(CurrentSchema.ViewportDao.DESCRIPTION);
    public final StringPath KEY = createString(CurrentSchema.ViewportDao.KEY);
    public final NumberPath<Integer> LOGO_ID = createNumber(CurrentSchema.ViewportDao.LOGO_ID, Integer.class);
    public final StringPath NAME = createString(CurrentSchema.ViewportDao.NAME);
    public final NumberPath<Long> PROJECT_ID = createNumber(CurrentSchema.ViewportDao.PROJECT_ID, Long.class);
    public final NumberPath<Integer> THEME_ID = createNumber(CurrentSchema.ViewportDao.THEME_ID, Integer.class);
    public final BooleanPath SEND_EMAIL_NOTIFICATIONS = createBoolean(CurrentSchema.ViewportDao.SEND_EMAIL_NOTIFICATIONS);

    QViewportDao(String table) {
        super(QViewportDao.class, table);
    }

    public interface CurrentSchema {
        interface ViewportDao {
            String TABLE_NAME  = "VIEWPORT";
            String ID = "ID";
            String KEY = "KEY";
            String PROJECT_ID = "PROJECT_ID";
            String NAME = "NAME";
            String DESCRIPTION = "DESCRIPTION";
            String SEND_EMAIL_NOTIFICATIONS = "SEND_EMAIL_NOTIFICATIONS";
            String LOGO_ID = "LOGO_ID";
            String THEME_ID = "THEME_ID";
        }
    }
}