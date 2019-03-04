package be.igorarshinov.avatar_creator.data;

import android.support.annotation.NonNull;
import android.util.LruCache;

public class AppCache<T> {

    private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    private final int cacheSize = maxMemory / 6;

    private LruCache<String, T> cache;

    private static volatile AppCache appCache;

    private AppCache() {
        if (cache == null) {
            cache = new LruCache<String, T>(cacheSize);
        }
    }

    public static <T> T getInstance() {
        if (appCache == null) {
            synchronized (AppCache.class) {
                if (appCache == null) {
                    appCache = new AppCache();
                    return (T) appCache;
                }
            }
        }
        return (T) appCache;
    }

    public LruCache<String, T> getCache() {
        return cache;
    }

    public void put(@NonNull String key, T t) {
        if (cache == null) {
            cache = new LruCache<String, T>(cacheSize);
        }
        cache.put(key, t);
    }

    public <T> T get(@NonNull String key) {
        if (key == null) {
            return null;
        }
        if (cache == null) {
            return null;
        }
        return (T) cache.get(key);
    }

    public <T> T remove(String key) {
        return (T) cache.remove(key);
    }
}
