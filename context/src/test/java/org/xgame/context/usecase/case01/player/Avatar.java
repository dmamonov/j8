package org.xgame.context.usecase.case01.player;

import org.xgame.context.Entity;
import org.xgame.context.usecase.case01.domain.Domain;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 9:14 PM
 */
public interface Avatar extends Entity.Action{
    default void move(final double dx, final double dy) {
        final Domain.XY xy = field(Domain.XY.class);
        change().plus(xy.x(), dx).plus(xy.y(), dy);
        System.out.println(xy.x()+", "+xy.y());
    }
}
