package fy.socket.JavaWebsocket.util;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import fy.socket.JavaWebsocket.util.logger.LoggerUtil;


public class SendMsgQueue {

	private Logger logger = LoggerUtil.getLogger(this.getClass().getName());
	/**
	 * 发送队列
	 */
	private Queue<ByteBuffer> msgQ ;
	/**
	 * 发送队列最大长度
	 */
	private int maxSize;
	/**
	 * 发送队列锁
	 */
	private ReentrantLock lock;


	/**
	 * 插入，提取操作锁
	 */
	private Condition lines;
	private Condition space;
	
	// 是否接受新的消息到待发送消息队列
	private boolean PendingStatus;
	
	
	public SendMsgQueue(int maxSize){
		this.maxSize = maxSize;
		msgQ = new LinkedList<ByteBuffer>();
		lock = new ReentrantLock();
		lines = lock.newCondition();
		space = lock.newCondition();
		PendingStatus = true;
	}
	
	/**
	 * 将待发送数据放入待发送队列
	 * @param msg
	 */
	public void insert(ByteBuffer msg) {
		lock.lock();
		try {
			while (msgQ.size() == maxSize) {
				space.await();
			}
			msgQ.offer(msg);
			logger.log(Level.INFO, Thread.currentThread()
					.getName() + " Inserted mes num: "+ msgQ.size());
			lines.signalAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 从待发送队列中，取出消息，准备发送
	 * @return
	 */
	public ByteBuffer get() {
		ByteBuffer line=null;
		lock.lock();		
		try {
			while ((msgQ.size() == 0) &&(hasPendingStatus())) {
				lines.await();
			}
			
			if (hasPendingStatus()) {
				line = msgQ.poll();
				logger.log(Level.INFO, Thread.currentThread().getName()+" 发送队列剩余待发送消息数目: "+msgQ.size());
				space.signalAll();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return line;
	}
	
	

	/**
	 * 如果连接关闭 ，不再接受新的消息到待发送队列
	 * 先将待发送队列中的消息发送完
	 * @param PendingStatus
	 */
	public void setPendingStatus(boolean PendingStatus) {
		this.PendingStatus = PendingStatus;
	}


	/**
	 * 设置是否再接受发送消息，进入队列。
	 * @return
	 */
	public boolean hasPendingStatus() {
		return PendingStatus || msgQ.size() > 0;
	}
}
