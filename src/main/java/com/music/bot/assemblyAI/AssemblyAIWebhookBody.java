package com.music.bot.assemblyAI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssemblyAIWebhookBody {

    @JsonProperty("transcript_id")
    String transcriptId;
    String status;
}
