package PacmanPack;

public class GameMode extends Thread {
    private boolean chaseMode;
    private int scatterTime;
    private int chaseTime;
    private static int cycle = 1;
    private int frightenedTime;
    private boolean startedFrighted = false;


    @Override
    public void run() {
        chaseMode = false;
        modeSleep(scatterTime);
        chaseMode = true;
        modeSleep(chaseTime);
        chaseMode = false;
        if (frightenedTime == 0) {
            incCycle();
        } else {
            frightenedMode();
        }
    }

    public int getFrightenedTime() {
        return frightenedTime;
    }

    private void frightenedMode() {
//        System.out.println("FRIGHTENED");
        frightenedSleep(frightenedTime);
        frightenedTime = 0;
//        System.out.println("Frightened ended");
    }


    public void setFrightened(int frightenedTime) {
        startedFrighted = true;
        this.frightenedTime = frightenedTime;
        synchronized (this) {
            this.notifyAll();
        }
    }

    private void modeSleep(int sleepTime) {
        if (frightenedTime == 0) {
            try {
                synchronized (this) {
                    wait(sleepTime * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void frightenedSleep(int sleepTime) {
        try {
            synchronized (this) {
                wait(sleepTime * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setGameMode(int scatter, int chase) {
        this.scatterTime = scatter;
        this.chaseTime = chase;
    }

    public boolean isChaseMode() {
        return chaseMode;
    }

    public void resetCycle() {
        cycle = 0;
    }

    public void incCycle() {
        ++cycle;
    }

    public int getCycle() {
        return cycle;
    }

}
