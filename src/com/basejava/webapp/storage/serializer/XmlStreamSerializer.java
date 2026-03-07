package com.basejava.webapp.storage.serializer;

import com.basejava.webapp.model.AbstractSection;
import com.basejava.webapp.model.Link;
import com.basejava.webapp.model.ListSection;
import com.basejava.webapp.model.Organization;
import com.basejava.webapp.model.OrganizationSection;
import com.basejava.webapp.model.Resume;
import com.basejava.webapp.model.TextSection;
import com.basejava.webapp.util.XmlParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class XmlStreamSerializer implements StreamSerializer {
    private XmlParser xmlParser;

    public XmlStreamSerializer() {
        // Классы из Resume
        xmlParser = new XmlParser(
                Resume.class,
                AbstractSection.class,
                Organization.class,
                Link.class,
                OrganizationSection.class,
                TextSection.class,
                ListSection.class,
                Organization.Position.class
        );
    }
    @Override
    public void doWrite(Resume r, OutputStream os) throws IOException {
        try (Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            xmlParser.marshall(r, w);
        }
    }

    @Override
    public Resume doRead(InputStream is) throws IOException {
        try (Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return xmlParser.unmarshall(r);
        }
    }
}
