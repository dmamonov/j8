package org.xgame.context.usecase.case01.engine;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 9:09 PM
 */
public interface Physics {
    interface Shape {
        interface Box extends Shape {

        }
        interface Sphere extends Shape {

        }
    }

    final class Vec {
        double x;
        double y;

        void assign(final Vec origin, final Vec plus, final double interpolate) {
            this.x = origin.x + plus.x*interpolate;
            this.y = origin.y + plus.y * interpolate;
        }
        void assign(final Vec origin){
            this.x = origin.x;
            this.y = origin.y;
        }
    }

    final class Body {
        final Vec origin = new Vec();
        final Vec target = new Vec();
        final Vec speed = new Vec();
    }

    interface Collider {
        boolean isCollide(Body body);
    }

    interface Solver {
        default void solve(final double interval, final Iterable<Body> dynamics, final Collider collider) {
            checkArgument(interval > 0.0);
            for (final Body body : dynamics) {
                for(double depth=1.0;depth>0;depth-=0.05) {
                    body.target.assign(body.origin, body.speed, interval*depth);
                    if (!collider.isCollide(body)) {
                        break;
                    }
                }
            }
        }
    }
}
