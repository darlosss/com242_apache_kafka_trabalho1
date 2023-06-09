package br.unifei.imc;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FahrenheitService {

    public static void main(String[] args) {
        var farenhService = new FahrenheitService();
        try(var service = new KafkaService<>(FahrenheitService.class.getSimpleName(),
                "TEMPERATURE_CURRENT",
                farenhService::parse,
                Temperature.class,
                Map.of())){
            service.run();
        }
    }
    private void parse(ConsumerRecord<String, Temperature> record) {
        outputRecord(record);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Temperaturas processadas!");
        dispatcherRecord(record);
    }

    private  void dispatcherRecord(ConsumerRecord<String, Temperature> record) {
        try(var dispatcher = new KafkaDispatcher<Temperature>()){
            var key = UUID.randomUUID().toString();
            var convertTmp = new Temperature(toFarenheits(record.value().getActualTemp()),"Farenheits", key, record.value().getCatchId());
            dispatcher.send("TEMPERATURE_CONVERT", key, convertTmp);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private  void outputRecord(ConsumerRecord<String, Temperature> record) {
        System.out.println("------------------------------------------");
        System.out.println("Mensagem em processamento pelo serviço de Fahrenheit:");
        System.out.println("PackageUUID - " + record.key());
        System.out.println("Temperature provided - " + record.value().getActualTemp());
        System.out.println("Consumed partition - " + record.partition());
        System.out.println("Message partition offeset - " + record.offset());
    }

    private static Double toFarenheits(Double tmp){
        return ((1.8 * tmp) + 32);
    }
}
