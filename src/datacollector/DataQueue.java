package datacollector;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;

class DataQueue{
    private Node first;
    private Node last;

    DataQueue(){
        first = null;
        last = null;
    }
    void add(float temperature, String id, LocalTime time, LocalDate date){
        if(first == null){
            first = new Node(temperature, id, time, date);
            last = first;
        } else{
            last.next = new Node(temperature, id, time, date);
            last = last.next;
        }
    }

    float getTemperature(){
        return first.temperature;
    }
    String getID(){
        return first.id;
    }
    LocalTime getTime(){
        return first.time;
    }
    LocalDate getDate(){
        return first.date;
    }

    void remove(){
        first = first.next;
    }

    boolean ready(){
        if(first == null) return false;
        return first.next != null;
    }

    private class Node{
        private float temperature;
        private String id;
        private LocalTime time;
        private LocalDate date;

        private Node next;

        private Node(float temperature, String id, LocalTime time, LocalDate date){
            this.temperature = temperature;
            this.id = id;
            this.time = time;
            this.date = date;

            next = null;
        }
    }
}
