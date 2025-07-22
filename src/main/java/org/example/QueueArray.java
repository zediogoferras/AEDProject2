package org.example;

import java.lang.reflect.Array;
import java.util.Iterator;

public class QueueArray<Item> implements Iterable<Item>{

    private int available;  //first position after last

    private Item[] items;

    public QueueArray(int max){
        this.available = 0;
        this.items = (Item[]) new Object[max];
    }

    public void enqueue(Item item){
        if(this.available == this.items.length){
            throw new OutOfMemoryError("OutOfMemoryError");
        }
        else if(item == null){
            throw new IllegalArgumentException("IllegalArgumentException");
        }
        else{
            this.items[available] = item;
            available++;
        }
    }

    public Item dequeue(){
        if(this.available == 0){
            return null;
        }
        Item result = this.items[0];
        this.items[0] = null;
        available--;
        for(int i = 0; i < available; i++){
            items[i] = items [i+1];
        }
        return result;
    }

    public Item peek(){
        if(this.isEmpty()){
            return null;
        }
        Item result = this.items[0];
        return result;
    }

    public boolean isEmpty(){
        return this.available == 0;
    }

    public int size(){
        return this.available;
    }

    public QueueArray<Item> shallowCopy(){
        QueueArray result = new QueueArray<Item>(this.items.length);
        result.available = this.available;
        result.items = this.items;
        return result;
    }

    public Iterator<Item> iterator() {
        return null;
    }

    private class QueueArrayIterator implements Iterator<Item>{

        int iterator;

        QueueArrayIterator(){
            this.iterator = -1;
        }

        public boolean hasNext() {
            return this.iterator < available;
        }

        public Item next() {
            this.iterator++;
            return items[this.iterator];
        }
    }

    static void main(String args[]){

    }
}
