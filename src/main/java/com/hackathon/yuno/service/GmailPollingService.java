package com.hackathon.yuno.service;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.hackathon.yuno.util.EmailContent;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;

@Service
public class GmailPollingService {

    @Autowired
    public MerchantService merchantService;


    @Value("${app.mail.bot.email}")
    private String botMail;

    @Value("${app.mail.bot.password}")
    private String botMailPassword;

    @Scheduled(fixedDelay = 5000)
    public void checkEmails(){
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");

        try{
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", botMail , botMailPassword);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            for(Message message: messages){
                String sender = message.getFrom()[0].toString();

                EmailContent content = parseMessageContent(message);


                merchantService.proccessFromEmail(sender, content.bodyText, content.attachmentStream);
                

                message.setFlag(Flags.Flag.SEEN, true);

            }
            
            inbox.close(false);
            store.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private EmailContent parseMessageContent(Message message) throws Exception {
            EmailContent content = new EmailContent();
            Object msgContent = message.getContent();

            if (msgContent instanceof MimeMultipart) {
                extractMultipart((MimeMultipart) msgContent, content);
            } else {
                content.bodyText = message.getContent().toString();
            }
            return content;
    }

    private void extractMultipart(MimeMultipart multipart, EmailContent content) throws Exception {
        for(int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            if(Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) && content.attachmentStream == null){
                content.attachmentStream = bodyPart.getInputStream();
            } 
            else if (bodyPart.isMimeType("text/plain")) {
                content.bodyText = bodyPart.getContent().toString();
            }
            else if (bodyPart.isMimeType("multipart/*")) {
                extractMultipart((MimeMultipart) bodyPart.getContent(), content);
            }
        }
    }


    
        
}
