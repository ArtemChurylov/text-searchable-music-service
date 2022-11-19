package com.music.bot.controller;

import com.music.bot.service.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.music.bot.controller.RestMapping.AUDIO_API;

@RestController
@RequestMapping(AUDIO_API)
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    @GetMapping
    public String searchAudioByLyrics(@RequestParam String lyrics) {
        return audioService.searchAudioByLyrics(lyrics);
    }
}
