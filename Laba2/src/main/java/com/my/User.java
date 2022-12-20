package com.my;

import java.util.List;
import java.util.ArrayList;


public class User {
    private final String userName;
    private final int processQty;
    private int userQuantum;

    private List<ProcessRunnable> waitingQ, readyQ;

    User(){ userName = " "; processQty = 0;}

    User(String name, int qty) {
        userName = name;
        processQty = qty;
        waitingQ = new ArrayList<>();
        readyQ = new ArrayList<>();
    }


    public String getName() {return userName;}
    public int getUserQuantum() {return userQuantum;}
    public int getProcessQty() {return processQty;}


    public List<ProcessRunnable> getWaitingProcessQ() {return waitingQ;}
    public List<ProcessRunnable> getReadyProcessQ() {return readyQ;}


    public void addProcess(int ready, int service){
        if (waitingQ.size() >= processQty)
            Main.log.error("Number of Processes Exceeds Specified Quantity!");
        else
            waitingQ.add(new ProcessRunnable(userName, ready, service));
    }


    public void addToReadyQueue(ProcessRunnable process) {
        readyQ.add(0, process);
        waitingQ.remove(process);
    }


    public void setUserQuantum(int quantum) {
        userQuantum = (quantum / readyQ.size());
    }


}