package com.github.nottyl.earwormsbot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import reactor.core.publisher.Mono;

import static com.github.nottyl.earwormsbot.Main.player;
import static com.github.nottyl.earwormsbot.Main.provider;

public class Commands {
    public static final Map<String, Command> commands = new HashMap<>();

    interface Command {
        void execute(MessageCreateEvent event);
    }
    static {
        commands.put("hello", event -> event.getMessage()
                .getChannel().block()
                .createMessage("Hello to you too!").block());
        commands.put("join", event -> {
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        event.getMessage().getChannel().block().createMessage("Joined the Voice Channel.").block();
                        channel.join(spec -> spec.setProvider(provider)).block();
                    }
                }
            }
        });
        final TrackScheduler scheduler = new TrackScheduler(player);
        commands.put("play", event -> Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> Arrays.asList(content.split(" ")))
                .doOnNext(command -> Main.playerManager.loadItem(command.get(1), scheduler))
                .then()
                .block());
        commands.put("pause", event -> Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> Arrays.asList(content.split(" ")))
                .doOnNext(command -> Main.playerManager.loadItem(command.get(5), scheduler))
                .then()
                .block());
    }

}
