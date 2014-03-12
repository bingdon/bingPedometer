package com.bing.mystep;



import java.math.BigDecimal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
/**
 * 参照githup上Pedometer编写
 * @author lyl
 *
 */
public class MainActivity extends Activity {

	private Button mButton;
	/**
	 * 计步
	 */
	private TextView mTextView;
	/**
	 * 卡路里
	 */
	private TextView calorTextView;
	/**
	 * 速度
	 */
	private TextView speedTextView;
	/**
	 * 距离
	 */
	private TextView distanceTextView;
	
	private Thread thread;
	
	private Double distance = 0.0;// 路程：米
	private Double calories = 0.0;// 热量：卡路里
	private Double velocity = 0.0;// 速度：米每秒

	private int step_length = 70;
	private int weight = 54;
	private int total_step = 0;
	private long timer = 0;// 运动时间
	
	private boolean THREADFLAG=false;
	
	private static long startTimer = 0;// 开始时间

	private static long tempTime = 0;
	
	private Intent serviceIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mButton=(Button)findViewById(R.id.button1);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				serviceIntent=new Intent();
				serviceIntent.setClass(MainActivity.this, StepService.class);
				startService(serviceIntent);
				
				startTimer = System.currentTimeMillis();
				tempTime = timer;
				
				
				initThread();
				
				
//				MainActivity.this.getWindow().setBackgroundDrawableResource(R.drawable.dibubg);
				
				
				
			}
		});
		
		mTextView=(TextView)findViewById(R.id.step_txt);
		calorTextView=(TextView)findViewById(R.id.carlulitxt);
		speedTextView=(TextView)findViewById(R.id.speed);
		distanceTextView=(TextView)findViewById(R.id.distance);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		THREADFLAG=true;
		stopService(serviceIntent);
		
	}




	Handler handler = new Handler() {// Handler对象用于更新当前步数

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			countDistance();

			if (timer != 0 && distance != 0.0) {

				// 体重、距离
				// 跑步热量（kcal）＝体重（kg）×距离（公里）×1.036
				calories = weight * distance * 0.001;

				velocity = distance * 1000 / timer;
			} else {
				calories = 0.0;
				velocity = 0.0;
			}

			countStep();

			mTextView.setText(total_step + "步");// 显示当前步数
			calorTextView.setText(changep2(calories)+"卡路里");
			speedTextView.setText(changep2(velocity)+"m/s");
			distanceTextView.setText(changep2(distance)+"m");
			
		}

	};

	
	/**
	 * 计算行走的步数
	 */
	private void countDistance() {
		if (StepDetector.CURRENT_SETP % 2 == 0) {
			distance = (StepDetector.CURRENT_SETP / 2) * 3 * step_length * 0.01;
		} else {
			distance = ((StepDetector.CURRENT_SETP / 2) * 3 + 1) * step_length
					* 0.01;
		}
	}
	
	/**
	 * 实际的步数
	 */
	private void countStep() {
		if (StepDetector.CURRENT_SETP % 2 == 0) {
			total_step = StepDetector.CURRENT_SETP / 2 * 3;
		} else {
			total_step = StepDetector.CURRENT_SETP / 2 * 3 + 1;
		}
	}
	
	private void initThread(){
		if (thread == null) {

			thread = new Thread() {// 子线程用于监听当前步数的变化

				@Override
				public void run() {
					// TODO Auto-generated method stub
					super.run();
					int temp = 0;
					while (!THREADFLAG) {
						try {
							Thread.sleep(300);
							Log.i("====", "运行"+StepService.FLAG);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (StepService.FLAG) {
							Message msg = new Message();
							if (temp != StepDetector.CURRENT_SETP) {
								temp = StepDetector.CURRENT_SETP;
							}
							
							
							if (startTimer != System.currentTimeMillis()) {
								timer = tempTime + System.currentTimeMillis()
										- startTimer;
							}
							
							Log.i("====", "步伐:"+temp);
							handler.sendMessage(msg);// 通知主线程
						}
					}
				}
			};
			thread.start();
		}
	}
	
	public double changep2(double i){
		BigDecimal b = new BigDecimal(i);
		 int saveBitNum = 2;
		 double c = b.setScale(saveBitNum , BigDecimal.ROUND_HALF_UP).doubleValue();
		 return c;
	}
}
