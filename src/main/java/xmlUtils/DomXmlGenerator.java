package xmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

public class DomXmlGenerator {

    public static String xmlDocumentAsString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    public static Document getXmlFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new ByteArrayInputStream(xml.getBytes()));
    }

    public static String getAttributeValueForTagElement(Document doc, String elementName, String attributeName) {
        NodeList nodeList = doc.getElementsByTagName(elementName);
        if (nodeList == null) {
            return null;
        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                return element.getAttribute(attributeName);
            }
        }

        return null;
    }

    public static String getElementValue(Document doc, String elementName) {
        NodeList nodeList = doc.getElementsByTagName(elementName);
        if (nodeList == null) {
            return null;
        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i).getFirstChild();
            if (node != null) {
                return node.getNodeValue();
            }
        }

        return null;
    }

    public static void addNewTagWithAttribute(Document doc, Node parent, String newTagName, String attribute, String attributeValue) {
        Element newElement = doc.createElement(newTagName);
        newElement.setAttribute(attribute, attributeValue);
        parent.appendChild(newElement);
    }
}
