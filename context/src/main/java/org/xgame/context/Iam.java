package org.xgame.context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.xgame.context.impl.DefaultInstanceCache.defaultInterfaceInstance;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-17 12:59 AM
 */
class Iam {
    static final Entity entity = defaultInterfaceInstance(Entity.class);
    static final Entity.DataOperation dataOperation = defaultInterfaceInstance(Entity.DataOperation.class);
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
}
