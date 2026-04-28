package moonulio.skinpresets.client.mojang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class MultipartBodyBuilder {
    private final String boundary;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private MultipartBodyBuilder(String boundary) {
        this.boundary = boundary;
    }

    static MultipartBodyBuilder create(String boundary) {
        return new MultipartBodyBuilder(boundary);
    }

    MultipartBodyBuilder addTextPart(String name, String value) throws IOException {
        write("--" + boundary + "\r\n");
        write("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        write(value + "\r\n");
        return this;
    }

    MultipartBodyBuilder addFilePart(String name, String filename, String contentType, byte[] bytes) throws IOException {
        write("--" + boundary + "\r\n");
        write("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n");
        write("Content-Type: " + contentType + "\r\n\r\n");
        out.write(bytes);
        write("\r\n");
        return this;
    }

    byte[] build() throws IOException {
        write("--" + boundary + "--\r\n");
        return out.toByteArray();
    }

    private void write(String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
    }
}
