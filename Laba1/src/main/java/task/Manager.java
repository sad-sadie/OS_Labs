package task;

import os.lab1.compfuncs.advanced.Conjunction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class Manager {

    public static String serverHost = "localhost";
    public static int serverPort = 12345;
    private ServerSocketChannel server;

    private int x;
    private Computation computationF;
    private Computation computationG;
    private volatile boolean computationsCompleted;
    private Boolean result;

    private Function<Integer, Optional<Optional<Boolean>>> f;
    private Function<Integer, Optional<Optional<Boolean>>> g;

    AtomicBoolean confirmation = new AtomicBoolean(false);

    public Manager() {
        initializeFunctions();
        connectSockets();
        addShutDownHook();
    }



    public void run() {
        x = getX();
        new Thread(getServer()).start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        computationF.start();
        computationG.start();

        Thread cancel = new Thread(cancel());
        cancel.setDaemon(true);
        cancel.start();

        while(!computationsCompleted) {}

        if(confirmation.get()) {
            System.out.println("Overridden by system:");
            confirmation.set(false);
        }
        System.out.println("Result: " + result);
        System.exit(1);
    }

    private Runnable getServer() {
        return () -> {
            try {
                server = ServerSocketChannel.open();
                server.socket().bind(new InetSocketAddress(serverHost, serverPort));
                server.configureBlocking(false);

                Selector selector = Selector.open();
                server.register(selector, SelectionKey.OP_ACCEPT);

                while (true) {
                    selector.select();
                    Set<SelectionKey> readyKeys = selector.selectedKeys();

                    Iterator<SelectionKey> iterator = readyKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isAcceptable()) {
                            SocketChannel client = server.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_WRITE);

                        } else if (key.isReadable()) {

                            SocketChannel client = (SocketChannel) key.channel();
                            ByteBuffer inBuf = ByteBuffer.allocate(1024);
                            client.read(inBuf);
                            char getFromBuffer = new String(inBuf.array(), StandardCharsets.UTF_8).charAt(0);
                            boolean computationResult = getFromBuffer == '1';

                            if(result == null) {
                                result = computationResult;
                            } else {
                                result &= computationResult;
                                computationsCompleted = true;
                            }

                            key.cancel();
                        } else if (key.isWritable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            String response = String.valueOf(x);
                            byte[] bs = response.getBytes(StandardCharsets.UTF_8);
                            ByteBuffer buffer = ByteBuffer.wrap(bs);
                            client.write(buffer);

                            client.register(selector, SelectionKey.OP_READ);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }



    private static  int getX() {
        System.out.println("Enter x: ");
        return new Scanner(System.in).nextInt();
    }

    private void initializeFunctions() {
        f = x -> {
            try {
                return Conjunction.trialF(x);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        g = x -> {
            try {
                return Conjunction.trialG(x);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void connectSockets() {
        computationF = new Computation(f);
        computationG = new Computation(g);
    }


    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            if(confirmation.get()) {
                System.out.println("Overridden by system:");
            }

            String computationFResult = computationF.getResult();
            if(computationFResult == null) {
                System.out.println("f(x) - not finished");
            } else {
                switch (computationFResult) {
                    case "HardFail" -> System.out.println("f(x) - hard fail");
                    case "SoftFail" -> System.out.println("f(x) - soft fail");
                    default -> System.out.println("f(x) - computed");
                }
            }

            String computationGResult = computationG.getResult();
            if(computationGResult == null) {
                System.out.println("g(x) - not finished");
            } else {
                switch (computationGResult) {
                    case "HardFail" -> System.out.println("g(x) - hard fail");
                    case "SoftFail" -> System.out.println("g(x) - soft fail");
                    default -> System.out.println("g(x) - computed");
                }
            }
        }
        ));
    }



    private Runnable confirm(AtomicLong start, AtomicBoolean confirmation) {
        return () -> {
            try {
                Thread.sleep(5000);

                confirmation.set(false);
                start.set(0);

                System.out.println("Action is not confirmed after 5 seconds duration");
            } catch (InterruptedException ignored) {
            }
        };
    }

    private Runnable cancel(){
        return () -> {

            AtomicLong start = new AtomicLong(0);
            Scanner scanner = new Scanner(System.in);
            Thread confirmationThread = null;

            while (true) {
                String input = scanner.nextLine();
                if (!confirmation.get() && input.equals("c")) {
                    System.out.println("Please confirm whether the computations should be cancelled y/n:");
                    start.set(System.currentTimeMillis());
                    confirmation.set(true);

                    confirmationThread = new Thread(confirm(start, confirmation));
                    confirmationThread.setDaemon(true);
                    confirmationThread.start();

                } else if (confirmation.get() && input.equals("y")) {
                    System.out.println("Computations were cancelled");
                    Objects.requireNonNull(confirmationThread).interrupt();
                    System.exit(0);
                } else if(confirmation.get() && input.equals("n")){
                    confirmation.set(false);
                    start.set(0);
                    System.out.println("Computing continues");
                    Objects.requireNonNull(confirmationThread).interrupt();
                }
            }
        };
    }
}