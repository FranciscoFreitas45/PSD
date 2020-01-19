package Worker;

import org.zeromq.SocketType;
import tp.Messages;
import tp.Messages.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.zeromq.ZMQ;

import java.nio.file.LinkOption;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Worker extends Thread {
    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket pull = context.socket(SocketType.PULL);
        ZMQ.Socket subSocket = context.socket(SocketType.SUB);
        ZMQ.Socket push = context.socket(SocketType.PUSH);

        push.connect("tcp://localhost:" + 12349);
        pull.connect("tcp://localhost:" + 12348);
        subSocket.connect("tcp://localhost:" + 12346);
        Notifier note = new Notifier(context);

        PullManufacturer pullManufacturer = new PullManufacturer(pull, subSocket,push,note);
        SubImporters subImporters = new SubImporters(pullManufacturer, subSocket);
        pullManufacturer.start();
        subImporters.start();
    }
}