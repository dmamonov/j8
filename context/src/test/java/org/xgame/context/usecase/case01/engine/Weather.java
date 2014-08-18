package org.xgame.context.usecase.case01.engine;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 9:09 PM
 */
public interface Weather {
    interface Event {
        interface Temperature {
            void temperature(double degrees);
        }

        interface Rainfall {
            void rain(double fall);
        }

        interface SolarRadiation {
            void intensity(double calories);
        }

        interface Wind {
            void wind(double speed);
        }
    }
}
