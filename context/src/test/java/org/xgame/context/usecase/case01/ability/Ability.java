package org.xgame.context.usecase.case01.ability;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 9:20 PM
 */
public interface Ability {
    interface Damageable {
        void harm(double damage);
    }

    interface Pickable {
        void catchUp();
    }

    interface Introspectable {
        String view();
    }

    interface Pushable {
        void push();
    }

    interface Heatable {
        void heat(double temperature, double exposition);
    }


    interface Controllable {
        void forward();
        void backward();
        void left();
        void right();
        void fire();
        void change();
        void use();
        void hide();
    }
}
