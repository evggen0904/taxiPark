import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

@Data
public class TaxiDriver implements Callable {
    private final String END_OF_WORK = "finish";

    private int id;
    private BlockingQueue<String> driverOrders;
    private String fileDir;

    public TaxiDriver(int id, BlockingQueue<String> driverOrders) {
        this.id = id;
        this.driverOrders = driverOrders;
        this.fileDir = "src/driversDB/" + id + "/";
    }

    private void doOrder(String message, String fileName) {
        try {
            FileUtils.writeStringToFile(new File(fileName), message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopWorking() {
        driverOrders.add(END_OF_WORK);
    }

    @Override
    public String call() throws Exception {
        try {
            System.out.println(String.format("Driver id = %d begin working", id));
            String message;
            int count = 1;
            while (!(message = driverOrders.take()).equals(END_OF_WORK)) {
                System.out.println(String.format("Driver id = %d gets message : %s", id, message));
                doOrder(message, fileDir + count + ".xml");
                count++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "finished working";
    }
}
