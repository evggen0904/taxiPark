import lombok.Data;
import org.w3c.dom.Document;
import xmlUtils.ClientXmlCreator;
import xmlUtils.XmlCreator;
import xmlUtils.DomXmlGenerator;

import javax.xml.parsers.ParserConfigurationException;

@Data
public class Client {
    private String message;
    private String driverId;

    public Client (String driverId, String message) {
        this.driverId = driverId;
        this.message = message;
    }

    public String generateXmlString() throws ParserConfigurationException {
        XmlCreator xmlCreator = new ClientXmlCreator(driverId, message);
        Document xmlDocument = xmlCreator.createXmlDocument();

        return DomXmlGenerator.xmlDocumentAsString(xmlDocument);
    }
}
