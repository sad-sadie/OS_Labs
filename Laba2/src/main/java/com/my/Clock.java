package com.my;


public enum Clock implements Runnable {
    INSTANCE();

    private int time;
    private boolean status;

    Clock() {
        time = 1000;
        status = false;
    }

    public int getTime() {
        return time;
    }

    public void setStatus(boolean state) {
        status = state;
    }

    public void logEvent(String m) {
        Main.log.info(m);
    }


    @Override
    public void run() {
        Main.log.info("Clock Started!");

        while(!status) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Main.log.error(e.getMessage());
            }

            time += 10;
        }

        Main.log.info("Clock Stopped!");
    }
}