package org.xgame.context.usecase.case01.domain;

import org.xgame.context.Entity;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 10:08 PM
 */
public interface Domain {
    interface XY extends Entity.Data {
        double x(double... x);

        double y(double... y);
    }

    interface Z extends Entity.Data {
        double z(double... z);
    }

    interface Texture extends Entity.Data {
        String texture(String... texture);
    }

    interface PoV extends XY {
        double angle360(double... angle360);
    }

    interface Body extends PoV {
        double size(double... size);
    }


    interface Name extends Entity.Data {
        String name(String... name);
    }

    interface Life extends Entity.Data {
        double bar(double... bar);
    }

    interface Hourglass extends Entity.Data {
        double timeRemains(double... time);
    }


    interface CanStandOn extends Entity.Data {
    }

    interface Slip extends Entity.Data {
        double ratio(double... friction);
    }
}