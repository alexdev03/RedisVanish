package org.alexdev.redisvanish.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;

import java.util.List;

@Configuration()
@Getter
@SuppressWarnings("FieldMayBeFinal")
public class Config {
    @Comment("redis[s]://[password@]host[:port][/database][?option=value]")
    private String redisUri = "redis://localhost:6379/2?timeout=20s&clientName=RedisVanish";

    private String serverType = "survival";

    @Comment("The list of commands that are checked when players try to tab complete them by removing vanished players")
    private List<String> commandsToClean = List.of("tp", "tpa", "tpahere");

}
