import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static String dataFile = "src/main/resources/tickets.json";
    private static List<Ticket> tickets = new ArrayList<>();
    private static double procentile = 90.0;
    public static void main(String[] args) {
        parseFile();
        double time = 0;
        List<Long> flights = new ArrayList<>();
        for(Ticket ticket : tickets){
            Duration duration = Duration.between(ticket.getDepartureTime(), ticket.getArrivalTime());
            flights.add(duration.toMinutes());
            time = time + duration.toMinutes();
        }
        List<Long> sortedDurationFlight = flights.stream().sorted().collect(Collectors.toList());
        System.out.println();

        double middleMinutes = time / tickets.size();
        int hours1 = (int) (middleMinutes / 60);
        int minutes1 =(int) middleMinutes - (hours1 * 60);
        LocalTime middleTime = LocalTime.of(hours1, minutes1);
        System.out.println("Среднее время полета между городами Владивосток и Тель-Авив: " + middleTime + "\n");

        double index = procentile / 100 * tickets.size();

        double ninetyProcentile = (sortedDurationFlight.get(tickets.size()-1) + sortedDurationFlight.get((int)(index - 1))) / 2;
        int hours2 = (int) (ninetyProcentile/60);
        int minutes2 = (int) ninetyProcentile - (hours2 * 60);
        LocalTime ninetyProcentileTime = LocalTime.of(hours2, minutes2);

        System.out.println("90-й процентиль времени полета между городами  Владивосток и Тель-Авив: " + ninetyProcentileTime);
    }

    public static void parseFile() {
        JSONParser parser = new JSONParser();
        try {
            JSONObject JSONObject = (JSONObject) parser.parse(getJsonFile());
            JSONArray ticketsArray = (JSONArray) JSONObject.get("tickets");
            parseTickets(ticketsArray);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static void parseTickets(JSONArray ticketsArray) {
        ticketsArray.forEach(ticketObject -> {
            JSONObject ticketJsonObject = (JSONObject) ticketObject;
            Ticket ticket = new Ticket();

            ticket.setOrigin((String) ticketJsonObject.get("origin"));
            ticket.setOriginName((String) ticketJsonObject.get("origin_name"));
            ticket.setDestination((String) ticketJsonObject.get("destination"));
            ticket.setDestinationName((String) ticketJsonObject.get("destination_name"));
            ticket.setDepartureDate(convertToLocalDate((String) ticketJsonObject.get("departure_date")));
            ticket.setDepartureTime(convertToLocalTime((String) ticketJsonObject.get("departure_time")));
            ticket.setArrivalDate(convertToLocalDate((String) ticketJsonObject.get("arrival_date")));
            ticket.setArrivalTime(convertToLocalTime((String) ticketJsonObject.get("arrival_time")));
            ticket.setCarrier((String) ticketJsonObject.get("carrier"));
            long stops = (long) ticketJsonObject.get("stops");
            ticket.setStops((int) stops);
            long price = (long) ticketJsonObject.get("price");
            ticket.setPrice((int) price);
            tickets.add(ticket);
        });
    }

    private static LocalDate convertToLocalDate(String date) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d.MM.yy");
        LocalDate localDate = LocalDate.parse(date, dateFormatter);
        return localDate;
    }

    private static LocalTime convertToLocalTime(String time) {
        if(time.length() == 5) {
            return LocalTime.parse(time);
        }else if(time.length() == 4) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
            return LocalTime.parse(time,timeFormatter);
        }
        return null;
    }


    private static String getJsonFile() {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(dataFile));
            lines.forEach(line -> builder.append(line));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        String content = builder.toString();
        content = content.replaceAll("\\uFEFF", "");
        return content;
    }
}
