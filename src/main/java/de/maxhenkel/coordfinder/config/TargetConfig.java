package de.maxhenkel.coordfinder.config;

import de.maxhenkel.configbuilder.CommentedProperties;
import de.maxhenkel.configbuilder.CommentedPropertyConfig;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TargetConfig extends CommentedPropertyConfig {

    private final Map<UUID, String> targets;

    public TargetConfig(Path path) {
        super(new CommentedProperties(false));
        this.path = path;
        reload();
        targets = new HashMap<>();
        Map<String, String> entries = getEntries();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            try {
                UUID playerId = UUID.fromString(entry.getKey());
                targets.put(playerId, entry.getValue());
            } catch (IllegalArgumentException e) {
                // Ignore malformed UUID entries to keep config resilient.
            }
        }
    }

    public void setTarget(UUID player, String placeName) {
        targets.put(player, placeName);
        set(player.toString(), placeName);
        save();
    }

    public void clearTarget(UUID player) {
        targets.remove(player);
        properties.remove(player.toString());
        save();
    }

    @Nullable
    public String getTarget(UUID player) {
        return targets.get(player);
    }

    public Map<UUID, String> getTargets() {
        return targets;
    }

}
