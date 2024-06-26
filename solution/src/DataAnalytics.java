import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DataAnalytics {
    // Функция, которая будет вычислять месяца когда покупателя тратили больше всего
    public String getAnalytics() throws IOException {
        // Указываю путь к файлу, на основе которого буду проводить анализ
        var fileName = "solution/data/format.json";
        // Создаю экземпляр класса ObjectMapper чтобы, прочитать json
        var objectMapper = new ObjectMapper();
        // Предварительно создав класс Client со всеми необходимыми полями, я считываю json файл с данными о клиентах,
        // создавая объекты этого класса Client и добавляя их в список
        List<Client> clients = objectMapper.readValue(new File(fileName), new TypeReference<>() {
        });
        // Далее создаю мапу для хранения месяца и суммы, которую за этот месяц потратили клиенты
        Map<String, Double> monthlySales = new HashMap<>();
        // Прохожусь по списку клиентов и вытаскиваю оттуда месяц, сразу приводя его к строчному типу и нижнему регистру
        // потому что в Java объекты типа Month записываются в верхнем регистре
        clients.forEach(client -> {
            var month = LocalDateTime.parse(client.getOrderedAt(), DateTimeFormatter.ISO_DATE_TIME)
                    .getMonth()
                    .toString()
                    .toLowerCase();
            // Проверяю что статус заказа завершённый и только после этого суммирую траты клиентов, которые были
            // произведены в один и тот же месяц
            if (client.getStatus().equals("COMPLETED")){
                double total = Double.parseDouble(client.getTotal());
                // Добавляю в мапу получившиеся месяца с общей суммой трат
                monthlySales.merge(month, total, Double::sum);
            }
        });
        // Среди всех месяцев нахожу один или несколько, в которые клиенты потратили больше всего
        var maxSales = Double.MIN_VALUE;
        for (double sales : monthlySales.values()) {
            maxSales = Math.max(maxSales, sales);
        }
        // Теперь создаю список в который буду добавлять название месяца(-ев) когда было потрачено больше всего
        List<String> maxMonths = new ArrayList<>();
        // Прохожусь по мапе с месяцами и тратами, сравниваю максимальное количество трат с тем, в какой месяц они были
        // произведены и в конце добавляю в список
        for (Map.Entry<String, Double> entry : monthlySales.entrySet()) {
            if (entry.getValue() == maxSales) {
                maxMonths.add(entry.getKey());
            }
        }
        // Создаю компоратор, чтобы сравнивать месяца по их значению в течение года и тем самым отсортировать
        Comparator<Month> monthComparator = Comparator.comparingInt(Month::getValue);

        maxMonths = monthlySales.entrySet().stream()
                .filter(entry -> entry.getValue().equals(Collections.max(monthlySales.values())))
                // Преобразуем название месяца обратно в объект Month
                .map(entry -> Month.valueOf(entry.getKey().toUpperCase()))
                // Сортируем месяцы по порядку в течение года
                .sorted(monthComparator)
                // Преобразуем объекты Month обратно в названия месяце и приводим к нижнему регистру
                .map(month -> month.toString().toLowerCase())
                .collect(Collectors.toList());

        var result = "{\"months\": " + maxMonths.stream().map(month -> "\"" + month + "\"")
                .collect(Collectors.joining(",")) + "}";

        return result;
    }
}
