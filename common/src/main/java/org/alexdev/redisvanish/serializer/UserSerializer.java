package org.alexdev.redisvanish.serializer;

import com.google.gson.*;
import org.alexdev.redisvanish.data.User;
import org.alexdev.redisvanish.data.VanishContainer;
import org.alexdev.redisvanish.data.VanishProperty;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserSerializer implements JsonDeserializer<User>, JsonSerializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        UUID uuid = UUID.fromString(jsonObject.get("uuid").getAsString());
        String name = jsonObject.get("name").getAsString();
        Map<String, VanishContainer> vanishLevels = new ConcurrentHashMap<>();

        JsonObject vanishLevelsObject = jsonObject.getAsJsonObject("vanishLevels");
        for (Map.Entry<String, JsonElement> entry : vanishLevelsObject.entrySet()) {
            String server = entry.getKey();
            JsonObject vanishContainerObject = entry.getValue().getAsJsonObject();
            Map<VanishProperty, Boolean> properties = new ConcurrentHashMap<>();

            JsonObject propertiesObject = vanishContainerObject.getAsJsonObject("properties");
            for (Map.Entry<String, JsonElement> propertyEntry : propertiesObject.entrySet()) {
                VanishProperty property = VanishProperty.valueOf(propertyEntry.getKey());
                boolean value = propertyEntry.getValue().getAsBoolean();
                properties.put(property, value);
            }

            VanishContainer vanishContainer = new VanishContainer(properties);
            vanishLevels.put(server, vanishContainer);
        }

        return new User(uuid, name, vanishLevels);
    }

    @Override
    public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uuid", src.uuid().toString());
        jsonObject.addProperty("name", src.name());

        JsonObject vanishLevelsObject = new JsonObject();
        for (Map.Entry<String, VanishContainer> entry : src.vanishLevels().entrySet()) {
            String server = entry.getKey();
            VanishContainer vanishContainer = entry.getValue();

            JsonObject vanishContainerObject = new JsonObject();
            JsonObject propertiesObject = new JsonObject();

            for (Map.Entry<VanishProperty, Boolean> propertyEntry : vanishContainer.properties().entrySet()) {
                VanishProperty property = propertyEntry.getKey();
                boolean value = propertyEntry.getValue();
                propertiesObject.addProperty(property.name(), value);
            }

            vanishContainerObject.add("properties", propertiesObject);
            vanishLevelsObject.add(server, vanishContainerObject);
        }

        jsonObject.add("vanishLevels", vanishLevelsObject);
        return jsonObject;
    }
}
