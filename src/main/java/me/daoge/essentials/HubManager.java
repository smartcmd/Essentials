package me.daoge.essentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class HubManager {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path hubFile;
    private LocationRecord hubLocation;

    public HubManager(Path dataFolder) {
        this.hubFile = dataFolder.resolve("hub.json");
        load();
    }

    public LocationRecord getHub() {
        return hubLocation;
    }

    public void setHub(LocationRecord location) {
        this.hubLocation = location;
        save();
    }

    public boolean hasHub() {
        return hubLocation != null;
    }

    private void load() {
        if (!Files.exists(hubFile)) {
            log.info("Hub file not found, creating new one");
            hubLocation = null;
            return;
        }

        try {
            String json = Files.readString(hubFile);
            hubLocation = gson.fromJson(json, LocationRecord.class);
            log.info("Loaded hub location: {}", hubLocation);
        } catch (IOException e) {
            log.error("Failed to load hub location", e);
            hubLocation = null;
        }
    }

    private void save() {
        try {
            Files.createDirectories(hubFile.getParent());
            String json = gson.toJson(hubLocation);
            Files.writeString(hubFile, json);
            log.info("Saved hub location: {}", hubLocation);
        } catch (IOException e) {
            log.error("Failed to save hub location", e);
        }
    }
}
