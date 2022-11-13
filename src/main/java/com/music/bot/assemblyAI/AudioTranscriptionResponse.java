package com.music.bot.assemblyAI;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AudioTranscriptionResponse {

    String id;
    String status;
    String text;
}
