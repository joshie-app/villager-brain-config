package joshie.vbc.config;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProfessionPenaltyConfig {
    @SerializedName("avoids")
    public List<AvoidEntry> avoids;
}