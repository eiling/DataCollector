package datacollector;

import java.time.LocalDate;
import java.time.LocalTime;

class DataQueue{
    private final Object lock;
    private volatile Node first;
    private volatile Node last;

    DataQueue(){
        lock = new Object();
        first = null;
        last = null;
    }

    void add(float temperature, String id, LocalTime time, LocalDate date){
        synchronized(lock){
            if (first == null){
                first = new Node(temperature, id, time, date);
                last = first;
            } else {
                last.next = new Node(temperature, id, time, date);
                last = last.next;
            }
        }
    }

    Node getFirst() {
        return first;
    }

    void remove() {
        synchronized(lock){
            if (first == null)
                return;
            if (first == last) {
                first = null;
                last = null;
            } else{
                Node temp = first.next;
                first.next = null;
                first = temp;
            }
        }
    }

    boolean hasElements(){
        return first != null;
    }

    static final class Node{
        final float temperature;
        final String id;
        final LocalTime time;
        final LocalDate date;

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
