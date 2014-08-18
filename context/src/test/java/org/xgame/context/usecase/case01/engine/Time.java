package org.xgame.context.usecase.case01.engine;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 9:09 PM
 */
public interface Time {
    interface Alarm {
        void schedule(double delay, Class<? extends Signal> callback);

        interface Signal {
            void onAlarm();
        }
    }
}
