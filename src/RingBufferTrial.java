import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * Created by Administrator on 2020/3/8 21:40.
 */
public class RingBufferTrial {
    static int n = 100;

    public static void main(String[] args) {
        RingBufferTrial ringBufferTrial = new RingBufferTrial();

//        ringBufferTrial.create();//创建RingBuffer
//        ringBufferTrial.put();//增加元素
//        ringBufferTrial.capacity();//获取体积
//        ringBufferTrial.cursor();//指针操作
//        ringBufferTrial.low();//获取最小指针
//        ringBufferTrial.back();//反向读取

//        ringBufferTrial.multipleProducer();//多生产者模式
    }

    private void put() {
        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        RingBuffer<Integer[]> ringBuffer = RingBuffer.createSingleProducer(eventFactory, 4, waitStrategy);

        //增加消费者
        Sequence customer = new Sequence();
        ringBuffer.addGatingSequences(customer);


        long sequence = ringBuffer.next();//申请序号
        Integer[] integers = ringBuffer.get(sequence);//获取元素
        integers[0] = Integer.valueOf(999);//更新元素
        System.out.println("[before]cursor is " + ringBuffer.getCursor());
        ringBuffer.publish(sequence);//更新序号，并通知消费者们（阻塞的消费者将被唤醒）
        System.out.println("[after]cursor is " + ringBuffer.getCursor());

    }

    private void back() {
        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        RingBuffer<Integer[]> ringBuffer = RingBuffer.createSingleProducer(eventFactory, 4, waitStrategy);
        Sequence customer = new Sequence();
        ringBuffer.addGatingSequences(customer);

        //反向读
        for (int i = 100; i > -200; i--) {
            Integer[] integers = ringBuffer.claimAndGetPreallocated(i);
            System.out.println("[" + i +"]" + integers[0]);
        }



        //回退重写
//        for (int i = 0; i < 5; i++) {
//            long seq = ringBuffer.next();
//            Integer[] integers = ringBuffer.get(seq);
//            integers[0] = 40 + i;
//            System.out.println("add is " + seq);
//            ringBuffer.publish(seq);
//        }
//
//        System.out.println("getCursor is " + ringBuffer.getCursor());
//
//
//        for (int i = 0; customer.get() < ringBuffer.getCursor(); i++) {
//            Integer[] integers = ringBuffer.get(i);
//            System.out.println("[" + i + "]" + integers[0]);
//            customer.set(i);
//        }
//
//
//        ringBuffer.claimAndGetPreallocated(2);
//        for (int i = 2; i < 3; i++) {
//            long seq = ringBuffer.next();
//            Integer[] integers = ringBuffer.get(seq);
//            integers[0] = 80 + i;
//            System.out.println("update is " + seq);
//            ringBuffer.publish(seq);
//        }
//        System.out.println("getCursor is " + ringBuffer.getCursor());
//
//
//        customer.set(-1);
//        for (int i = 0; customer.get() < ringBuffer.getCursor(); i++) {
//            Integer[] integers = ringBuffer.get(i);
//            System.out.println("[" + i + "]" + integers[0]);
//            customer.set(i);
//        }


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

        long seq;
        seq = ringBuffer.next(1);
//        ringBuffer.publish(seq);
//        ringBuffer.publish(1);
        seq = ringBuffer.getCursor();
        System.out.println("getCursor is " + seq);

        //        for (int i = 0; i < 50; i++) {
//            ringBuffer.next();
//            System.out.println("remainingCapacity is " + ringBuffer.remainingCapacity());
//        }

    }

    private void low() {
        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        RingBuffer<Integer[]> ringBuffer = RingBuffer.createSingleProducer(eventFactory, 8, waitStrategy);
        Sequence customerOne = new Sequence();
        Sequence customerTwo = new Sequence();
        ringBuffer.addGatingSequences(customerOne, customerTwo);
        customerOne.set(2);
        customerTwo.set(5);
        ringBuffer.publish(7);
        System.out.println("getCursor is " + ringBuffer.getCursor());

        System.out.println("getMinimumGatingSequence is " + ringBuffer.getMinimumGatingSequence());
    }

    private void capacity() {

        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        RingBuffer<Integer[]> ringBuffer = RingBuffer.createSingleProducer(eventFactory, 8, waitStrategy);

        //创建两个消费者
        Sequence customer1 = new Sequence();
        Sequence customer2 = new Sequence();
        ringBuffer.addGatingSequences(customer1, customer2);
        System.out.println(ringBuffer);


        //生产者申请序号
//        for (int i = 0; i < 50; i++) {
//            long sequence = ringBuffer.next();//RingBuffer尺寸为8，所以第8次申请时将阻塞
//            System.out.println("sequence is " + sequence + ", Cursor is " + ringBuffer.getCursor());
//        }

        //获取可用空间
//        for (int i = 0; i < 50; i++) {
//            long sequence = ringBuffer.next();
//            System.out.println("sequence is " + sequence + ", Cursor is " + ringBuffer.getCursor());
//            System.out.println("remainingCapacity is " + ringBuffer.remainingCapacity());
//        }


        //检查剩余空间
//        for (int i = 0; i < 50; i++) {
//            long sequence = ringBuffer.next();
//            System.out.println("sequence is " + sequence + ", Cursor is " + ringBuffer.getCursor());
//            System.out.println("hasAvailableCapacity is " + ringBuffer.hasAvailableCapacity(1));
//        }

    }

    private void cursor() {
        WaitStrategy waitStrategy = new BlockingWaitStrategy();
        EventFactory eventFactory = new OneEventFactory();

        RingBuffer<Integer[]> ringBuffer = RingBuffer.create(ProducerType.SINGLE, eventFactory, 4, waitStrategy);


        ringBuffer.publish(0);
        System.out.println("getCursor is " + ringBuffer.getCursor());
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
