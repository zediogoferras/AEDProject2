package aed.collections;

import aed.utils.TimeAnalysisUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;

public class QueueArray<Item> implements Iterable<Item>{

    private Item[] items;   //array of items
    private int firstI;   //index of the first position of the queue inside the array
    private int available;  //index of where the next element will be added to

    @SuppressWarnings("unchecked")
    public QueueArray(int max){
        this.items = (Item[]) new Object[max];
        this.firstI = 0;
        this.available = 0; //both firstI and available start at 0
    }

    public boolean isFull(){
        //if first and  available are the same and first is not empty
        if(this.items[this.firstI] != null && this.available == this.firstI){
            return true;
        }
        return false;
    }

    public boolean isEmpty(){
        //if the first position of the queue is null then the queue is empty
        if(this.items[this.firstI] == null){
            return true;
        }
        return false;
    }

    public void enqueue(Item item){

        if(this.isFull()){
            throw new OutOfMemoryError("OutOfMemoryError");
        }
        else if(item == null){
            throw new IllegalArgumentException("IllegalArgumentException");
        }
        else{
            this.items[this.available] = item;
            this.available++;
            if(this.available == this.items.length){
                this.available = 0;
            }
        }
    }

    public Item dequeue(){
        if(this.isEmpty()){
            return null;
        }
        Item result = this.items[this.firstI];
        this.items[this.firstI] = null;
        this.firstI++;
        if(this.firstI == this.items.length){
            this.firstI = 0;
        }
        return result;
    }

    public Item peek(){
        if(this.isEmpty()){
            return null;
        }
        Item result = this.items[this.firstI];
        return result;
    }

    public int size(){
        int result;
        if(this.isEmpty()){
            return 0;
        }
        if(this.firstI < this.available){
            result = this.available - this.firstI;
        }
        else{
            result = (this.items.length - this.firstI) + this.available;
        }
        return result;
    }

    public QueueArray<Item> shallowCopy(){
        Item[] copiedItems = this.items.clone();
        QueueArray<Item> result = new QueueArray<>(this.items.length);
        result.items = copiedItems;
        result.available = this.available;
        result.firstI = this.firstI;
        return result;
    }

    public Iterator<Item> iterator() {
        return new QueueArrayIterator();
    }

    private class QueueArrayIterator implements Iterator<Item>{

        int iterator;

        QueueArrayIterator(){
            this.iterator = firstI;
        }

        public boolean hasNext() {
            return items[this.iterator] != null;
        }

        public Item next() {
            Item result = items[this.iterator];
            this.iterator++;
            if(this.iterator == items.length){
                this.iterator = 0;
            }
            return result;
        }
    }

    /*
    public static QueueArray<Integer> generateQueueArrayExample(int n) {
        Random r = new Random();
        int nElements = r.nextInt(1, n-1);   //creates a random number of elements to add into te queueArray so that you can always dequeue or enqueue
        QueueArray<Integer> example = new QueueArray<>(n);
        for (int i = 0; i < nElements; i++) {
            example.enqueue(r.nextInt());
        }
        return example;
    }

    public static double calculateAverageExecutionTimeEnqueue(int n)
    {
        int trials = 30;
        double totalTime = 0;
        for(int i = 0; i < trials; i++)
        {
            QueueArray<Integer> queueArrayExample = generateQueueArrayExample(n);
            Random r = new Random();
            int x = r.nextInt();
            long time = System.currentTimeMillis();
            queueArrayExample.enqueue(x);
            totalTime += System.currentTimeMillis() - time;
        }
        return totalTime/trials;
    }

    public static double calculateAverageExecutionTimeDequeue(int n)
    {
        int trials = 30;
        double totalTime = 0;
        for(int i = 0; i < trials; i++)
        {
            QueueArray<Integer> queueArrayExample = generateQueueArrayExample(n);
            long time = System.currentTimeMillis();
            queueArrayExample.dequeue();
            totalTime += System.currentTimeMillis() - time;
        }
        return totalTime/trials;
    }
     */

    public static void main(String args[]){
        /*
        int n = 125;
        double previousTime = calculateAverageExecutionTimeEnqueue(n);  //test enqueue
        //double previousTime = calculateAverageExecutionTimeDequeue(n);    //test dequeue
        double newTime;
        double doublingRatio;
        for(int i = 250; true; i*=2)
        {
            newTime = calculateAverageExecutionTimeEnqueue(i);  //test enqueue
            //newTime = calculateAverageExecutionTimeDequeue(i);  //test dequeue
            if(previousTime > 0)
            {
                doublingRatio = newTime/previousTime;
            }
            else doublingRatio = 0;
            previousTime = newTime;
            System.out.println(i + "\t" + newTime + "\t" + doublingRatio);
        }
         */
    }
}
