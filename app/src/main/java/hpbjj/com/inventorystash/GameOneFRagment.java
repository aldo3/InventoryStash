package hpbjj.com.inventorystash;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameOneFRagment extends Fragment {


    public GameOneFRagment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GameView view = new GameView(getActivity().getApplicationContext());
        return view;
    }


    public class GameView extends SurfaceView {
        private Bitmap bmp;
        private SurfaceHolder holder;
        private GameLoopThread gameLoopThread;
        private List<Sprite> sprites = new ArrayList<Sprite>();
        private long lastClick;

        public GameView(Context context) {
            super(context);
            gameLoopThread = new GameLoopThread(this);
            holder = getHolder();
            holder.addCallback(new Callback() {

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    createSprites();
                    gameLoopThread.setRunning(true);
                    gameLoopThread.start();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format,
                                           int width, int height) {
                }
            });

        }

        private void createSprites() {
            sprites.add(createSprite(R.drawable.tank2_blue_01_64));
//            sprites.add(createSprite(R.drawable.bad2));
//            sprites.add(createSprite(R.drawable.bad3));
//            sprites.add(createSprite(R.drawable.bad4));
//            sprites.add(createSprite(R.drawable.bad5));
//            sprites.add(createSprite(R.drawable.bad6));
//            sprites.add(createSprite(R.drawable.good1));
//            sprites.add(createSprite(R.drawable.good2));
//            sprites.add(createSprite(R.drawable.good3));
//            sprites.add(createSprite(R.drawable.good4));
//            sprites.add(createSprite(R.drawable.good5));
//            sprites.add(createSprite(R.drawable.good6));
        }

        private Sprite createSprite(int resouce) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), resouce);
            return new Sprite(this, bmp);
        }

        protected  void Draw(Canvas canvas)
        {
            canvas.drawColor(Color.BLACK);
            for (Sprite sprite : sprites) {
                sprite.Draw(canvas);
            }

        }



        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (System.currentTimeMillis() - lastClick > 500) {
                lastClick = System.currentTimeMillis();
                synchronized (getHolder()) {
                    for (int i = sprites.size() - 1; i >= 0; i--) {
                        Sprite sprite = sprites.get(i);
                        if (sprite.isCollition(event.getX(), event.getY())) {
                            sprites.remove(sprite);
                            break;
                        }
                    }
                }
            }
            return true;
        }
    }

    public class GameLoopThread extends Thread {
        static final long FPS = 10;
        private GameView view;
        private boolean running = false;

        public GameLoopThread(GameView view) {
            this.view = view;
        }

        public void setRunning(boolean run) {
            running = run;
        }

        @Override
        public void run() {
            long ticksPS = 1000 / FPS;
            long startTime;
            long sleepTime;
            while (running) {
                Canvas c = null;
                startTime = System.currentTimeMillis();
                try {
                    c = view.getHolder().lockCanvas();
                    synchronized (view.getHolder()) {
                        view.Draw(c);
                    }
                } finally {
                    if (c != null) {
                        view.getHolder().unlockCanvasAndPost(c);
                    }
                }
                sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
                try {
                    if (sleepTime > 0)
                        sleep(sleepTime);
                    else
                        sleep(10);
                } catch (Exception e) {}
            }
        }
    }

    public class Sprite {
        // direction = 0 up, 1 left, 2 down, 3 right,
        // animation = 3 back, 1 left, 0 front, 2 right
        int[] DIRECTION_TO_ANIMATION_MAP = { 3, 1, 0, 2 };
        private static final int BMP_ROWS = 4;
        private static final int BMP_COLUMNS = 3;
        private static final int MAX_SPEED = 5;
        private GameView gameView;
        private Bitmap bmp;
        private int x = 0;
        private int y = 0;
        private int xSpeed;
        private int ySpeed;
        private int currentFrame = 0;
        private int width;
        private int height;

        public Sprite(GameView gameView, Bitmap bmp) {
            this.width = bmp.getWidth() / BMP_COLUMNS;
            this.height = bmp.getHeight() / BMP_ROWS;
            this.gameView = gameView;
            this.bmp = bmp;

            Random rnd = new Random();
            x = rnd.nextInt(gameView.getWidth() - width);
            y = rnd.nextInt(gameView.getHeight() - height);
            xSpeed = rnd.nextInt(MAX_SPEED * 2) - MAX_SPEED;
            ySpeed = rnd.nextInt(MAX_SPEED * 2) - MAX_SPEED;
        }

        private void update() {
            if (x >= gameView.getWidth() - width - xSpeed || x + xSpeed <= 0) {
                xSpeed = -xSpeed;
            }
            x = x + xSpeed;
            if (y >= gameView.getHeight() - height - ySpeed || y + ySpeed <= 0) {
                ySpeed = -ySpeed;
            }
            y = y + ySpeed;
            currentFrame = ++currentFrame % BMP_COLUMNS;
        }

        public void Draw(Canvas canvas) {
            update();
            int srcX = currentFrame * width;
            int srcY = getAnimationRow() * height;
            Rect src = new Rect(srcX, srcY, srcX + width, srcY + height);
            Rect dst = new Rect(x, y, x + width, y + height);
            canvas.drawBitmap(bmp, src, dst, null);
        }

        private int getAnimationRow() {
            double dirDouble = (Math.atan2(xSpeed, ySpeed) / (Math.PI / 2) + 2);
            int direction = (int) Math.round(dirDouble) % BMP_ROWS;
            return DIRECTION_TO_ANIMATION_MAP[direction];
        }

        public boolean isCollition(float x2, float y2) {
            return x2 > x && x2 < x + width && y2 > y && y2 < y + height;
        }
    }


}
