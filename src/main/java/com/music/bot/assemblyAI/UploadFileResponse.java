package com.music.bot.assemblyAI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadFileResponse {

    @JsonProperty("upload_url")
    String uploadUrl;
}
