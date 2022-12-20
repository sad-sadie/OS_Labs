package com.my;

public class ProcessRunnable implements Runnable {

    private final String processName;
    public String userName;
    static private int nameCounter = 0;

    private final int readyTime;
    private final int serviceTime;
    private int cpuTime;

    private int processState;

    ProcessRunnable(String uName, int ready, int service) {
        userName = uName;
        processName = "P" + nameCounter++;
        processState = -1;

        readyTime = ready;
        serviceTime = service;
        cpuTime = 0;
    }


    public void setProcessState(int state) {processState = state;}


    public int getReady() { return readyTime; }
    public int getProcessState() { return processState; }

    @Override
    public void run() {
        int currTime = Clock.INSTANCE.getTime();
        Clock.INSTANCE.logEvent("Time " + currTime + ", User " + userName + ", Process " + processName + " Started!");

        do {
            synchronized (this) {
                currTime = Clock.INSTANCE.getTime();
                switch (processState) {
                    case 0:
                        Clock.INSTANCE.logEvent("Time " + currTime + ", User " + userName + ", Process " + processName + " Paused.");
                        processState = 3;
                        break;
                    case 1:
                        Clock.INSTANCE.logEvent("Time " + currTime + ",User " + userName + ", Process " + processName + " Resumed.");
                        processState = 2;
                        cpuTime += 10;
                        break;
                    case 2:
                        cpuTime += 10;
                        break;
                    case 3:
                        break;
                    default:
                        Main.log.error("Error: State " + processState + " Doesn't Exist!!");
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Main.log.error(e.getMessage());
            }
        } while (cpuTime < 1000 * serviceTime);

        processState = 4;
        Clock.INSTANCE.logEvent("Time " + currTime + ", User " + userName + ", Process " + processName + " Finished!");
    }

    public String getData(){
        return "Process Name: " + processName + " => Arrival Time: " + readyTime + ", Burst Time: " + serviceTime;
    }
}
