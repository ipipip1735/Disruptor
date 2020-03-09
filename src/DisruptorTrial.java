import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Administrator on 2020/3/6 5:29.
 */
public class DisruptorTrial {
    static int n = 100;

    public static void main(String[] args) {
        DisruptorTrial disruptorTrial = new DisruptorTrial();

        disruptorTrial.disruptor();
//        disruptorTrial.customer();
//        disruptorTrial.productor();
//        disruptorTrial.disruptorWith();
//        disruptorTrial.barrier();
//        disruptorTrial.poller();
//        disruptorTrial.sequence();
//        disruptorTrial.sequencer();


    }

    private void customer() {
        Disruptor<Integer[]> disruptor = new Disruptor(new OneEventFactory(), 4, Executors.defaultThreadFactory(), ProducerType.SINGLE, new BlockingWaitStrategy());

        RingBuffer<Integer[]> ringBuffer = disruptor.getRingBuffer();
        SequenceBarrier barrier = ringBuffer.newBarrier();


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    System.out.println("waiting...");
                    long seq = barrier.waitFor(3);
                    System.out.println("seq is " + seq);
                } catch (AlertException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();


        for (int i = 0; i < 9; i++) {
            ringBuffer.publish(i);

            System.out.println("thread is " + thread.getState());
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    private void productor() {
        Thread main = Thread.currentThread();

        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        Disruptor<Integer[]> disruptor = new Disruptor(new OneEventFactory(), 2, Executors.defaultThreadFactory(), ProducerType.SINGLE, waitStrategy);



//        RingBuffer<Integer[]> ringBuffer = disruptor.getRingBuffer();
        RingBuffer<Integer[]> ringBuffer = RingBuffer.createSingleProducer(new OneEventFactory(), 2);
        SequenceBarrier barrier = ringBuffer.newBarrier();
        Sequence sequence = new Sequence();
        ringBuffer.addGatingSequences(sequence);


        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    for (int i = 0; i < 10; i++) {

                        long seq = barrier.waitFor(i);
                        sequence.set(seq);

                        System.out.println("[c]seq is " + seq);
                        System.out.println("[c]main is " + main.getState());
                        Thread.sleep(1000L);
                    }


                } catch (AlertException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }

            }
        }).start();


        for (int i = 0; i < 9; i++) {
            System.out.println(i);

            long s = ringBuffer.next();
            System.out.println("pulish is " + s);


//            System.out.println("thread is " + thread.getState());
//            try {
//                Thread.sleep(100L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }


    }

    private void sequencer() {
        System.out.println("start");
        SingleProducerSequencer sequencer = new SingleProducerSequencer(4, new OneWaitStrategy());
        SequenceBarrier sequenceBarrier = sequencer.newBarrier(new Sequence());
        try {
            sequenceBarrier.waitFor(3);
        } catch (AlertException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

//        for (int i = 0; i < 9; i++) {
//            sequencer.claim(i);
//            System.out.println("getCursor is " + sequencer.getCursor());
//        }

        for (int i = 0; i < 9; i++) {
            long seq = sequencer.next(1);
            sequencer.publish(seq);

//            System.out.println("hasAvailableCapacity is " + sequencer.hasAvailableCapacity(5));
        }
        System.out.println("end");

    }

    private void sequence() {

        Sequence sequence = new Sequence();
        System.out.println("seq is " + sequence.get());
        System.out.println("incrementAndGet is " + sequence.incrementAndGet());
        System.out.println("seq is " + sequence.get());
        System.out.println("addAndGet 2 is " + sequence.addAndGet(2));
        System.out.println("seq is " + sequence.get());

    }

    private void disruptorWith() {

        Thread main = Thread.currentThread();

//        Disruptor<Integer[]> disruptor = new Disruptor(new OneEventFactory(), 2, Executors.defaultThreadFactory());
        Disruptor<Integer[]> disruptor = new Disruptor(new OneEventFactory(), 4, Executors.defaultThreadFactory(), ProducerType.SINGLE, new BlockingWaitStrategy());


        disruptor.handleEventsWith(new EventHandler<Integer[]>() {
            @Override
            public void onEvent(Integer[] event, long sequence, boolean endOfBatch) throws Exception {
                System.out.println("~~" + getClass().getSimpleName() + ".onEvent~~");
//                try {
//                    Thread.sleep(3000L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                System.out.println("sequence is " + sequence);
                System.out.println("endOfBatch is " + endOfBatch);
                System.out.println("event is " + event[0]);

                System.out.println("main is " + main.getState());

                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        RingBuffer<Integer[]> ringBuffer = disruptor.start();

        for (int i = 0; i < 19; i++) {
            ringBuffer.publishEvent(new EventTranslator<Integer[]>() {
                @Override
                public void translateTo(Integer[] event, long sequence) {
                    System.out.println("~~" + getClass().getSimpleName() + ".translateTo~~");


                    System.out.println("[T]sequence is " + sequence);
                    System.out.println("event is " + event[0]);
                }
            });
        }

    }

    private void poller() {
        EventFactory<Integer[]> eventFactory = new OneEventFactory();


        Disruptor<Integer[]> disruptor = new Disruptor(eventFactory, 1, Executors.defaultThreadFactory(), ProducerType.SINGLE, new BlockingWaitStrategy());
        RingBuffer<Integer[]> ringBuffer = disruptor.getRingBuffer();

        Sequence sequence = new Sequence();
        System.out.println("sequence is " + sequence);
        ringBuffer.newPoller(sequence);


//        long sequence = ringBuffer.next();
//        System.out.println("sequence is " + sequence);


    }

    private void barrier() {
        EventFactory<Integer[]> eventFactory = new OneEventFactory();


        Disruptor<Integer[]> disruptor = new Disruptor(eventFactory, 1, Executors.defaultThreadFactory(), ProducerType.SINGLE, new BlockingWaitStrategy());
        RingBuffer<Integer[]> ringBuffer = disruptor.getRingBuffer();
        SequenceBarrier barrier = ringBuffer.newBarrier();

        System.out.println(barrier);
        System.out.println("getCursor is " + barrier.getCursor());
        System.out.println("isAlerted is " + barrier.isAlerted());


//        for (int i = 0; i < 10; i++) {
//            long sequence = ringBuffer.next();
//            ringBuffer.get(sequence);
//            ringBuffer.publish(sequence);
////            try {
////                Thread.sleep(3000L);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//        }


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                System.out.println("----------start");


                try {
                    long seq = barrier.waitFor(3);
                    System.out.println("seq is " + seq);
                } catch (AlertException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
                System.out.println("----------end");

            }
        });
        thread.start();

        for (int i = 0; i < 1024; i++) {

            long sequence = ringBuffer.next();
            System.out.println("sequence is " + sequence);
//            System.out.println("getState is " + thread.getState());


            ringBuffer.get(sequence);

            ringBuffer.publish(sequence);

            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


//        System.out.println("alert is " + barrier.alert());
//        System.out.println("checkAlert is " + barrier.checkAlert());
//        System.out.println("clearAlert is " + barrier.clearAlert());
//        System.out.println("waitFor is " + barrier.waitFor(1));


    }

    private void disruptor() {
        EventFactory<Integer[]> eventFactory = new OneEventFactory();
        ThreadFactory threadFactory = new OneThreadFactory();
//        ThreadFactory threadFactory = Executors.defaultThreadFactory();

        Disruptor<Integer[]> disruptor = new Disruptor(eventFactory, 2, threadFactory, ProducerType.SINGLE, new BlockingWaitStrategy());
//        Disruptor<Integer[]> disruptor = new Disruptor(new OneEventFactory(), 4, Executors.defaultThreadFactory(), ProducerType.SINGLE, new BlockingWaitStrategy());
        System.out.println(disruptor);

        RingBuffer<Integer[]> ringBuffer = disruptor.getRingBuffer();


//        long sequence = ringBuffer.next();
//        ringBuffer.publish(sequence);
//        Integer[] integers = ringBuffer.get(0);
//        System.out.println(sequence + " - " + integers[0] + " - " + ringBuffer.getCursor());


        Thread main = Thread.currentThread();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    long sequence = ringBuffer.next();
                    SequenceBarrier sequenceBarrier = ringBuffer.newBarrier(new Sequence());
                    System.out.println("[C]" + Thread.currentThread().getState());
                    long seq = sequenceBarrier.waitFor(0);
                    Integer[] integers = ringBuffer.get(seq);
                    System.out.println("[C]" + seq + " - " + integers[0] + " - " + ringBuffer.getCursor());
                    System.out.println("[C]mian is " + main.getState());
                } catch (AlertException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();


//        for (int i = 0; i < 13; i++) {
//            long sequence = ringBuffer.next();
//            Integer[] integers = ringBuffer.get(sequence);
//            System.out.println(sequence + " - " + integers[0] + " - " + ringBuffer.getCursor());
////            System.out.println("thread is " + thread.getState());
//            ringBuffer.publish(sequence);
//        }
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

}
