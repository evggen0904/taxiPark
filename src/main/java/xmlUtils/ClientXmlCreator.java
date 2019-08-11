package xmlUtils;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ClientXmlCreator implements XmlCreator {
    private DocumentBuilderFactory dbf;
    private DocumentBuilder db ;
    private Document doc;
    @Getter
    @Setter
    private String driverId;
    @Getter
    @Setter
    private String message;

    public ClientXmlCreator (String driverId, String message) {
        dbf = DocumentBuilderFactory.newInstance();
        this.driverId = driverId;
        this.message = message;
    }

    @Override
    public Document createXmlDocument() throws ParserConfigurationException {
        db = dbf.newDocumentBuilder();
        doc = db.newDocument();

        Element root = doc.createElement("message");
        doc.appendChild(root);
        Element target = doc.createElement("target");
        target.setAttribute("id", driverId);
        Element content = doc.createElement("content");
        Element data = doc.createElement("data");
        data.setTextContent(message);
        content.appendChild(data);
        root.appendChild(target);
        root.appendChild(content);

        return doc;
    }
}
