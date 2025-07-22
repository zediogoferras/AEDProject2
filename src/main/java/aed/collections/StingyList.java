package aed.collections;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class StingyList<T> implements Iterable<T> {

    //representamos null como o long 0L.
    private static final long NULL = 0L;

    private int size;
    private long first;
    private long last;

    public StingyList()
    {
        this.size = 0;
        this.first = NULL;  //Cria first node NULL
        this.last = NULL;  //Cria last node NULL
    }

    // Embora não seja obrigatório, aconselho-vos a implementar estes 3 métodos seguintes, pois o código da StingyList pode
    // ser implementado de forma simples com base nestes métodos.

    // Dado um long que representa o endereço do nó atual (node), e um segundo long que representa o endereço do nó
    // de onde viemos numa sequência antes de chegar ao nó atual, devolve uma referência para o nó onde queremos ir a seguir.
    // Este método funciona quer estejamos a "viajar" da esquerda para a direita, ou da direita para a esquerda,
    // como podemos ver no seguinte diagrama:
    //       from -- to --> Node -- to --> beyond
    //       beyond <-- to -- Node <-- to -- from
    //
    long getBeyond(long node, long fromAddr)
    {
        return UNode.get_prev_next_addr(node) ^ fromAddr;   //pXORn XOR p = n ou pXORn XOR n = p
    }

    // Atualiza uma das referências do nó (pode ser usado para atualizar o previous ou o next).
    // Recebe como argumento um endereço para o nó, um endereço para a ligação que queremos atualizar (previous ou next),
    // e o novo endereço a usar. Se passármos o previous, este método atualiza apenas o ponteiro para o previous
    // mantendo o ponteiro para o next, e vice-versa.
    static void updateNodeReference(long node, long oldAddr, long newAddr)
    {
        long newXor = (UNode.get_prev_next_addr(node) ^ oldAddr) ^ newAddr; // (pXORn XOR p) XOR newP = n XOR newP ou (pXORn XOR n) XOR newN = p XOR newN
        UNode.set_prev_next_addr(node, newXor);
    }

    //Atualiza ambas as referências do nó em simultâneo (previous e next). Útil quando queremos atualizar ambas,
    //e já temos as referências para o novo previous e o novo next. Recebe como argumentos o novo nó previous para o
    // qual queremos apontar, e o novo nó next para o qual queremos apontar.
    void updateBothNodeReferences(long node, long prevAddr, long nextAddr)
    {
        long newXor = prevAddr ^ nextAddr;  //pXORn
        UNode.set_prev_next_addr(node, newXor);
    }


    //Stingy List Methods

    public void add(T item)
    {
        if(item == null){
            throw new IllegalArgumentException();
        }
        //Caso em que a lista está vazia
        else if(this.isEmpty()){
            this.first = UNode.create_node(item, NULL, NULL);   //cria o node first sem next nem prev
            this.last = this.first; //aponta o last para o first, quando o tamanho do array é 1, amos os ponteiros apontarão para o mesmo elemento
        }
        //Caso em que a lista tem apenas 1 elemento
        else if(size == 1){
            this.last = UNode.create_node(item, this.first, NULL);  //cria o node last da lista, com prev em first e next em NULL
            updateBothNodeReferences(this.first, NULL, this.last);  //atribui ao first o next em last e prev em NULL
        }
        //Caso em que a lista tem mais que um elemento
        else{
            long tempNodeAddr = this.last;  //guarda o endereço do antigo last
            this.last = UNode.create_node(item, tempNodeAddr, NULL);    //cria novo last com prev em last-1 e next em NULL
            updateNodeReference(tempNodeAddr, NULL, this.last); //aponta next de last antigo para last
        }
        this.size++;
    }

    public T remove()
    {
        T result;
        //Caso em que a lista está vazia
        if(this.isEmpty()){
            throw new IndexOutOfBoundsException();
        }
        //Caso em que a lista tem apenas 1 elemento
        else if(this.size == 1){
            result = UNode.get_item(this.first);
            UNode.free_node(this.first); //limpa a parte da memória onde estava endereçado o first
            size--;
        }
        //Caso em que a lista tem mais que 1 elemento
        else{
            long tempNodeAddr = getBeyond(this.last, NULL); //guarda o endereço de last-1 que passará a ser last
            result = UNode.get_item(this.last);
            updateNodeReference(tempNodeAddr, this.last, NULL);  //aponta o next de last-1 para NULL em vez de last
            UNode.free_node(this.last);  //liberta a parte da memória onde estava endereçado o antigo last
            this.last = tempNodeAddr;   //last-1 passa a ser o novo last
            size--;
        }
        return result;
    }

    public T get()
    {
        T result;
        //Caso em que a lista está vazia
        if(this.size == 0){
            throw new IndexOutOfBoundsException();
        }
        //Caso em que a lista tem apenas 1 elemento
        else if(this.size == 1){
            result = UNode.get_item(this.first);
        }
        //Caso em que a lista tem mais que 1 elemento
        else{
            result = UNode.get_item(this.last);
        }
        return result;
    }

    //Encontra o endereço do node que tem posição i na lista
    //Trabalha para a primeira metade da lista
    public long getAddrFirstHalf(int i){
        int count = 0;  //iterador da lista
        long countAddr = this.first;    //variavel long que tera sempre o endereço da posicao "count" da lista
        long countPrevAddr = NULL;  //variavel long que tera sempre o endereço prev da posicao "count" da lista
        while(count < i){
            long tempAddr = countAddr;  //guarda endereço do count atual que sera o prev da proxima iteracao
            countAddr = getBeyond(tempAddr, countPrevAddr);    //avança o endereço do count 1 posiçao a frente na lista: next = prevXORnext XOR prev
            countPrevAddr = tempAddr;   //atualiza o endereço prev da posicao count atual
            count++;
        }
        return countAddr;
    }

    //Encontra o endereço do node que tem posição i na lista
    //Trabalha para a segunda metade da lista
    public long getAddrSecondHalf(int i){
        int count = this.size-1;  //iterador da lista
        long countAddr = this.last;    //variavel long que tera sempre o endereço da posicao "count" da lista
        long countNextAddr = NULL;  //variavel long que tera sempre o endereço next da posicao "count" da lista
        while(count > i){
            long tempAddr = countAddr;  //guarda endereço do count atual que sera o next da proxima iteracao
            countAddr = getBeyond(tempAddr, countNextAddr);    //avança o endereço do count 1 posiçao tras na lista: prev = prevXORnext XOR next
            countNextAddr = tempAddr;   //atualiza o endereço next da posicao count atual
            count--;
        }
        return countAddr;
    }

    public T getSlow(int i)
    {
        //Caso em que i não é uma posição válida da lista
        if(i < 0 || i >= this.size){
            throw new IndexOutOfBoundsException();
        }
        long resultAddr = getAddrFirstHalf(i);
        return UNode.get_item(resultAddr);
    }

    public T get(int i)
    {
        T result;
        //Caso em que i não é uma posição válida da lista
        if(i < 0 || i > this.size-1){
            throw new IndexOutOfBoundsException();
        }
        //Caso em que i se encontra na primeira metade da lista
        else if(i < (this.size/2)){
            result = this.getSlow(i);
        }
        //Caso em que i se encontra na segunda metade da lista
        else{
            long resultAddr = getAddrSecondHalf(i);
            result = UNode.get_item(resultAddr);
        }
        return result;
    }

    //Encontra o endereço do node que tem posição i e i-1 na lista
    //Trabalha para a primeira metade da lista
    public long[] getAddrFirstHalfForAts(int i){
        int count = 0;  //iterador da lista
        long countAddr = this.first;    //variavel long que tera sempre o endereço da posicao "count" da lista
        long countPrevAddr = NULL;  //variavel long que tera sempre o endereço prev da posicao "count" da lista
        while(count < i){
            long tempAddr = countAddr;  //guarda endereço do count atual que sera o prev da proxima iteracao
            countAddr = getBeyond(tempAddr, countPrevAddr);    //avança o endereço do count 1 posiçao a frente na lista: next = prevXORnext XOR prev
            countPrevAddr = tempAddr;   //atualiza o endereço prev da posicao count atual
            count++;
        }
        long[] results = new long[2];
        results[0] = countPrevAddr;
        results[1] = countAddr;
        return results;
    }

    //Encontra o endereço do node que tem posição i e i+1 na lista
    //Trabalha para a segunda metade da lista
    public long[] getAddrSecondHalfForAts(int i){
        int count = this.size-1;  //iterador da lista
        long countAddr = this.last;    //variavel long que tera sempre o endereço da posicao "count" da lista
        long countNextAddr = NULL;  //variavel long que tera sempre o endereço next da posicao "count" da lista
        while(count > i){
            long tempAddr = countAddr;  //guarda endereço do count atual que sera o next da proxima iteracao
            countAddr = getBeyond(tempAddr, countNextAddr);    //avança o endereço do count 1 posiçao tras na lista: prev = prevXORnext XOR next
            countNextAddr = tempAddr;   //atualiza o endereço next da posicao count atual
            count--;
        }
        long[] results = new long[2];
        results[0] = countAddr;
        results[1] = countNextAddr;
        return results;
    }

    public void addAt(int i, T item)
    {
        //Caso em que a lista é vazia e i = 0
        if(this.isEmpty() && i == 0){
            this.add(item);
        }
        //Caso em que i não é uma posição válida da lista
        else if(i < 0 || i > this.size){
            throw new IndexOutOfBoundsException();
        }
        //Caso em que i é o início da lista
        else if(i == 0){
            long tempNodeAddr = this.first; //guarda o antigo endereço de first
            this.first = UNode.create_node(item, NULL, tempNodeAddr);   //cria novo first com prev em NULL e next no antigo first
            updateNodeReference(tempNodeAddr, NULL, this.first);    //atualiza o ponteiro do antigo first de null para o novo first
            this.size++;
        }
        //Caso em que i é o fim da lista
        else if(i == this.size){
            this.add(item);
        }
        else{
            long iAddr; //endereço da posiçao i
            long iPrevAddr; //endereço da posiçao i-1
            //Caso em que i se encontra na primeira metade da lista
            if(i < (this.size/2)){
                long[] iPrevAddrAndiAddr = getAddrFirstHalfForAts(i);
                iPrevAddr = iPrevAddrAndiAddr[0]; //endereço da posiçao i-1
                iAddr = iPrevAddrAndiAddr[1];   //endereço da posiçao i
                long newNode = UNode.create_node(item, iPrevAddr, iAddr);   //novo node que ficará entre i-1 e i
                updateNodeReference(iPrevAddr, iAddr, newNode); //aponta o next do node i-1 para o newNode em vez do node i
                updateNodeReference(iAddr, iPrevAddr, newNode); //aponta o previous do node i para newNode em vez do node i-1
            }
            //Caso em que i se encontra na segunda metade da lista
            else{
                long[] iAddrAndiNextAddr = getAddrSecondHalfForAts(i);
                iAddr = iAddrAndiNextAddr[0];  //endereço da posiçao i
                iPrevAddr = getBeyond(iAddr, iAddrAndiNextAddr[1]); //endereço da posiçao i-1
                long newNode = UNode.create_node(item, iPrevAddr, iAddr);   //novo node que ficará entre i-1 e i
                updateNodeReference(iPrevAddr, iAddr, newNode); //aponta o next do node i-1 para o newNode em vez do node i
                updateNodeReference(iAddr, iPrevAddr, newNode); //aponta o previous do node i para newNode em vez do node i-1
            }
            this.size++;
        }
    }

    public T removeAt(int i)
    {
        T result;
        //Caso em que i não é uma posição válida da lista ou se a lista for vazia
        if(i < 0 || i > this.size-1 || this.isEmpty()){
            throw new IndexOutOfBoundsException();
        }
        //Caso em que i é o início da lista
        else if(i == 0){
            //Caso em que a lista tem apenas um elemento
            if(this.size == 1){
                result = UNode.get_item(this.first);
                UNode.free_node(this.first);
                //UNode.free_node(this.last);
            }
            else{
                long tempNodeAddr = getBeyond(this.first, NULL); //guarda o endereço de first+1 que passará a ser first
                result = UNode.get_item(this.first);
                updateNodeReference(tempNodeAddr, this.first, NULL);    //aponta o prev do first+1 para NULL em vez de first
                UNode.free_node(this.first);    //liberta a parte da memória onde estava guardado o endereço do first
                this.first = tempNodeAddr;  //first+1 passa a ser o novo first
            }
            size--;
        }
        //Caso em que i é o fim da lista
        else if(i == this.size-1){
            result = this.remove();
        }
        else{
            long iAddr; //endereço da posiçao i
            long iPrevAddr; //endereço da posiçao i-1
            long iNextAddr; //endereço da posiçao i+1
            //Caso em que i se encontra na primeira metade da lista
            if(i < (this.size/2)){
                long[] iPrevAddrAndiAddr = getAddrFirstHalfForAts(i);
                iAddr = iPrevAddrAndiAddr[1];   //endereço da posiçao i
                iPrevAddr = iPrevAddrAndiAddr[0]; //endereço da posiçao i-1
                iNextAddr = getBeyond(iAddr, iPrevAddr);  //endereço da posiçao i+1
                updateNodeReference(iPrevAddr, iAddr, iNextAddr); //aponta o next do node i-1 para o i+1 em vez do node i
                updateNodeReference(iNextAddr, iAddr, iPrevAddr); //aponta o previous do node i+1 para i-1 em vez do node i
                result = UNode.get_item(iAddr);
                UNode.free_node(iAddr); //liberta a parte da memória onde estava guardado o endereço do node i
            }
            //Caso em que i se encontra na segunda metade da lista
            else{
                long[] iAddrAndiNextAddr = getAddrSecondHalfForAts(i);
                iAddr = iAddrAndiNextAddr[0];  //endereço da posiçao i
                iNextAddr = iAddrAndiNextAddr[1];  //endereço da posiçao i+1
                iPrevAddr = getBeyond(iAddr, iNextAddr); //endereço da posiçao i-1
                updateNodeReference(iPrevAddr, iAddr, iNextAddr); //aponta o next do node i-1 para o i+1 em vez do node i
                updateNodeReference(iNextAddr, iAddr, iPrevAddr); //aponta o previous do node i+1 para i-1 em vez do node i
                result = UNode.get_item(iAddr);
                UNode.free_node(iAddr); //liberta a parte da memória onde estava guardado o endereço do node i
            }
            this.size--;
        }
        return result;
    }

    public void reverse()
    {
        long tempAddr = this.first;
        this.first = this.last;
        this.last = tempAddr;
    }

    public StingyList<T> reversed()
    {
        StingyList<T> result = new StingyList<>();
        result.size = this.size;
        result.first = this.last;
        result.last = this.first;
        return result;
    }

    public void clear()
    {
        while(this.size > 0){
            this.remove();
        }
    }

    public boolean isEmpty()
    {
        if(this.size == 0){
            return true;
        }
        return false;
    }

    public int size()
    {
        return this.size;
    }

    public Object[] toArray()
    {
        Object[] result = new Object[this.size];
        for(int i = 0; i < this.size; i++){
            result[i] = this.get(i);
        }
        return result;
    }

    public Iterator<T> iterator()
    {
        return new StingyListIterator();
    }

    private class StingyListIterator implements Iterator<T>{

        long prevI;

        long i;

        StingyListIterator(){
            this.prevI = NULL;
            this.i = first;
        }

        @Override
        public boolean hasNext() {
            return this.i != NULL;
        }

        @Override
        public T next() {
            T result = UNode.get_item(this.i);
            long tempPrevI = this.prevI;
            this.prevI = this.i;
            this.i = getBeyond(this.i, tempPrevI);
            return result;
        }
    }
/*
    public static StingyList<Integer> generateStingyListExample(int n){
        Random r = new Random();
        StingyList<Integer> example = new StingyList<>();
        for(int i = 0; i < n; i++){
            example.add(r.nextInt());
        }
        return example;
    }

    public static double calculateAverageExecutionTimeGetSlow(int n){
        int trials = 30;
        double totalTime = 0;
        for(int i = 0; i < trials; i++){
            StingyList<Integer> exampleList = generateStingyListExample(n);
            Random r = new Random();
            int randomI = r.nextInt(0, n);
            long time = System.currentTimeMillis();
            exampleList.getSlow(randomI);
            totalTime += System.currentTimeMillis() - time;
        }
        return totalTime/trials;
    }

    public static double calculateAverageExecutionTimeGet(int n){
        int trials = 30;
        double totalTime = 0;
        for(int i = 0; i < trials; i++){
            StingyList<Integer> exampleList = generateStingyListExample(n);
            Random r = new Random();
            int randomI = r.nextInt(0, n);
            long time = System.currentTimeMillis();
            exampleList.get(randomI);
            totalTime += System.currentTimeMillis() - time;
        }
        return totalTime/trials;
    }

    public static double calculateAverageExecutionTimeReverse(int n){
        int trials = 30;
        double totalTime = 0;
        for(int i = 0; i < trials; i++){
            StingyList<Integer> exampleList = generateStingyListExample(n);
            long time = System.currentTimeMillis();
            exampleList.reverse();
            totalTime += System.currentTimeMillis() - time;
        }
        return totalTime/trials;
    }

 */

    public static void main(String args[]){
        /*
        int n = 125;
        //double previousTime = calculateAverageExecutionTimeGetSlow(n);    //test getSlow
        //double previousTime = calculateAverageExecutionTimeGet(n);    //test get
        double previousTime = calculateAverageExecutionTimeReverse(n);  //test reverse
        double newTime;
        double doublingRatio;
        for(int i = 250; true; i*=2)
        {
            //newTime = calculateAverageExecutionTimeGetSlow(i);  //test getSlow
            //newTime = calculateAverageExecutionTimeGet(i);  //test get
            newTime = calculateAverageExecutionTimeReverse(i);  //test reverse
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