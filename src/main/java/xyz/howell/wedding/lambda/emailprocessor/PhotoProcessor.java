package xyz.howell.wedding.lambda.emailprocessor;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import xyz.howell.wedding.lambda.emailprocessor.model.Email;

import javax.activation.DataSource;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;
import static xyz.howell.wedding.lambda.emailprocessor.model.Attachment.of;

public class PhotoProcessor {


    private final AmazonS3 s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());

    public void handleEmail(S3EventNotification s3Event, Context context) {
        LambdaLogger logger = context.getLogger();

        List<S3EventNotificationRecord> records = s3Event.getRecords();
        logger.log(format("%d records in event", records.size()));
        records.forEach(record -> processRecord(record, logger));
    }

    private void processRecord(S3EventNotificationRecord record, LambdaLogger logger) {
        String emailId = record.getS3().getObject().getKey();
        InputStream objectData = s3Photo(emailId);
        logger.log(format("Email ID: %s", emailId));
        Email email = extractEmail(objectData, logger);
        writeAttachmentsFrom(emailId, email, logger);
    }

    public void writeAttachmentsFrom(String emailId, Email email, LambdaLogger logger) {
        email.attachments().forEach(attachment -> {
            String photoId = emailId + "-" + attachment.name();
            try {
                s3Client.putObject("photo-email-photos", photoId, attachment.data(), new ObjectMetadata());
            } catch (Exception e) {
                logger.log(format("An exception was thrown trying to save %s to S3: %s", photoId, e.getMessage()));
            }
            logger.log(format("Photo ID: %s", photoId));
        });
    }

    public InputStream s3Photo(String key) {
        S3Object object = s3Client.getObject("photo-emails", key);
        return object.getObjectContent();
    }

    public Email extractEmail(InputStream objectData, LambdaLogger logger) {
        Email.EmailBuilder email = new Email.EmailBuilder();
        try {
            MimeMessageParser messageParser = messageParserFrom(IOUtils.toString(objectData));
            email.withSender(messageParser.getFrom());
            email.withContent(messageParser.getPlainContent().trim());
            email.withSubject(messageParser.getSubject().trim());
            List<DataSource> attachments = messageParser.getAttachmentList();
            logger.log(format("%s email attachments", attachments.size()));
            attachments.forEach(attachment -> {
                addAttachment(email, attachment, logger);
            });
        } catch (Exception e) {
            logger.log("ERROR:" + e.getMessage());
        } finally {
            IOUtils.closeQuietly(objectData, null);
        }
        return email.build();
    }

    private void addAttachment(Email.EmailBuilder email, DataSource attachment, LambdaLogger logger) {
        logger.log(format("email attachment: %s", attachment.getName()));
        try {
            email.withAttachment(of(attachment.getName(), attachment.getInputStream()));
        } catch (IOException e) {
            logger.log(format("ERROR: Error processing %s - %s", attachment.getName(), e.getMessage()));
        }
    }

    private MimeMessageParser messageParserFrom(String messageContent) throws Exception {
        MimeMessageParser parser = new MimeMessageParser(new MimeMessage(Session.getDefaultInstance(new Properties()), new ByteArrayInputStream(messageContent.getBytes())));
        return parser.parse();
    }


}
