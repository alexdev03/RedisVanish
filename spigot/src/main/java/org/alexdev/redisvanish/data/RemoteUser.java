package org.alexdev.redisvanish.data;

import java.util.UUID;


public record RemoteUser(UUID uuid, String name, boolean bypass, boolean vanished, int vanishLevel) {

}
