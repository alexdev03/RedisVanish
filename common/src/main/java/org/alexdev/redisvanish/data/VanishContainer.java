package org.alexdev.redisvanish.data;

import java.util.Map;

public record VanishContainer(Map<VanishProperty, Boolean> properties) {

    public boolean isVanished() {
        return properties.getOrDefault(VanishProperty.VANISH, false);
    }

    public boolean canPickup() {
        return properties.getOrDefault(VanishProperty.PICKUP, false);
    }

    public boolean canDamageOthers() {
        return properties.getOrDefault(VanishProperty.DAMAGE_OTHERS, false);
    }

    public boolean canDamageMe() {
        return properties.getOrDefault(VanishProperty.DAMAGE_ME, false);
    }

    public boolean hasNightVision() {
        return properties.getOrDefault(VanishProperty.NIGHT_VISION, false);
    }

    public boolean isDoubleShift() {
        return properties.getOrDefault(VanishProperty.DOUBLE_SHIFT, false);
    }

    public boolean hasActionBar() {
        return properties.getOrDefault(VanishProperty.ACTION_BAR, false);
    }

    public boolean hasPlayerInventory() {
        return properties.getOrDefault(VanishProperty.PLAYER_INVENTORY, false);
    }

    public boolean isSilent() {
        return properties.getOrDefault(VanishProperty.SILENT, false);
    }

    public void setVanished(boolean vanished) {
        properties.put(VanishProperty.VANISH, vanished);
    }

    public void setPickup(boolean pickup) {
        properties.put(VanishProperty.PICKUP, pickup);
    }

    public void setDamageOthers(boolean damageOthers) {
        properties.put(VanishProperty.DAMAGE_OTHERS, damageOthers);
    }

    public void setDamageMe(boolean damageMe) {
        properties.put(VanishProperty.DAMAGE_ME, damageMe);
    }

    public void setNightVision(boolean nightVision) {
        properties.put(VanishProperty.NIGHT_VISION, nightVision);
    }

    public void setDoubleShift(boolean doubleShift) {
        properties.put(VanishProperty.DOUBLE_SHIFT, doubleShift);
    }

    public void setActionBar(boolean actionBar) {
        properties.put(VanishProperty.ACTION_BAR, actionBar);
    }

    public void setPlayerInventory(boolean playerInventory) {
        properties.put(VanishProperty.PLAYER_INVENTORY, playerInventory);
    }

    public void setSilent(boolean silent) {
        properties.put(VanishProperty.SILENT, silent);
    }

}
