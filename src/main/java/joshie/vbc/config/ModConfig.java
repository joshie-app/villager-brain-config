package joshie.vbc.config;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModConfig {
    @SerializedName("walk_on_block_penalties")
    public Map<String, Double> walkOnBlockPenalties = new HashMap<>();

    @SerializedName("walk_through_block_penalties")
    public Map<String, Double> walkThroughBlockPenalties = new HashMap<>();

    @SerializedName("actions_penalties")
    public Map<String, Double> actionsPenalties = new HashMap<>();

    @SerializedName("professions")
    public Map<String, ModConfig> professions = new HashMap<>();

    @SerializedName("avoids")
    public List<AvoidEntry> avoids = new ArrayList<>();
}