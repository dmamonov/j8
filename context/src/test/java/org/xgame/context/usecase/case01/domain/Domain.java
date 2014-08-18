package org.xgame.context.usecase.case01.domain;

import org.xgame.context.Entity;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 10:08 PM
 */
public interface Domain {
    interface XY extends Entity.Data {
        double x();

        double y();
    }

    interface Texture extends Entity.Data {
        String texture();
    }

    interface PoV extends XY {
        double angle360();
    }

    interface Body extends PoV {
        double size();
    }


    interface Name extends Entity.Data {
        String name();
    }

    interface Life extends Entity.Data {
        double bar();
    }

    interface Hourglass extends Entity.Data {
        double timeRemains();
    }

}