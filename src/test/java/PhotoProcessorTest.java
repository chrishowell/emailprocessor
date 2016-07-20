import org.junit.Test;
import xyz.howell.wedding.lambda.emailprocessor.PhotoProcessor;
import xyz.howell.wedding.lambda.emailprocessor.model.Email;

import java.io.InputStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PhotoProcessorTest {

    private final PhotoProcessor photoProcessor = new PhotoProcessor();

    @Test
    public void processS3EmailContent() throws Exception {
        InputStream emailContent = photoProcessor.s3Photo("bdiglnb4r8k9kkeg4u8noa8jpgmkfgj0kloe1ng1");
        Email email = photoProcessor.extractEmail(emailContent, System.out::println);
        assertThat(email.sender(), is("chris@howell.xyz"));
        assertThat(email.attachments().size(), is(1));
        assertThat(email.attachments().get(0).name(), is("IMG_7601.JPG"));
        assertThat(email.subject(), is("test subject"));
        assertThat(email.content(), is("test body"));
    }
}