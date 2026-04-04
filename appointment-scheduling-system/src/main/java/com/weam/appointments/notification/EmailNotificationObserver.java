package com.weam.appointments.notification;

import com.weam.appointments.domain.User;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;


public class EmailNotificationObserver implements NotificationObserver {
    private final String username;
    private final String password;
    private final Session session;

    public EmailNotificationObserver() {
        Dotenv dotenv = Dotenv.load();
        this.username = dotenv.get("EMAIL_USERNAME");
        this.password = dotenv.get("EMAIL_PASSWORD");
        System.out.println("Email username: " + this.username);
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "*");
        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public void notify(User user, String message) {
        try {
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(username));
            // Assume username is the email address (or append domain)
           // String toEmail = user.getUsername().contains("@") 
            
            String toEmail = user.getEmail(); 
           // String toEmail ="hamoda2004alomari@gmail.com";
            
               // ? user.getUsername() 
              //  : user.getUsername() + "@example.com";
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            mimeMessage.setSubject("Appointment Reminder");
            mimeMessage.setText(message);
            Transport.send(mimeMessage);
            System.out.println("Email sent to " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email", e);
        }
    }
}