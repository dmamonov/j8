package org.xgame.context.usecase.case01.engine;

import org.xgame.context.usecase.case01.domain.Domain;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 9:09 PM
 */
public interface Physics {
    interface Shape {

    }

    interface Body {
        Domain.XY xy();
        Domain.XY speed();
        Shape shape();
    }

    interface Collider {

    }

    interface Solver {
        default void solve(final double interval, final Iterable<Body> dynamics, final Collider collider) {
            checkArgument(interval > 0.0);

        }
    }
}
