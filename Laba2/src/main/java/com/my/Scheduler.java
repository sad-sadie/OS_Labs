package com.my;

import java.util.ArrayList;
import java.util.List;


public class Scheduler implements Runnable{
    private final int quantum;

    private final List<User> waitingUserQ;
    private final List<User> readyUserQ;
    private final List<Thread> threadQueue;


    public Scheduler(String q){
        quantum = Integer.parseInt(q);

        readyUserQ = new ArrayList<>();
        waitingUserQ = new ArrayList<>();
        threadQueue = new ArrayList<>();
    }


    public void parseInput(ArrayList<String> input){
        int arrival, duration;

        User user = new User();
        for(int i = 1; i < input.size() - 1; i++)

            if(!input.get(i).matches("[0-9]+")) {
                user = new User(input.get(i++), Integer.parseInt(input.get(i)));
                waitingUserQ.add(user);

            } else {
                arrival = Integer.parseInt(input.get(i++));
                duration = Integer.parseInt(input.get(i));
                user.addProcess(arrival, duration);
            }
    }


    @Override
    public void run() {
        Main.log.info("Scheduler Started!");

        do {
            readyCheck();

            quantumSeparator();

            fairShareScheduling();

            removeEmptyUser();
        } while(!waitingUserQ.isEmpty() || !readyUserQ.isEmpty());

        threadQueue.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Main.log.error(e.getMessage());
            }
        });

        Main.log.info("Scheduler Stopped!");
    }


    public void readyCheck() {
        for (int i = 0; i < waitingUserQ.size(); i++) {
            User user = waitingUserQ.get(i);

            for (int j = 0; j < user.getWaitingProcessQ().size(); j++) {
                ProcessRunnable process = user.getWaitingProcessQ().get(j);

                if (1000 * process.getReady() <= Clock.INSTANCE.getTime() && !user.getReadyProcessQ().contains(process)) {
                    user.addToReadyQueue(process);

                    if (!readyUserQ.contains(user)) {
                        readyUserQ.add(user);
                        --i;
                    }

                    --j;
                }
            }
        }
    }


    public void quantumSeparator() {
        readyUserQ.forEach(user -> user.setUserQuantum(quantum / readyUserQ.size()));
    }



    public void fairShareScheduling() {
        int currTime;
        int startTime = Clock.INSTANCE.getTime();

        for(User user: readyUserQ) {
            for(int i = 0; i < user.getReadyProcessQ().size(); i++) {

                if(user.getReadyProcessQ().get(i).getProcessState() == -1) {
                    Thread processT = new Thread(user.getReadyProcessQ().get(i));
                    threadQueue.add(processT);
                    user.getReadyProcessQ().get(i).setProcessState(2);
                    processT.start();
                } else {
                    user.getReadyProcessQ().get(i).setProcessState(1);
                }

                currTime = Clock.INSTANCE.getTime();
                while (currTime - startTime < 1000 * user.getUserQuantum()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Main.log.error(e.getMessage());
                    }

                    currTime = Clock.INSTANCE.getTime();
                }

                if(user.getReadyProcessQ().get(i).getProcessState() != 4 && user.getReadyProcessQ().get(i).getProcessState() != 0)
                    user.getReadyProcessQ().get(i).setProcessState(0);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Main.log.error(e.getMessage());
                }

                startTime = Clock.INSTANCE.getTime();
            }
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Main.log.error(e.getMessage());
        }
    }

    public void removeEmptyUser() {
        for(int i = 0; i < readyUserQ.size(); i++) {
            User user = readyUserQ.get(i);
            for(int j = 0; j < user.getReadyProcessQ().size(); j++) {
                ProcessRunnable process = user.getReadyProcessQ().get(j);


                if(process.getProcessState() == 4) {
                    user.getReadyProcessQ().remove(process);
                    --j;
                }
            }


            if(user.getReadyProcessQ().isEmpty())
                readyUserQ.remove(user);
        }

        for(int i = 0; i < waitingUserQ.size(); i++) {
            User user = waitingUserQ.get(i);

            if(user.getWaitingProcessQ().isEmpty())
                waitingUserQ.remove(user);

        }
    }


    public void printData() {
        waitingUserQ.forEach(user -> {
            Main.log.info("User " + user.getName() + " Has " + user.getProcessQty() + " Processes:");
            for (ProcessRunnable process : user.getWaitingProcessQ())
                Main.log.info(process.getData());
            Main.log.info(" ");
        });
    }
}