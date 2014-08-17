package org.xgame.context.impl;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dmitry.mamonov
 *         Created: 2014-08-17 12:31 AM
 */
public class DefaultInstanceCache {
    private static final ConcurrentHashMap<Class, Object> cacheMap = new ConcurrentHashMap<>();
    public static <T> T defaultInterfaceInstance(final Class<T> type) {
        final Object cached = cacheMap.get(type);
        if (cached!=null){
            //noinspection unchecked
            return (T)cached;
        } else {
            try {
                final Object newOne = new InterfaceInstance(type).defineClass().newInstance();
                final Object concurrentInstance = cacheMap.putIfAbsent(type, newOne);
                //noinspection unchecked
                return (T) (concurrentInstance != null ? concurrentInstance : newOne);
            } catch (final RuntimeException re){
                throw re;
            } catch (final Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
