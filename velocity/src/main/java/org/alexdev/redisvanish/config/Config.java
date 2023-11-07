package org.alexdev.redisvanish.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import org.alexdev.redisvanish.data.VanishLevel;

import java.util.Map;

@Configuration()
@Getter
@SuppressWarnings("FieldMayBeFinal")
public class Config {
    @Comment("redis[s]://[password@]host[:port][/database][?option=value]")
    private String redisUri = "redis://localhost:6379/2?timeout=20s&clientName=RedisVanish";

    private Map<Integer, VanishLevel> vanishLevels = Map.of(
            1, new VanishLevel("Admin", "redisvanish.level.admin"),
            2, new VanishLevel("Moderator", "redisvanish.level.moderator"),
            3, new VanishLevel("Helper", "redisvanish.level.helper")
    );


}
