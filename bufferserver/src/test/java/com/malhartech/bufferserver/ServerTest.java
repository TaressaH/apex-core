/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.bufferserver;

import com.malhartech.bufferserver.Buffer.Data.DataType;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Chetan Narsude <chetan@malhar-inc.com>
 */
public class ServerTest
{
  private static final Logger logger = LoggerFactory.getLogger(ServerTest.class);
  static Server instance;
  static BufferServerPublisher bsp;
  static BufferServerSubscriber bss;
  static BufferServerController bsc;
  static int spinCount = 500;

  @BeforeClass
  public static void setupServerAndClients() throws Exception
  {
    instance = new Server(0);
    SocketAddress result = instance.run();
    assert (result instanceof InetSocketAddress);
    String host = ((InetSocketAddress)result).getHostName();
    int port = ((InetSocketAddress)result).getPort();

    bsp = new BufferServerPublisher("MyPublisher");
    bsp.setup(host, port);

    bss = new BufferServerSubscriber("MyPublisher", null);
    bss.setup(host, port);

    bsc = new BufferServerController("MyPublisher");
    bsc.setup(host, port);
  }

  @AfterClass
  public static void teardownServerAndClients()
  {
    bsc.teardown();
    bss.teardown();
    bsp.teardown();
    instance.shutdown();
  }

  @Test
  public void testNoPublishNoSubscribe() throws InterruptedException
  {
    bsp.activate();
    bss.activate();

    Thread.sleep(100);

    bss.deactivate();
    bsp.deactivate();

    assertEquals(0, bss.tupleCount.get());
  }

