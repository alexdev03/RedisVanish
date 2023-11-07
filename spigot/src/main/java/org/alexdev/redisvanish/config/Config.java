package org.alexdev.redisvanish.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

@Configuration()
@Getter
@SuppressWarnings("FieldMayBeFinal")
public class Config {
    @Comment("redis[s]://[password@]host[:port][/database][?option=value]")
    private String redisUri = "redis://localhost:6379/2?timeout=20s&clientName=RedisVanish";

    private String serverType = "Survival";

}
