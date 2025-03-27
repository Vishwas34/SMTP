package com.bulk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GmailController {

    @Autowired
    private GmailService gmailService;

    // Example: http://localhost:8080/sendEmail?to=recipient@example.com&subject=Test&body=Hello
    @GetMapping("/sendEmail")
    public ResponseEntity<String> sendEmail(OAuth2AuthenticationToken oauth2Token,
                                              @RequestParam String to,
                                              @RequestParam String subject,
                                              @RequestParam String body) {
        try {
            gmailService.sendEmail(oauth2Token, to, subject, body);
            return ResponseEntity.ok("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error sending email: " + e.getMessage());
        }
    }
}
