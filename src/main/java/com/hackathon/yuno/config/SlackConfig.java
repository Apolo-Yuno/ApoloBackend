package com.hackathon.yuno.config;

import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SlackConfig {

    @Value("${slack.app.token}")
    private String appToken;

    @Bean
    @Scope("singleton")
    public App slackApp() {
        return new App();
    }

    @Bean
    @Scope("singleton")
    public SocketModeApp socketModeApp(App app) throws Exception {
        SocketModeApp socketModeApp = new SocketModeApp(appToken, app);
        return socketModeApp;
    }
}
