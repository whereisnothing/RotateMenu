package com.chenxu.rotatemenu;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.FloatEvaluator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;


import java.util.ArrayList;
import java.util.List;

public class RotateMenu extends FrameLayout implements CustomImageViewListener {

	private Context context;
	private AbsoluteLayout absoluteLayout;
	private List<Integer> imageIdList;
	private List<CustomImageView> imageViewList;
	private float width, height;
	private List<AbsoluteLayout.LayoutParams> layoutParamsList;
	private static final float ITEM_WIDTH = 100f, ITEM_HEIGHT = 100f;
	private float radius;
	private RotateMenuListener listener;
	private int currentSelectIndex;
	private int gap;
    private BitmapFactory.Options options;

	public RotateMenu(Context context, List<Integer> imageIdList,
                      RotateMenuListener listener) {
		super(context);
		this.context = context;
		this.imageIdList = imageIdList;
		this.listener = listener;
        imageViewList = new ArrayList<CustomImageView>();
        layoutParamsList = new ArrayList<AbsoluteLayout.LayoutParams>();
        options=new BitmapFactory.Options();
        options.inSampleSize=8;
    }

	public RotateMenu(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		this.context = context;
	}

	@SuppressWarnings("deprecation")
	public void init() {
        removeAllViews();
        imageViewList.clear();
		for (int i = 0; i < imageIdList.size(); i++) {
			int id = imageIdList.get(i);
			CustomImageView customImageView = new CustomImageView(context, i,
					this);
			Bitmap circlebiBitmap = getCircleImage(BitmapFactory.decodeResource(getResources(), id, options));
			customImageView.setImageBitmap(circlebiBitmap);
			imageViewList.add(customImageView);
		}
        layoutParamsList.clear();
		absoluteLayout = new AbsoluteLayout(context);
        FrameLayout.LayoutParams absoluteLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        absoluteLayout.setLayoutParams(absoluteLayoutParams);
		for (int i = 0; i < imageViewList.size(); i++) {
			CustomImageView customImageView = imageViewList.get(i);
			int displayIndex = customImageView.displayIndex;
			float x = (float) (Math.cos(Math.toRadians(90 + displayIndex
					* (360 / (imageIdList.size()))))
					* radius + width / 2)
					- ITEM_WIDTH / 2;
			float y = (float) (Math.sin(Math.toRadians(90 + displayIndex
					* (360 / (imageIdList.size()))))
					* radius + height / 2)
					- ITEM_HEIGHT / 2;
			AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
					(int) ITEM_WIDTH, (int) ITEM_HEIGHT, (int) x, (int) y);
			customImageView.setLayoutParams(params);
			layoutParamsList.add(params);
			absoluteLayout.addView(customImageView);
		}

