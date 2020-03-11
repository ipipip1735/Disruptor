import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Administrator on 2020/3/6 5:29.
 */
public class DisruptorTrial {
    static int n = 100;

    public static void main(String[] args) {
        DisruptorTrial disruptorTrial = new DisruptorTrial();

//        disruptorTrial.singleProducer();//单生产者模式
//        disruptorTrial.multipleProducer();//多生产者模式

        disruptorTrial.dependency();//消费依赖
    }

    private void dependency() {


        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();
        ThreadFactory threadFactory = new OneThreadFactory();

        Disruptor<Integer[]> disruptor = new Disruptor(eventFactory, 8, threadFactory, ProducerType.SINGLE, waitStrategy);

        System.out.println(disruptor);

        //打印RingBuffer
        for (int i = 0; i < disruptor.getRingBuffer().getBufferSize(); i++) {
            System.out.println("[" + i + "]event is " + disruptor.getRingBuffer().get(i)[0]);
        }

        //注册多个消费者，1和2并行执行，然后3、4、5再并行执行
        disruptor.handleEventsWith(new OneEventHandler(1), new OneEventHandler(2))
                .then(new OneEventHandler(3), new OneEventHandler(4), new OneEventHandler(5));
        disruptor.start();//启动消费者线程


        //发送数据到RingBuffer
        for (int i = 0; i < 2; i++) {

            disruptor.publishEvent(new EventTranslator<Integer[]>() {
                @Override
                public void translateTo(Integer[] event, long sequence) {
//                    System.out.println("~~translateTo~~");
//                    System.out.println("sequence is " + sequence);
//                    System.out.println("event is " + event[0]);

                    event[0] = Integer.valueOf(999);
//                    System.out.println(Thread.currentThread());
                }
            });
        }

        disruptor.shutdown();//关闭消费者线程
    }

    private void multipleProducer() {

        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ExecutorService executorService = Executors.newFixedThreadPool(2);


        Disruptor<Integer[]> disruptor = new Disruptor(eventFactory, 16, threadFactory, ProducerType.MULTI, waitStrategy);


        //注册消费者
        disruptor.handleEventsWith(new EventHandler<>() {
            @Override
            public void onEvent(Integer[] event, long sequence, boolean endOfBatch) throws Exception {
                System.out.println("~~onEvent~~");
                System.out.println("sequence is " + sequence);
                System.out.println("endOfBatch is " + endOfBatch);
                System.out.println("event is " + event[0]);

                System.out.println(Thread.currentThread());
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        disruptor.start();//启动消费者线程


        //生产者
        Runnable task = new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < 10; i++) {
                    disruptor.publishEvent(new EventTranslator<Integer[]>() {
                        @Override
                        public void translateTo(Integer[] event, long sequence) {
                            System.out.println("~~translateTo~~");
                            System.out.println("sequence is " + sequence);
                            System.out.println("event is " + event[0]);

                            event[0] = Integer.valueOf(999);
                            System.out.println(Thread.currentThread());
                            try {
                                Thread.sleep(100L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        };

        //使用两个线程生产数据
        executorService.submit(task);
        executorService.submit(task);

        disruptor.shutdown();//关闭消费者线程
        executorService.shutdown();//关闭生产者线程

    }


    private void singleProducer() {

        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();
        ThreadFactory threadFactory = new OneThreadFactory();

        Disruptor<Integer[]> disruptor = new Disruptor(eventFactory, 16, threadFactory, ProducerType.SINGLE, waitStrategy);

        System.out.println(disruptor);

        //打印RingBuffer
        for (int i = 0; i < disruptor.getRingBuffer().getBufferSize(); i++) {
            System.out.println("[" + i + "]event is " + disruptor.getRingBuffer().get(i)[0]);
        }

        //注册消费者
        disruptor.handleEventsWith(new EventHandler<>() {
            @Override
            public void onEvent(Integer[] event, long sequence, boolean endOfBatch) throws Exception {
                System.out.println("~~onEvent~~");
                System.out.println("sequence is " + sequence);
                System.out.println("endOfBatch is " + endOfBatch);
                System.out.println("event is " + event[0]);

                System.out.println(Thread.currentThread());
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        //注册多个消费者
//        disruptor.handleEventsWith(new OneEventHandler(1))
//                .handleEventsWith(new OneEventHandler(2));
        disruptor.start();//启动消费者线程


        //发送数据到RingBuffer
        for (int i = 0; i < 26; i++) {

            disruptor.publishEvent(new EventTranslator<Integer[]>() {
                @Override
                public void translateTo(Integer[] event, long sequence) {
                    System.out.println("~~translateTo~~");
                    System.out.println("sequence is " + sequence);
                    System.out.println("event is " + event[0]);

                    event[0] = Integer.valueOf(999);
                    System.out.println(Thread.currentThread());
                }
            });
        }

        disruptor.shutdown();//关闭消费者线程
    }

    class OneEventFactory implements EventFactory<Integer[]> {
        int n = 300;

        @Override
        public Integer[] newInstance() {
            System.out.println("~~" + getClass().getSimpleName() + ".newInstance~~");

            Integer[] integer = new Integer[]{n++};
            return integer;
        }
    }


    class OneThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            System.out.println("~~" + getClass().getSimpleName() + ".newThread~~");

            return new Thread(r);
        }
    }

    class OneWaitStrategy implements WaitStrategy {
        @Override
        public long waitFor(long sequence, Sequence cursor, Sequence dependentSequence, SequenceBarrier barrier) throws AlertException, InterruptedException, TimeoutException {
            System.out.println("~~" + getClass().getSimpleName() + ".waitFor~~");
            System.out.println("sequence is " + sequence);
            System.out.println("cursor is " + cursor);
            System.out.println("dependentSequence is " + dependentSequence);
            System.out.println("barrier is " + barrier);
            return 0;
        }

        @Override
        public void signalAllWhenBlocking() {
            System.out.println("~~" + getClass().getSimpleName() + ".signalAllWhenBlocking~~");

        }
    }

    class OneEventHandler implements EventHandler<Integer[]> {
        int n;

        public OneEventHandler(int n) {
            this.n = n;
        }

        @Override
        public void onEvent(Integer[] event, long sequence, boolean endOfBatch) throws Exception {
            System.out.println("~~[" + this.n + "]onEvent~~");
            System.out.println("sequence is " + sequence);
            System.out.println("endOfBatch is " + endOfBatch);
            System.out.println("event is " + event[0]);

            if (n == 1) {
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
