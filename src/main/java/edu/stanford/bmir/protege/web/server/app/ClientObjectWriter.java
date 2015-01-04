package edu.stanford.bmir.protege.web.server.app;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 23/12/14
 */
public class ClientObjectWriter<T>  {

    private final String variableName;

    private final ClientObjectEncoder<T> encoder;

    public ClientObjectWriter(String variableName, ClientObjectEncoder<T> encoder) {
        this.variableName = checkNotNull(variableName);
        this.encoder = checkNotNull(encoder);
    }

    public static <T> ClientObjectWriter<T> get(String variableName, ClientObjectEncoder<T> encoder) {
        return new ClientObjectWriter<T>(variableName, encoder);
    }

    public void writeVariableDeclaration(T object, Writer ps) {
        PrintWriter pw = new PrintWriter(ps);
        pw.print("var ");
        pw.print(variableName);
        pw.print(" = ");
        Map<String, Object> properties = new HashMap<String, Object>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        Json.createWriterFactory(properties).createWriter(pw).writeObject(encoder.encode(object));
        pw.println(";");
        pw.flush();
    }
}
