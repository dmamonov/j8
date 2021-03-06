package org.xgame.context.usecase;

import org.junit.Test;
import org.xgame.context.Entity;
import org.xgame.context.impl.Server;
import org.xgame.context.usecase.case01.Map01;
import org.xgame.context.usecase.case01.domain.Domain;
import org.xgame.context.usecase.case01.player.Avatar;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-16 11:37 PM
 */
public class TestUseCases {

    public interface World extends Entity.Action {
        default void loop() {
            query(Ability.OnLoop.class, a -> a.step(0.9));
            dispose();
        }

        interface Ability {
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
                fabric(new Bullet.Factory(field(Domain.Body.class)) {

                });
            }
        }

        interface Bullet {
            class Factory implements Fabric {
                final Domain.Body body;

                public Factory(final Domain.Body body) {
                    this.body = body;
                }

                @Override
                public void produce() {
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

    }


    static class KeyAction {
        final int code;
        final double dx;
        final double dy;

        KeyAction(final int code, final double dx, final double dy) {
            this.code = code;
            this.dx = dx;
            this.dy = dy;
        }

        public int getCode() {
            return code;
        }

        public double getDx() {
            return dx;
        }

        public double getDy() {
            return dy;
        }
    }

    @Test
    public void testCreate() throws Exception {
        Entity.seed(root -> {
            Map01.load(root);

            final World world = root.addAction(World.class).action(World.class);

            //create named entity:
            final Entity.Ref johnDoeRef = root.create(e -> {
                final Domain.Name name = e.data(Domain.Name.class);
                e.change().assign(name.name(), "John Doe");
                return e.self();
            });
            johnDoeRef.with(e -> System.out.println(e.data(Domain.Name.class).name()));

            //create entity with factory:
            final Entity.Ref trooper1 = root.create(new World.Humanoid.Human.Trooper.Factory() {
                @Override
                public void setupBody(final Domain.Body body) {
                    change().assign(body.x(), 1.0)
                            .assign(body.y(), 1.0)
                            .assign(body.size(), 0.5)
                            .assign(body.angle360(), 90.0);
                }
            });

            trooper1.with(e -> System.out.println(e.describe().replace('\n', '~')));

            trooper1.with(e -> e.action(World.Ability.Trigger.class).fire());

            System.out.println("DONE");
            final AtomicReference<String> state = new AtomicReference<>(root.toJson().toString());
            final Server server = Server.create(time->state.get());
            server.start();
            try {
                Desktop.getDesktop().open(new File("context/src/main/resources/frontend.html"));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            while (!server.getPressedKeys().contains(27)){
                for (final KeyAction keyAction : new KeyAction[]{
                        new KeyAction(37, -0.1, 0.0), //left
                        new KeyAction(39, +0.1, 0.0), //right
                        new KeyAction(38, 0.0, -0.1), //up
                        new KeyAction(40, 0.0, +0.1), //down
                }) {
                    if (server.getPressedKeys().contains(keyAction.getCode())) {
                        root.query(Avatar.class, avatar -> {
                            final double scale = 0.4;
                            avatar.move(keyAction.getDx()*scale, keyAction.getDy()*scale);
                        });
                    }
                }
                final long start = System.currentTimeMillis();
                world.loop();
                state.set(root.toJson().toString());
                try {
                    final long passed = System.currentTimeMillis() - start;
                    Thread.sleep(Math.max(1,30- passed));
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.exit(0);
        });
    }
}
