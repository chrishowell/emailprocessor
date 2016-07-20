package xyz.howell.wedding.lambda.emailprocessor.model;

import java.util.ArrayList;
import java.util.List;

public class Email {
    private String sender;
    private String subject;
    private String content;
    private List<Attachment> attachments;

    private Email(String sender, String subject, String content, List<Attachment> attachments) {
        this.sender = sender;
        this.subject = subject;
        this.content = content;
        this.attachments = attachments;
    }

    public EmailBuilder email() {
        return new EmailBuilder();
    }

    public String sender() {
        return sender;
    }

    public String subject() {
        return subject;
    }

    public String content() {
        return content;
    }

    public List<Attachment> attachments() {
        return attachments;
    }

    public static class EmailBuilder {
        private String sender;
        private String subject;
        private String content;
        private List<Attachment> attachments = new ArrayList<>();

        public EmailBuilder withSender(String sender) {
            this.sender = sender;
            return this;
        }

        public EmailBuilder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailBuilder withContent(String content) {
            this.content = content;
            return this;
        }

        public EmailBuilder withAttachment(Attachment attachment) {
            this.attachments.add(attachment);
            return this;
        }

        public Email build() {
            return new Email(sender, subject, content, attachments);
        }
    }
}
