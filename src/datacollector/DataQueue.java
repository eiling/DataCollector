package datacollector;

import java.sql.Date;
import java.time.LocalTime;

public class DataQueue{
    private Node first;
    private Node last;

    public DataQueue(){
        first = null;
        last = null;
    }
    public void add(){
        if(first == null){
            first = new Node();
            last = first;
        } else{
            last.next = new Node();
            last = last.next;
        }
    }
    public void addToSum(float temp){
        last.sum += temp;
        last.n++;
    }
    public void setID(String id){
        last.id = id;
    }
    public void setMax(float max){
        last.max = max;
    }
    public void setMin(float min){
        last.min = min;
    }
    public void setTime(LocalTime time){
        last.time = time;
    }
    public void setDate(long date){
        last.date = date;
    }
    public String getID(){
        return first.id;
    }
    public float getAverage(){
        return first.sum / first.n;
    }
    public float getMax(){
        return first.max;
    }
    public float getMin(){
        return first.min;
    }
    public LocalTime getTime(){
        return first.time;
    }
    public Date getDate(){
        return new Date(first.date);
    }
    public void remove(){
        first = first.next;
    }
    public boolean ready(){
        return first.next != null;
    }

    private class Node{
        private String id;
        private float sum;
        private int n;
        private float max;
        private float min;
        private LocalTime time;
        private long date;

        private Node next;

        private Node(){
            this.sum = 0;
            this.n = 0;
            next = null;
        }
    }
}
