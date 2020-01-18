import java.util.ArrayDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class InfoCliente {
    private Integer type;// 1 -Fabricante  2-Importador
    private boolean logged;
    private boolean reply;
    private ReentrantLock lock = new ReentrantLock();
    private Condition waitReply = lock.newCondition();


    InfoCliente(Integer type) {
        this.type = type;
        this.logged = false;
        this.reply=false;


    }

    synchronized public boolean isLogged() {
        return logged;
    }

    synchronized public void setLogged(boolean status) {
        logged = status;
    }

     synchronized public void setType(int type){
        this.type=type;

    }

    public Integer getType() {
        return type;
    }

    public void waitReply() {
        try {
            lock.lock();
            this.reply = true;
            while (this.reply)
                this.waitReply.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void awake(){
        try{
            lock.lock();
            this.reply = false;
            this.waitReply.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

}
