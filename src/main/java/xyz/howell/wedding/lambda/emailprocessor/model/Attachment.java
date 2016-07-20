package xyz.howell.wedding.lambda.emailprocessor.model;

import java.io.InputStream;

/**
 *
 */
public class Attachment {

    private String name;
    private InputStream data;

    private Attachment(String name, InputStream data) {
        this.name = name;
        this.data = data;
    }

    public String name() {
        return name;
    }

    public InputStream data() {
        return data;
    }

    public static Attachment of(String name, InputStream data) {
        return new Attachment(name, data);
    }
}
