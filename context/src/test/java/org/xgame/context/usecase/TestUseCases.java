package org.xgame.context.usecase;

import org.junit.Test;
import org.xgame.context.Entity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-16 11:37 PM
 */
public class TestUseCases {

    public interface World extends Entity.Action {
        default void loop() {
            System.out.println("============== World loop: Begin ==============");
            query(Ability.OnLoop.class, a -> a.step(0.9));
            dispose();
        }


        interface Domain {
            interface XY extends Data {
                double x();

                double y();
            }

            interface PoV extends XY {
                double angle360();
            }

            interface Body extends PoV {
                double size();
            }


            interface Name extends Data {
                String name();
            }

            interface Life extends Data {
                double bar();
            }

            interface Hourglass extends Data {
                double timeRemains();
            }

        }

        interface Ability {
            interface Damageable {
                default boolean isAlive() {
                    return true;
                }

                default void damage(final double loss) {

                }
            }

            interface Inflammable {
                default void burn(final double temperature) {

                }
            }

            interface Moveable {
                default Moveable rotate(final double angle360) {
                    return this;
                }

                default Moveable move(final double distance) {
                    return this;
                }
            }

            interface Trigger extends Action {
                default void fire() {
                    final Weapon weapon = find(Weapon.class);
                    if (weapon != null) {
                        weapon.shot();
                    } else {
                        System.out.println("No Weapon!");
                    }
                }
            }

            interface Timeout extends Action {
                void timeout();

                interface DestroyOnTimeout extends Timeout {
                    default void timeout() {
                        dispose();
                        System.out.println("Disposed by Timeout: " + describeOneLine());
                    }
                }
            }

            abstract interface OnLoop extends Action {
                void step(double timeDelta);

                interface TimeoutOnLoop extends OnLoop {
                    default void step(final double timeDelta) {
                        final Domain.Hourglass hourglass = field(Domain.Hourglass.class);
                        change().plus(hourglass.timeRemains(), -timeDelta);
                        if (hourglass.timeRemains() < 0) {
                            action(Timeout.class).timeout();
                        } else {
                            System.out.println("Time remains: " + hourglass.timeRemains());
                        }
                    }
                }

                interface MoveOnLoop extends OnLoop {
                    default void step(final double timeDelta) {
                        final double speed = 2.0;
                        final Domain.Body body = field(Domain.Body.class);
                        final double angleRad = body.angle360() / 180 * Math.PI;
                        final double dx = speed * Math.cos(angleRad);
                        final double dy = speed * Math.sin(angleRad);
                        change().plus(body.x(), dx).plus(body.y(), dy);
                        System.out.println("Move On Loop: " + describeOneLine());
                    }
                }
            }
        }


        interface Humanoid extends Entity {

            interface Human extends Humanoid {

                interface Trooper extends Human {
                    interface Factory extends Entity, Configurer {
                        final AtomicInteger nameSeq = new AtomicInteger();

                        default void setupBody(final Domain.Body body) {
                            change().assign(body.size(), 0.5);
                        }

                        default void setupName(final Domain.Name name) {
                            change().assign(name.name(), "John Doe " + nameSeq.incrementAndGet());
                        }

                        @Override
                        default Ref apply(final Entity entity) {
                            addAction(Ability.Trigger.class);
                            addAction(Weapon.class);
                            setupBody(data(Domain.Body.class));
                            setupName(data(Domain.Name.class));
                            return self();
                        }
                    }
                }

            }
        }

        interface Weapon extends Action {
            default void shot() {
                create(new Bullet.Factory(field(Domain.Body.class)) {

                });
            }
        }

        interface Bullet {
            class Factory implements Fabricator {
                final Domain.Body body;

                public Factory(final Domain.Body body) {
                    this.body = body;
                }

                @Override
                public void fabric() {
                    change().assign(Domain.Body.class, body);
                    addAction(Ability.OnLoop.MoveOnLoop.class);
                    { //set finite lifetime:
                        addAction(Ability.OnLoop.TimeoutOnLoop.class);
                        change().assign(data(Domain.Hourglass.class).timeRemains(), 5.0);
                        addAction(Ability.Timeout.DestroyOnTimeout.class);
                    }
                    System.out.println("Bullet created: " + describeOneLine());
                }
            }
        }

        interface Landscape {
            interface Ground {
                interface Liquid {

                }

                interface Solid {
                    interface Soil {

                    }

                    interface Sand {

                    }

                    interface Scree {

                    }

                    interface Road {

                    }
                }
            }

            interface Wall {
                interface ConcreteFence {

                }

                interface WoodenFence {

                }
            }

            interface Cover {

            }
        }
    }


    @Test
    public void testCreate() throws Exception {
        Entity.seed(root -> {
            final World world = root.addAction(World.class).action(World.class);

            //create named entity:
            final Entity.Ref johnDoeRef = root.create(e -> {
                final World.Domain.Name name = e.data(World.Domain.Name.class);
                e.change().assign(name.name(), "John Doe");
                return e.self();
            });
            johnDoeRef.with(e -> System.out.println(e.data(World.Domain.Name.class).name()));

            //create entity with factory:
            final Entity.Ref trooper1 = root.create(new World.Humanoid.Human.Trooper.Factory() {
                @Override
                public void setupBody(final World.Domain.Body body) {
                    change().assign(body.x(), 1.0)
                            .assign(body.y(), 1.0)
                            .assign(body.size(), 0.5)
                            .assign(body.angle360(), 90.0);
                }
            });

            trooper1.with(e -> System.out.println(e.describe().replace('\n', '~')));

            trooper1.with(e -> e.action(World.Ability.Trigger.class).fire());

            for (int i = 0; i < 10; i++) {
                world.loop();
            }
            System.out.println("DONE");
        });
    }
}
