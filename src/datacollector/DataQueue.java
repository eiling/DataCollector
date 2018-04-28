package datacollector;

import java.time.LocalDateTime;

class DataQueue{
    private final Object lock;
    private volatile Node first;
    private volatile Node last;

    DataQueue(){
        lock = new Object();
        first = null;
        last = null;
    }

    void add(String temperature, String id, LocalDateTime datetime){
        synchronized(lock){
            if (first == null){
                first = new Node(temperature, id, datetime);
                last = first;
            } else {
                last.next = new Node(temperature, id, datetime);
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
        final String temperature;
        final String id;
        final LocalDateTime datetime;

        private Node next;

        private Node(String temperature, String id, LocalDateTime datetime){
            this.temperature = temperature;
            this.id = id;
            this.datetime = datetime;

            next = null;
        }
    }
}
