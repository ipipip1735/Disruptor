import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * Created by Administrator on 2020/3/8 21:40.
 */
public class RingBufferTrial {
    static int n = 100;

    public static void main(String[] args) {
        RingBufferTrial ringBufferTrial = new RingBufferTrial();

//        ringBufferTrial.create();
//        ringBufferTrial.capacity();
//        ringBufferTrial.cursor();
//        ringBufferTrial.low();

//        ringBufferTrial.back();

        ringBufferTrial.multipleProducer();
    }

    private void back() {
        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        RingBuffer<Integer[]> ringBuffer = RingBuffer.create(ProducerType.SINGLE, eventFactory, 8, waitStrategy);
        Sequence customer = new Sequence();
        ringBuffer.addGatingSequences(customer);

        //例一：反向读
//        System.out.println("next is " + ringBuffer.next());

//        for (int i = 100; i > -200; i--) {
//            Integer[] integers = ringBuffer.claimAndGetPreallocated(i);
//            System.out.println("[" + i +"]" + integers[0]);
//        }

//        System.out.println("next is " + );
//        System.out.println(ringBuffer);




        //例二：回退重写
       for (int i = 0; i < 5; i++) {
            long seq = ringBuffer.next();
            Integer[] integers = ringBuffer.get(seq);
            integers[0] = 40 + i;
            System.out.println("add is " + seq);
            ringBuffer.publish(seq);
        }

        System.out.println("getCursor is " + ringBuffer.getCursor());


        for (int i = 0; customer.get() < ringBuffer.getCursor(); i++) {
            Integer[] integers = ringBuffer.get(i);
            System.out.println("[" + i + "]" + integers[0]);
            customer.set(i);
        }


        ringBuffer.claimAndGetPreallocated(2);
        for (int i = 2; i < 3; i++) {
            long seq = ringBuffer.next();
            Integer[] integers = ringBuffer.get(seq);
            integers[0] = 80 + i;
            System.out.println("update is " + seq);
            ringBuffer.publish(seq);
        }
        System.out.println("getCursor is " + ringBuffer.getCursor());


        customer.set(-1);
        for (int i = 0; customer.get() < ringBuffer.getCursor(); i++) {
            Integer[] integers = ringBuffer.get(i);
            System.out.println("[" + i + "]" + integers[0]);
            customer.set(i);
        }


    }

    private void multipleProducer() {
        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        RingBuffer<Integer[]> ringBuffer = RingBuffer.createMultiProducer(eventFactory, 4, waitStrategy);

        Sequence customerOne = new Sequence();
        Sequence customerTwo = new Sequence();
        ringBuffer.addGatingSequences(customerOne, customerTwo);
//        customerOne.set(0);
//        customerTwo.set(0);


        long seq = ringBuffer.next();
        ringBuffer.publish(seq);
        seq = ringBuffer.getCursor();
        System.out.println("getCursor is " + seq);

        //        for (int i = 0; i < 50; i++) {
//            ringBuffer.next();
            System.out.println("remainingCapacity is " + ringBuffer.remainingCapacity());
//        }

    }

    private void low() {
        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        RingBuffer<Integer[]> ringBuffer = RingBuffer.create(ProducerType.SINGLE, eventFactory, 4, waitStrategy);

//        Sequence customerOne = new Sequence();
//        ringBuffer.addGatingSequences(customerOne);
//        customerOne.set(5);

        ringBuffer.publish(20);
        System.out.println("getCursor is " + ringBuffer.getCursor());

        System.out.println("getMinimumGatingSequence is " + ringBuffer.getMinimumGatingSequence());
    }

    private void capacity() {

        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        RingBuffer<Integer[]> ringBuffer = RingBuffer.create(ProducerType.SINGLE, eventFactory, 4, waitStrategy);

        Sequence customer = new Sequence();
        ringBuffer.addGatingSequences(customer);

        for (int i = 0; i < 50; i++) {
            System.out.println("next is " + ringBuffer.next(2));
        }

//        for (int i = 0; i < 50; i++) {
//            ringBuffer.next();
//            System.out.println("remainingCapacity is " + ringBuffer.remainingCapacity());
//        }

//        for (int i = 0; i < 50; i++) {
//            ringBuffer.next();
//            System.out.println("hasAvailableCapacity is " + ringBuffer.hasAvailableCapacity(1));
//        }

    }

    private void cursor() {
        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        RingBuffer<Integer[]> ringBuffer = RingBuffer.create(ProducerType.SINGLE, eventFactory, 4, waitStrategy);

//        ringBuffer.claimAndGetPreallocated(2);
//        ringBuffer.publish(0);
        ringBuffer.publish(50);
        System.out.println("getCursor is " + ringBuffer.getCursor());
    }

    private void create() {

        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        //方式一：手动指定生产者
        RingBuffer<Integer[]> ringBuffer = RingBuffer.create(ProducerType.SINGLE, eventFactory, 2, waitStrategy);
//        RingBuffer<Integer[]> ringBuffer = RingBuffer.create(ProducerType.MULTI, eventFactory, 2, waitStrategy);

        //方式二：使用单生产者
//        RingBuffer<Integer[]> ringBuffer = RingBuffer.createSingleProducer(new OneEventFactory(), 2);
//        RingBuffer<Integer[]> ringBuffer = RingBuffer.createSingleProducer(eventFactory, 2, waitStrategy);

        //方式三：使用多生产者
//        RingBuffer<Integer[]> ringBuffer = RingBuffer.createMultiProducer(eventFactory, 2);
//        RingBuffer<Integer[]> ringBuffer = RingBuffer.createMultiProducer(eventFactory, 2, waitStrategy);


        System.out.println(ringBuffer);
        System.out.println("getBufferSize is " + ringBuffer.getBufferSize());
        System.out.println("getCursor is " + ringBuffer.getCursor());


        for (int i = 0; i < ringBuffer.getBufferSize(); i++) {
            Integer[] integers = ringBuffer.get(i);
            System.out.println(integers[0]);
        }


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
}
