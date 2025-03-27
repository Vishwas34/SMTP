package com.bulk;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

@Service
public class GmailService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final com.google.api.client.http.HttpTransport httpTransport;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    public GmailService(OAuth2AuthorizedClientService authorizedClientService) throws GeneralSecurityException, IOException {
        this.authorizedClientService = authorizedClientService;
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    public void sendEmail(OAuth2AuthenticationToken oauth2Token, String to, String subject, String bodyText)
            throws IOException, MessagingException {
        // Retrieve the authorized client from Spring Security
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                oauth2Token.getAuthorizedClientRegistrationId(), oauth2Token.getName());

        // Get the access token from the authorized client
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        // Build Google Credential using the access token
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(accessToken);

        // Build the Gmail API client
        Gmail gmailService = new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("BulkEmailApp")
                .build();

        // Create the email content
        MimeMessage email = createEmail(to, oauth2Token.getPrincipal().getAttribute("email"), subject, bodyText);
        Message message = createMessageWithEmail(email);

        // Send the email using Gmail API (the user "me" indicates the authenticated user)
        gmailService.users().messages().send("me", message).execute();
    }

    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        MimeMultipart multipart = new MimeMultipart();
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText);
        multipart.addBodyPart(textPart);
        email.setContent(multipart);
        return email;
    }

    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        String encodedEmail = Base64.encodeBase64URLSafeString(buffer.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