  @Test(dependsOnMethods = {"testNoPublishNoSubscribe"})
  @SuppressWarnings("SleepWhileInLoop")
  public void test1Window() throws InterruptedException
  {
    bsp.activate();
    bss.activate();

    ResetTuple rt = new ResetTuple();
    rt.id = 0x7afebabe000000faL;
    bsp.publishMessage(rt);

    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bss.tupleCount.get() > 0) {
        break;
      }
    }
    Thread.sleep(10);

    bss.deactivate();
    bsp.deactivate();

    assertEquals(bss.tupleCount.get(), 1);
    assertEquals(rt.getType(), bss.firstPayload.getType());
  }

  @Test(dependsOnMethods = {"test1Window"})
  @SuppressWarnings("SleepWhileInLoop")
  public void testLateSubscriber() throws InterruptedException
  {
    bss.activate();

    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bss.tupleCount.get() > 0) {
        logger.debug("{}", bss.tupleCount);
        break;
      }
    }
    Thread.sleep(10);

    bss.deactivate();

    assertEquals(bss.tupleCount.get(), 1);
    assertEquals(bss.firstPayload.getType(), DataType.RESET_WINDOW);
  }

  @Test(dependsOnMethods = {"testLateSubscriber"})
  @SuppressWarnings("SleepWhileInLoop")
  public void testATonOfData() throws InterruptedException
  {
    bss.activate();

    bsp.baseWindow = 0x7afebabe;
    bsp.windowId = 0;
    bsp.activate();

    BeginTuple bt = new BeginTuple();
    bt.id = 0x7afebabe00000000L;
    bsp.publishMessage(bt);

    for (int i = 0; i < 100; i++) {
      bsp.publishMessage(new byte[i]);
    }

    EndTuple et = new EndTuple();
    et.id = bt.id;
    bsp.publishMessage(et);

    BeginTuple bt1 = new BeginTuple();
    bt1.id = bt.id + 1;
    bsp.publishMessage(bt1);

    for (int i = 0; i < 100; i++) {
      bsp.publishMessage(new byte[i]);
    }

    EndTuple et1 = new EndTuple();
    et1.id = bt1.id;
    bsp.publishMessage(et1);

    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bss.tupleCount.get() > 204) {
        break;
      }
    }
    Thread.sleep(10); // wait some more to receive more tuples if possible

    bsp.deactivate();
    bss.deactivate();

    assertEquals(bss.tupleCount.get(), 205);
  }

  @Test(dependsOnMethods = {"testATonOfData"})
  @SuppressWarnings("SleepWhileInLoop")
  public void testPurgeNonExistent() throws InterruptedException
  {
    bsc.windowId = 0;
    bsc.activate();
    bsc.purge();
    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bsc.data != null) {
        break;
      }
    }
    bsc.deactivate();

    assertNotNull(bsc.data);

    bss.activate();
    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bss.tupleCount.get() > 204) {
        break;
      }
    }
    Thread.sleep(10);
    bss.deactivate();
    assertEquals(bss.tupleCount.get(), 205);
  }

  @Test(dependsOnMethods = {"testPurgeNonExistent"})
  @SuppressWarnings("SleepWhileInLoop")
  public void testPurgeSome() throws InterruptedException
  {
    bsc.windowId = 0x7afebabe00000000L;
    bsc.activate();
    bsc.purge();
    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bsc.data != null) {
        break;
      }
    }
    bsc.deactivate();

    assertNotNull(bsc.data);

    bss.activate();
    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bss.tupleCount.get() > 102) {
        break;
      }
    }
    bss.deactivate();
    assertEquals(bss.tupleCount.get(), 103);
  }

  @Test(dependsOnMethods = {"testPurgeSome"})
  @SuppressWarnings("SleepWhileInLoop")
  public void testPurgeAll() throws InterruptedException
  {
    bsc.windowId = 0x7afebabe00000001L;
    bsc.activate();
    bsc.purge();
    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bsc.data != null) {
        break;
      }
    }
    bsc.deactivate();

    assertNotNull(bsc.data);

    bss.activate();
    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bss.tupleCount.get() > 0) {
        break;
      }
    }
    Thread.sleep(10);
    bss.deactivate();
    assertEquals(bss.tupleCount.get(), 1);
  }

  @Test(dependsOnMethods = {"testPurgeAll"})
  public void testRepublish() throws InterruptedException
  {
    testATonOfData();
  }

  @Test(dependsOnMethods = {"testRepublish"})
  @SuppressWarnings("SleepWhileInLoop")
  public void testReblishLowerWindow() throws InterruptedException
  {
    logger.debug("test republish");
    bsp.baseWindow = 10;
    bsp.windowId = 0;
    bsp.activate();
    Thread.sleep(10);

    BeginTuple bt0 = new BeginTuple();
    bt0.id = 0L;
    bsp.publishMessage(bt0);

    for (int i = 0; i < 2; i++) {
      bsp.publishMessage(new byte[]{(byte)i});
    }

    EndTuple et0 = new EndTuple();
    et0.id = bt0.id;
    bsp.publishMessage(et0);

    BeginTuple bt1 = new BeginTuple();
    bt1.id = bt0.id + 1;
    bsp.publishMessage(bt1);

    for (int i = 0; i < 2; i++) {
      bsp.publishMessage(new byte[]{(byte)i});
    }

    EndTuple et1 = new EndTuple();
    et1.id = bt1.id;
    bsp.publishMessage(et1);

    bsp.deactivate();

    bss.activate();
    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bss.tupleCount.get() > 7) {
        break;
      }
    }
    Thread.sleep(10); // wait some more to receive more tuples if possible

    bsp.deactivate();
    bss.deactivate();

    assertEquals(bss.tupleCount.get(), 8);
  }

  @Test(dependsOnMethods = {"testReblishLowerWindow"})
  @SuppressWarnings("SleepWhileInLoop")
  public void testReset() throws InterruptedException
  {
    bsc.windowId = 0x7afebabe00000001L;
    bsc.activate();
    bsc.reset();
    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bsc.data != null) {
        break;
      }
    }
    bsc.deactivate();

    assertNotNull(bsc.data);

    bss.activate();
    for (int i = 0; i < spinCount; i++) {
      Thread.sleep(10);
      if (bss.tupleCount.get() > 0) {
        break;
      }
    }

    bss.deactivate();

    assertEquals(bss.tupleCount.get(), 0);
  }

  @Test(dependsOnMethods = {"testReset"})
  public void test1WindowAgain() throws InterruptedException
  {
    test1Window();
  }

  class ResetTuple implements Tuple
  {
    long id;

    public DataType getType()
    {
      return DataType.RESET_WINDOW;
    }

    public long getWindowId()
    {
      return id;
    }

    public int getIntervalMillis()
    {
      return (int)id;
    }

    public int getBaseSeconds()
    {
      return (int)(id >> 32);
    }
  }

  class BeginTuple extends ResetTuple
  {
    @Override
    public DataType getType()
    {
      return DataType.BEGIN_WINDOW;
    }
  }

  class EndTuple extends ResetTuple
  {
    @Override
    public DataType getType()
    {
      return DataType.END_WINDOW;
    }
  }
}
