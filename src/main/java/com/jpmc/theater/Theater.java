package com.jpmc.theater;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Theater {

  LocalDateProvider provider;
  private List<Showing> schedule;

  public Theater(LocalDateProvider provider) {
    this.provider = provider;

    Movie spiderMan = new Movie("Spider-Man: No Way Home", Duration.ofMinutes(90), 12.5, 1);
    Movie turningRed = new Movie("Turning Red", Duration.ofMinutes(85), 11, 0);
    Movie theBatMan = new Movie("The Batman", Duration.ofMinutes(95), 9, 0);
    schedule = List.of(
        new Showing(turningRed, 1, LocalDateTime.of(provider.currentDate(), LocalTime.of(9, 0))),
        new Showing(spiderMan, 2, LocalDateTime.of(provider.currentDate(), LocalTime.of(11, 0))),
        new Showing(theBatMan, 3, LocalDateTime.of(provider.currentDate(), LocalTime.of(12, 50))),
        new Showing(turningRed, 4, LocalDateTime.of(provider.currentDate(), LocalTime.of(14, 30))),
        new Showing(spiderMan, 5, LocalDateTime.of(provider.currentDate(), LocalTime.of(16, 10))),
        new Showing(theBatMan, 6, LocalDateTime.of(provider.currentDate(), LocalTime.of(17, 50))),
        new Showing(turningRed, 7, LocalDateTime.of(provider.currentDate(), LocalTime.of(19, 30))),
        new Showing(spiderMan, 8, LocalDateTime.of(provider.currentDate(), LocalTime.of(21, 10))),
        new Showing(theBatMan, 9, LocalDateTime.of(provider.currentDate(), LocalTime.of(23, 0)))
    );
  }

  public static void main(String[] args) throws JsonProcessingException {
    Theater theater = new Theater(LocalDateProvider.singleton());
    theater.printSchedule();
    theater.printScheduleInJson();
  }

  public Reservation reserve(Customer customer, int sequence, int howManyTickets) {
    Showing showing;
    try {
      showing = schedule.get(sequence - 1);
    } catch (RuntimeException ex) {
      ex.printStackTrace();
      throw new IllegalStateException(
          "not able to find any showing for given sequence " + sequence);
    }
    return new Reservation(customer, showing, howManyTickets);
  }

  public void printSchedule() throws JsonProcessingException {
    System.out.println(provider.currentDate());
    System.out.println("===================================================");
    schedule.forEach(s ->
        System.out.println(
            s.getSequenceOfTheDay() + ": " + s.getStartTime() + " " + s.getMovie().getTitle() + " "
                + humanReadableFormat(s.getMovie().getRunningTime()) + " $" + s.getMovieFee())
    );
    System.out.println("===================================================");
  }

  public void printScheduleInJson() throws JsonProcessingException {
    List<Map<String, String>> display = new ArrayList<>();
    System.out.println(provider.currentDate());
    System.out.println("===================================================");
    schedule.forEach(s -> {
          Map<String, String> showing = new LinkedHashMap<>();
          showing.put("Sequence", Integer.toString(s.getSequenceOfTheDay()));
          showing.put("Start Time", String.valueOf(s.getStartTime()));
          showing.put("Title", s.getMovie().getTitle());
          showing.put("Duration",
              humanReadableFormat(s.getMovie().getRunningTime()).replace("(", "").replace(")", ""));
          showing.put("Price", "$" + s.getMovieFee());
          display.add(showing);
        }
    );
    ObjectMapper objectMapper = new ObjectMapper();
    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(display));
    System.out.println("===================================================");
  }

  public String humanReadableFormat(Duration duration) {
    long hour = duration.toHours();
    long remainingMin = duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours());

    return String.format("(%s hour%s %s minute%s)", hour, handlePlural(hour), remainingMin,
        handlePlural(remainingMin));
  }

  // (s) postfix should be added to handle plural correctly
  private String handlePlural(long value) {
    return value == 1 ? "" : "s";
  }
}
