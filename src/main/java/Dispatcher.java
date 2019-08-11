import org.w3c.dom.Document;
import xmlUtils.DomXmlGenerator;
import xmlUtils.XmlValidator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Dispatcher {
    private final String VALIDATION_XSD_PATH = "src/main/resources/taxiSchema.xsd";

    private Map<Integer, BlockingQueue<String>> taxiDriverQueues;
    private ExecutorService dispatchers;
    private ExecutorService driversExecutor;
    private List<TaxiDriver> drivers = new ArrayList<>();
    private List<Future<String>> driversWorkingResultList = new ArrayList<>();
    private Set<Future<Integer>> dispatchersWorkingResultSet = new HashSet<>();
    private volatile AtomicInteger generatedId = new AtomicInteger(0);

    public Dispatcher(int carsCount, int dispatchersCount) {
        if (carsCount <= 0) {
            throw new IllegalArgumentException("Количество машин в таксопарке должно быть положительным числом");
        }
        if (dispatchersCount <= 0) {
            throw new IllegalArgumentException("Количество диспетчеров в таксопарке должно быть положительным числом");
        }

        taxiDriverQueues = new HashMap<>(carsCount);
        dispatchers = Executors.newFixedThreadPool(dispatchersCount);
        driversExecutor = Executors.newFixedThreadPool(carsCount);
        for (int i = 0; i < carsCount; i++) {
            int id = i + 1;
            BlockingQueue<String> driversQueue = new LinkedBlockingDeque<>();
            TaxiDriver taxiDriver = new TaxiDriver(id, driversQueue);
            drivers.add(taxiDriver);
            taxiDriverQueues.put(id, driversQueue);
            driversWorkingResultList.add(driversExecutor.submit(taxiDriver));
        }
    }

    public Future<Integer> receiveClientMessage(String xml) {

        Future<Integer> result = dispatchers.submit(() -> {
            InputStream xmlStream = new ByteArrayInputStream(xml.getBytes());
            InputStream validationXsd = new FileInputStream(new File(VALIDATION_XSD_PATH));
            if (!XmlValidator.validateAgainstXSD(xmlStream, validationXsd)) {
                return -1;
            }

            Document doc = DomXmlGenerator.getXmlFromString(xml);
            String driverId = DomXmlGenerator.getAttributeValueForTagElement(doc, "target", "id");
            String message = DomXmlGenerator.getElementValue(doc, "data");
            if (driverId != null) {
                BlockingQueue<String> driversQueue = taxiDriverQueues.get(Integer.valueOf(driverId));
                if (driversQueue == null) {
                    return -1;
                }

                int messageId = generatedId.getAndIncrement();
                DomXmlGenerator.addNewTagWithAttribute(doc,
                        doc.getElementsByTagName("message").item(0),
                        "dispatched",
                        "id",
                        String.valueOf(messageId));
                String newXml = DomXmlGenerator.xmlDocumentAsString(doc);
                driversQueue.add(newXml);
                System.out.println(String.format("message '%s' added to queue", message));

                return messageId;
            }

            return -1;
        });
        dispatchersWorkingResultSet.add(result);

        return result;
    }

    public void stopWorking() {
        Consumer<Future> finishWork = item -> {
            try {
                item.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        };
        dispatchersWorkingResultSet.forEach(finishWork);
        dispatchers.shutdown();
        drivers.forEach(taxiDriver -> taxiDriver.stopWorking());
        driversExecutor.shutdown();
    }
}
