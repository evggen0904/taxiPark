import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StartTaxiPark {
    private static final int CARS_COUNT = 10;
    private static final int DISPATCHERS_COUNT = 100;
    private static final int MESSAGE_COUNT = 1000;

    public static void main(String[] args) {

        Dispatcher dispatcher = new Dispatcher(CARS_COUNT, DISPATCHERS_COUNT);
        Random random = new Random();
        ExecutorService executorService = Executors.newFixedThreadPool(MESSAGE_COUNT);
        List<Future<Integer>> receivedIds = new ArrayList<>();

        @AllArgsConstructor
        class FutureResult implements Callable {
            private Client client;

            @Override
            public Object call() throws Exception {
                return dispatcher.receiveClientMessage(client.generateXmlString());
            }
        }

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            int driverId = random.nextInt(CARS_COUNT) + 1;
            String message = "client message #" + (i + 1) + " for driver " + driverId;
            Client client = new Client(String.valueOf(driverId), message);

            FutureResult futureResult = new FutureResult(client);
            Future<Integer> future = executorService.submit(futureResult);
            receivedIds.add(future);
        }

        //получение сформированного идентификатора сообщения в ответе
        receivedIds.forEach(item -> {
            try {
                item.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        executorService.shutdown();
        dispatcher.stopWorking();
    }
}
