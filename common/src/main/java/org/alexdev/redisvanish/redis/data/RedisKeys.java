package org.alexdev.redisvanish.redis.data;


public enum RedisKeys {


    USER("redisvanish:user"),
    USER_UPDATE("redisvanish:user:update"),
    USER_SET_CACHE("redisvanish:user:setcache"),
    USER_CACHE_REQUEST("redisvanish:user:cacherequest"),
    USER_JOIN("redisvanish:user:join"),
    USER_LEAVE("redisvanish:user:leave"),
    REMOTE_USER("redisvanish:remoteuser"),
    REMOTE_USER_UPDATE("redisvanish:remoteuser:update"),
    VANISH_LEVELS("redisvanish:vanish:levels"),
    VANISH_LEVELS_REQUEST("redisvanish:vanish:levels:request"),
    VANISH_LEVELS_UPDATE("redisvanish:vanish:levels:update")
    ;


    private final String keyName;

    /**
     * @param keyName the name of the key
     */
    RedisKeys(final String keyName) {
        this.keyName = keyName;
    }

    /**
     * Use {@link #toString} instead of this method
     *
     * @return the name of the key
     */
    public String getKey() {
        return keyName;
    }

}
