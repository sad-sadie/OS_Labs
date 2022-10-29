package task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;

import static task.Manager.serverHost;
import static task.Manager.serverPort;

public class Computation extends Thread {

    private final Function<Integer, Optional<Optional<Boolean>>> function;

    private static final int MAX_COMPUTATION_ATTEMPTS = 5;

    private String result;

    public Computation(Function<Integer, Optional<Optional<Boolean>>> function) {
        this.function = function;
    }

    @Override
    public void run() {
        SocketChannel client;
        int x;
        try {
            client = SocketChannel.open(new InetSocketAddress(serverHost, serverPort));
            ByteBuffer inBuf = ByteBuffer.allocate(1024);
            client.read(inBuf);
            x = Character.getNumericValue(new String(inBuf.array(), StandardCharsets.UTF_8).charAt(0));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        Optional<Optional<Boolean>> softOptional = Optional.empty();
        for (int i = 0; i < MAX_COMPUTATION_ATTEMPTS; i++) {
            softOptional = function.apply(x);
            if (softOptional.isPresent()){
                break;
            }
        }
        if (softOptional.isPresent()){
            Optional<Boolean> hardOptional = softOptional.get();
            if(hardOptional.isPresent()){
                result = hardOptional.get() ? "1" : "0";
            } else {
                result = "HardFail";
                System.exit(1);
            }
        } else {
            result = "SoftFail";
            System.exit(1);
        }

        try {
            result += Thread.currentThread().getName();
            byte[] bs = result.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(bs);
            while (buffer.hasRemaining()) {
                client.write(buffer);
            }

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getResult(){
        return result;
    }
}