		addView(absoluteLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        Log.i("chenxu", "init begin");
        for (int i = 0; i < layoutParamsList.size(); i++) {
            dumpUltimateIndexAndLayoutParams(i);
        }
        Log.i("chenxu", "init end");
    }

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
//		this.width = getMeasuredWidth();
//		this.height = getMeasuredHeight();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
        if (w!=0&&h!=0) {
            this.width = w;
            this.height = h;

            radius = Math.min(width, height) * 3 / 10;
            init();
        }
    }

	public List<Integer> getImageIdList() {
		return imageIdList;
	}

	public void setImageIdList(List<Integer> imageIdList) {
		this.imageIdList = imageIdList;
	}

	@Override
	public void customImageViewDidClick(CustomImageView civ) {
		gap = (layoutParamsList.size() - (civ.index - currentSelectIndex))
				% layoutParamsList.size();
		if (gap == 0) {
			if (listener != null) {
				listener.rotateMenuDidClick(civ.index);
			}
		} else {
			for (int i = 0; i < imageViewList.size(); i++) {
				CustomImageView customImageView = imageViewList.get(i);
				customImageView.startAnimation();
			}
			currentSelectIndex = civ.index;
		}
	}

	public static interface RotateMenuListener {
		public void rotateMenuDidClick(int position);
	}


	public class AngleEvaluator extends FloatEvaluator {
		@Override
		public Float evaluate(float fraction, Number startValue, Number endValue) {
			float start = startValue.floatValue();
			float end = endValue.floatValue();
			if (start < end) {
				return startValue.floatValue() + fraction
						* ((endValue.floatValue()) - startValue.floatValue());

			} else {
				return startValue.floatValue()
						+ fraction
						* ((endValue.floatValue() + 360) - startValue
								.floatValue()) % 360;

			}
		}
	}

	public static class AngleAndPosition {
		private float angle;
		private float x, y;

		public float getAngle() {
			return angle;
		}

		public void setAngle(float angle) {
			this.angle = angle;
		}

		public float getX() {
			return x;
		}

		public void setX(float x) {
			this.x = x;
		}

		public float getY() {
			return y;
		}

		public void setY(float y) {
			this.y = y;
		}

	}

	public Bitmap getCircleImage(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int diameter = Math.min(width, height);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, (width - diameter) / 2, (height - diameter) / 2, paint);
		return result;
	}

	public class CustomImageView extends ImageView implements OnClickListener {
		private int index, displayIndex;
		private CustomImageViewListener listener;

		public CustomImageView(Context context, int index,
				CustomImageViewListener listener) {
			super(context);
			this.index = index;
			this.displayIndex = index;
			this.listener = listener;
			setOnClickListener(this);
		}

		public void startAnimation() {
			int ultimateIndex = (displayIndex + gap) % imageViewList.size();
			float gapAngle = 360.0f / (imageViewList.size());
			float startAngle = 90 + displayIndex * gapAngle;
			float endAngle = (90 + ultimateIndex * gapAngle);
			ValueAnimator anim = ValueAnimator.ofObject(new AngleEvaluator(),
					startAngle, endAngle);
			anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float tempAngle = (Float) animation.getAnimatedValue();
					float x = (float) (Math.cos(Math.toRadians(tempAngle))
							* radius + width / 2)
							- ITEM_WIDTH / 2;
					float y = (float) (Math.sin(Math.toRadians(tempAngle))
							* radius + height / 2)
							- ITEM_HEIGHT / 2;
					AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
							(int) ITEM_WIDTH, (int) ITEM_HEIGHT, (int) x,
							(int) y);
					setLayoutParams(params);
				}
			});
			anim.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					// TODO Auto-generated method stub
					int ultimateIndex = (displayIndex + gap)
							% imageViewList.size();
					AbsoluteLayout.LayoutParams lp = layoutParamsList
							.get(ultimateIndex);
					setLayoutParams(lp);
                    dumpUltimateIndexAndLayoutParams(ultimateIndex);
					displayIndex = ultimateIndex;
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub

				}
			});
			anim.setDuration(1000);
			anim.start();
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public int getDisplayIndex() {
			return displayIndex;
		}

		public void setDisplayIndex(int displayIndex) {
			this.displayIndex = displayIndex;
		}

		public CustomImageViewListener getListener() {
			return listener;
		}

		public void setListener(CustomImageViewListener listener) {
			this.listener = listener;
		}

		@Override
		public void onClick(View v) {
			if (listener != null) {
				listener.customImageViewDidClick(this);
			}
		}

	}

    private void dumpUltimateIndexAndLayoutParams(int ultimateIndex){
        AbsoluteLayout.LayoutParams lp = layoutParamsList
                .get(ultimateIndex);
        Log.i("chenxu","ultimateIndex:"+ultimateIndex+" "+lp.x+" "+lp.y+" "+lp.width+" "+lp.height);
    }
	public RotateMenuListener getListener() {
		return listener;
	}

	public void setListener(RotateMenuListener listener) {
		this.listener = listener;
	}
}