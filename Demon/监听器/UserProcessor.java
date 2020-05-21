package com.listener;

import java.util.Calendar;

public class UserProcessor implements Runnable {

	private UserListener userListener;

	private UserEvent userEvent;

	public void registerUserListener(UserListener userListener, String userName) {
		this.userListener = userListener;
		this.userEvent = new UserEvent(userName);
	}

	public void eat() {

	}

	public void work() {
		userListener.work(userEvent);
	}

	public void talk() {
		userListener.talk(userEvent);
	}

	@Override
	public void run() {
		int count = 0;
		while (count < 3) {
			talk();
			count++;
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
	}

	public static void main(String[] args) {
		UserProcessor userProcessor = new UserProcessor();
		userProcessor.registerUserListener(new UserListener() {
			int talkCount = 1;

			@Override
			public void work(UserEvent userEvent) {
				System.out.println(Calendar.getInstance().getTime() + "\n" + userEvent.getUser() + "开始工作");
			}

			@Override
			public void talk(UserEvent userEvent) {
				System.out.println(
						Calendar.getInstance().getTime() + "\n" + userEvent.getUser() + "说话" + talkCount + "次");
				talkCount++;
			}
		}, "小王");

		userProcessor.eat();
		userProcessor.talk();
		userProcessor.work();
		userProcessor.run();
	}
}
