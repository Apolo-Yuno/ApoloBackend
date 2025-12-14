package com.hackathon.yuno.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.bolt.App;
import com.slack.api.model.event.MessageEvent;

@Configuration
public class SlackIntegrationService {

    @Value("${slack.app.token}")
    private String appToken;

    @Value("${slack.bot.token}")
    private String botToken;
        
    @Autowired
    private MerchantService merchantService;

    @Bean
    public SocketModeApp startLackApp() throws Exception {
        App app = new App();

        app.event(MessageEvent.class, (req, ctx) -> {
            MessageEvent event = req.getEvent();
            
            
            if (event.getBotId() != null) return ctx.ack();

            String text = event.getText();
            String user = event.getUser();

            InputStream fileStream = null;


            if(event.getFiles() != null && !event.getFiles().isEmpty()) {
                String downloadUrl = event.getFiles().get(0).getUrlPrivate();
                String fileName = event.getFiles().get(0).getName();


                System.out.println("File in Slack: " + fileName);
                fileStream = downloadFileFromSlack(downloadUrl);
            }

        merchantService.proccessFromEmail("SlackUser-" + user, text, fileStream);

        ctx.client().reactionsAdd(r -> r
                .channel(event.getChannel())
                .name("eyes")
                .timestamp(event.getTs())
            );
            
            return ctx.ack();

        });

        SocketModeApp socketModeApp = new SocketModeApp(appToken, app);
        socketModeApp.startAsync();

        return socketModeApp;
    }

    private InputStream downloadFileFromSlack(String urlString){
        try{
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + botToken);

            try(InputStream is = connection.getInputStream()) {
                return new ByteArrayInputStream(is.readAllBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
