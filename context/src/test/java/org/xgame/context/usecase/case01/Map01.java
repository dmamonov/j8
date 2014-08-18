package org.xgame.context.usecase.case01;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.xgame.context.Entity;
import org.xgame.context.usecase.case01.domain.Domain;
import org.xgame.context.usecase.case01.real.environment.Env;

import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 9:06 PM
 */
public class Map01 {
    public static void load(final Entity root) {
        final ImmutableMap<Character, Class<? extends Entity.Constructor>> constructors = ImmutableMap.<Character, Class<? extends Entity.Constructor>>builder()
                .put(' ', Env.Surface.Solid.Soil.class)
                .put('s', Env.Surface.Solid.Sand.class)
                .put('W', Env.Surface.Liquid.Water.class)
                .put('X', Env.Barrier.Rock.class)
                .build();


        final AtomicInteger y = new AtomicInteger();
        final AtomicInteger x = new AtomicInteger();
        for (final String line : ImmutableList.of(
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                "X                                             X",
                "X                                             X",
                "X                             ssssssssss      X",
                "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWsss     X",
                "Xsssssssssssssssssssssssssss         Wssssss  X",
                "X                                    WWWWWWWWWW",
                "X                                             X",
                "X                                             X",
                "X                                             X",
                "X                                             X",
                "X                                             X",
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        )) {

            for (final char code : line.toCharArray()) {
                checkState(constructors.containsKey(code),"missing code: %s", code);
                root.build().with(e -> {
                    final Domain.XY xy = e.data(Domain.XY.class);
                    e.change().assign(xy.x(), (double) x.get()).assign(xy.y(), (double) y.get());

                }).mixin(constructors.get(code));

                x.incrementAndGet();
            }
            y.incrementAndGet();
        }
    }
}
