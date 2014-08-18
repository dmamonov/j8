package org.xgame.context.usecase.case01.weapon;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 9:10 PM
 */
public interface Weapon {
    interface Melee extends Weapon {
        void attack();

        interface Knife {

        }
    }

    interface Firearm extends Weapon{
        void fire();
        void reload();

        interface Pistol {

        }
        interface Rifle {

        }
        interface AssaultRifle {

        }
    }
}
