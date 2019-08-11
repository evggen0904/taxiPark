package xmlUtils;

import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;

public interface XmlCreator {
    Document createXmlDocument() throws ParserConfigurationException;
}
