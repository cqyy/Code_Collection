package com.cqyuanye.ipc;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Kali on 14-8-4.
 */
class Client {
    private static final Logger LOG = Logger.getLogger(Client.class);

    private final Map<InetSocketAddress,Connection> connections = new HashMap<>();
    private final AtomicInteger callIdGen = new AtomicInteger(0);

    public Object call(InetSocketAddress server,Serializable param) throws Throwable {
        Call call = new Call(callIdGen.getAndIncrement(),param);
        try {
            Connection connection = getConnection(server);
            connection.sendParam(call);
        } catch (IOException e) {
            throw new IOException("Could not connect to server at " + server);
        }

        while (!call.isDone()){
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            }catch (InterruptedException ignored){}
        }
        if (call.hasError()){
            throw call.throwable;
        }
        return call.value();
    }

    private Connection getConnection(InetSocketAddress address) throws IOException {
        Connection con = connections.get(address);
        if (con == null){
            con = new Connection(address);
            con.start();
        }
        connections.put(address,con);
        return con;
    }

    private class Connection extends Thread{

        private static final int INTERVAL = 60;   ///1 min
        private volatile boolean shouldRun = true;

        //map index to call
        private final Map<Integer,Call> calls = new HashMap<>();
        private final InetSocketAddress server;
        private final Socket socket;
        private final InputStream is;
        private final OutputStream os;

        public Connection(InetSocketAddress address) throws IOException {
            this.server = address;
            if (server.isUnresolved()){
                throw new IOException("Unknown address " + server);
            }
            socket = new Socket();
            try {
                socket.connect(server);
            } catch (IOException e) {
                throw new IOException("Could not connect to server at " + server);
            }

            try {
                is = socket.getInputStream();
                os = socket.getOutputStream();
            }catch (IOException e){
                throw new IOException("Could not create IO stream to server at " + server);
            }

            this.setDaemon(true);
        }

        public void sendParam(Call call) throws IOException {
            DataOutputStream dos = new DataOutputStream(os);

            try {
                dos.writeInt(call.index);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(call.param);
                byte[] data = byteArrayOutputStream.toByteArray();
                dos.writeInt(data.length);
                dos.write(data);
                calls.put(call.index,call);
            } catch (IOException e) {
                throw e;
            }

        }


        public void close(){
            shouldRun = false;
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        @Override
        public void run() {
            DataInputStream dis = new DataInputStream(is);
            while (shouldRun){
                Call call = null;
                try {
                    int idx = dis.readInt();
                    call = calls.get(idx);
                    assert call != null:"Call should exists";
                    call.error = !dis.readBoolean();
                    if (call.error){
                        ObjectInputStream objectInputStream = new ObjectInputStream(is);
                        call.setException((Throwable)objectInputStream.readObject());
                    }else {
                        ObjectInputStream objectInputStream = new ObjectInputStream(is);
                        call.value(objectInputStream.readObject());
                    }
                } catch (IOException e) {
                    close();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    call.setDone();
                    calls.remove(call.index);
                }


            }
        }
    }


    public static class Call{
        private final int index;
        private final Serializable param;
        private Object value;
        private volatile boolean done;
        private boolean error;
        private Throwable throwable;

        Call(int index,Serializable param){
            this.index = index;
            this.param = param;
        }

        public void setException(Throwable throwable){
            this.throwable = throwable;
        }

        public Throwable getException(){
            return throwable;
        }

        public boolean isDone(){
            return done;
        }

        public void setDone(){
            done = true;
        }

        public boolean hasError(){
            return error;
        }

        public Object value(){
            return value;
        }

        public void value(Object value){
            this.value = value;
        }
    }

}
