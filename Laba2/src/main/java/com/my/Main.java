package com.my;

import java.util.ArrayList;
import org.apache.log4j.Logger;

public class Main {
    public static final Logger log = Logger.getLogger("FS");


    public static void main(String[] args) {

        String fileName = "Input";
        FileReader fileReader = new FileReader();
        ArrayList<String> input = fileReader.readFile(fileName);


        Scheduler scheduler = new Scheduler(input.get(0));
        scheduler.parseInput(input);
        scheduler.printData();

        log.info("Fair Share Scheduling Started!");

        Thread schedulerT = new Thread(scheduler);
        schedulerT.start();

        Thread clockT = new Thread(Clock.INSTANCE);
        clockT.start();

        try {
            schedulerT.join();
            Clock.INSTANCE.setStatus(true);
            clockT.join();
        } catch (InterruptedException e) {
            Main.log.error(e.getMessage());
        }

        log.info("Fair Share Scheduling Complete!");
    }
}