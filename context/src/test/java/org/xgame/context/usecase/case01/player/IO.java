package org.xgame.context.usecase.case01.player;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.xgame.context.Entity;
import org.xgame.context.usecase.case01.domain.Domain;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.transform;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-18 9:22 PM
 */
public interface IO extends Entity {
    default String read() {
        return toJson().toString();
    }

    default void write(final String data) {
        if (data != null && data.startsWith("[")) {
            final JsonObject pressedJson = new JsonParser().parse(data).getAsJsonObject();
            final ImmutableSet<Integer> pressed = copyOf(transform(pressedJson.entrySet(), e->Integer.parseInt(e.getKey())));
            data(Domain.Keys.class).pressed(pressed);
        }
    }
}
