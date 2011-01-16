package com.amir.game;


import java.util.Timer;
import java.util.TimerTask;

import com.amir.game.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;
 
 
public class TicTacToeView extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {
	
		private MainThread _thread;
        private int[] Xs = new int[2];
        private int[] Ys   = new int[2];
        private boolean draw = false;
        
        private Bitmap background;
        private Bitmap X_Player;
        private Bitmap O_Player;
        
        private int gameTable_height;
        private int gameTable_sY;
        private int gameTable_width;
        private int scoreBox_Width;
        private int scoreBox_Height;
        private int scoreBox_sY;
        private Rect[] squares;
        
        private final static int TABLE_BORDER = 6;
        private final static int BOX_BORDER   = 4;
        
        private Paint tablePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint boxPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        private Paint textPaint    = new Paint(Paint.SUBPIXEL_TEXT_FLAG | 
        		                               Paint.ANTI_ALIAS_FLAG);
        
        private Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG | 
        		                               Paint.FILTER_BITMAP_FLAG);
        
        private TicTacToe tictactoe;
       
        
        public TicTacToeView(Context context, AttributeSet as) {
            super(context);
            getHolder().addCallback(this);
        }
        
        public TicTacToeView(Context context, AttributeSet as , TicTacToe oldState) {
            super(context);
            getHolder().addCallback(this);
            this.tictactoe = oldState;
        }
 
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        	
            _thread = new MainThread(getHolder());	
        	
           if(tictactoe == null)	
        	this.tictactoe = new TicTacToe(new TicTacToeHandler(),false);
           else
        	  _thread.firstTime = false;
           
        	Resources resources = getContext().getResources(); 
        	
            this.background = BitmapFactory.decodeResource(resources, R.drawable.background);
            this.X_Player   = BitmapFactory.decodeResource(resources, R.drawable.x_pic);
            this.O_Player   = BitmapFactory.decodeResource(resources, R.drawable.o_pic);
            
            
            Rect rect = holder.getSurfaceFrame();
            this.gameTable_height = rect.height()-56;
            this.gameTable_sY     = 0;
            this.gameTable_width  = rect.width();
            this.scoreBox_Height  = 50;
            this.scoreBox_Width   = rect.width();
            this.scoreBox_sY      = rect.height()-this.scoreBox_Height;
            this.squares = new Rect[9];
            
            final int square_Width  = (int)(gameTable_width-(TABLE_BORDER*4))/3;
            final int square_Height = (int)(gameTable_height-(TABLE_BORDER*4))/3; 
            
            for(int i=0;i<9;i++){
              int row    = i%3;
              int column = i/3;
              int left = ((row+1)*TABLE_BORDER)+(square_Width*row);
              int top  = ((column+1)*TABLE_BORDER)+(square_Height*column);
              this.squares[i] = new Rect(left,top,left+square_Width,top+square_Height);	
              
            }
            
            this.tablePaint.setStyle(Style.STROKE);

            this.tablePaint.setShader(new LinearGradient(0, 0, this.gameTable_height,
            		                                      this.gameTable_width, 
            		                                      0xFF000000, 
            		                                      0xFF343434, 
            		                                      TileMode.MIRROR));
            this.tablePaint.setStrokeWidth(TABLE_BORDER);
            this.tablePaint.setAlpha(0xCC);
            
            this.boxPaint.setStyle(Style.STROKE);
        	this.boxPaint.setStrokeWidth(BOX_BORDER);
        	this.boxPaint.setColor(0xFFA90000);
        	
        	this.textPaint.setColor(0xFF000000);
        	this.textPaint.setTextAlign(Align.CENTER);
        	this.textPaint.setTextSize(14);
        	this.textPaint.setTypeface(Typeface.createFromAsset(getContext().getResources().getAssets(),"HARLOWSI.TTF"));
        	
        	this.contentPaint.setAlpha(0xDF);
            
            _thread.setRunning(true);
            _thread.start();
            setOnTouchListener(this);
            
        }
 
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        	
            boolean retry = true;
            _thread.setRunning(false);
            while (retry) {
                try {
                    _thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // we will try it again and again...
                }
            }
        }
        
        
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
        }
             
        ///////
        public TicTacToe getInternalState(){
        	return this.tictactoe;
        }
    
        ////////
        public void doDraw(Canvas canvas) {

        	canvas.drawBitmap(this.background, 0, 0, null);
        	drawTable(canvas);
        	
        	if(this.draw && Xs[0] == Xs[1] && Ys[0] == Ys[1]){
                CheckContent();
          	}
        	drawContent(canvas);

        }
        
        /////////
        private void updateScores(Canvas canvas){

        	final int halfBorder = (int)BOX_BORDER/2;
        	final Paint paint = textPaint;
        	
        	Rect rect = new Rect(halfBorder,
        			             this.scoreBox_sY-halfBorder,
        			             this.scoreBox_Width-halfBorder,
        			             (this.scoreBox_sY-halfBorder)+this.scoreBox_Height);
        	RectF rectf = new RectF(rect);

        	
        	canvas.drawRoundRect(rectf, 15, 15, this.boxPaint);
        	
        	canvas.drawText("Your Score : "+this.tictactoe.getYourScore(), 65,this.scoreBox_sY+25,paint);
        	canvas.drawText("Computer's Score : "+this.tictactoe.getOpponentScore(), 15+(this.scoreBox_Width/3)*2,this.scoreBox_sY+25,paint);
        	
        }  
        
        
      private void drawTable(Canvas canvas){
    	 
    	  final Paint paint = this.tablePaint;
    	  final float border = TABLE_BORDER;
    	  
    	  final int cellHeight = (int)(this.gameTable_height-(2*border))/3;
    	  final int cellWidth  = (int)(this.gameTable_width-(2*border))/3;
    	  
    	  final float table_eX = this.gameTable_width-border;
    	  final float table_eY = this.gameTable_height-border;
    	 	    
    	  canvas.drawLine(this.gameTable_sY+border, cellHeight+border, table_eX, cellHeight+border, paint);
    	  canvas.drawLine(this.gameTable_sY+border, (cellHeight*2)+border, table_eX, (cellHeight*2)+border, paint);
    	  
    	  canvas.drawLine(cellWidth+border, border, cellWidth+border, table_eY, paint);
    	  canvas.drawLine((cellWidth*2)+border, border, (cellWidth*2)+border, table_eY, paint);
    	  
    	  
    	  paint.setPathEffect(new CornerPathEffect(20));
    	  canvas.drawRect(border/2, border/2, this.gameTable_width-(border/2), 
    			                              this.gameTable_height-(border/2), 
    			                              paint);
    	  
    	  paint.setPathEffect(null);
      }
      
      
      private void CheckContent(){
          
    	  for(int i=0;i<9;i++){
    		  if(this.squares[i].contains(Xs[1], Ys[1])){
    			  this.tictactoe.move_Request(i);
    			  
    		   this.draw = false;	  
               return;   
    		 }		 
    	  }
      }
      
      
      private void drawContent(Canvas canvas){
    	  
  
    	 for(int i=0;i<9;i++){
    	   int squareContent = this.tictactoe.getContent(i);	 
    	   if(squareContent == TicTacToe.X_PLAYER)
    		   canvas.drawBitmap(this.X_Player,null,this.squares[i], this.contentPaint); 
    	   else if(squareContent == TicTacToe.O_PLAYER)
    		   canvas.drawBitmap(this.O_Player,null,this.squares[i], this.contentPaint);   
    		 
    	 }  
      }
       
      
    class TicTacToeHandler extends Handler{
    	
    	@Override
    	public void handleMessage(Message msg){
    	  
    		switch(msg.what){
    		
    		case TicTacToe.MOVEMENT: 
    			                     break; 
    		case TicTacToe.STATUS:  checkStatus(msg.getData());
    		                        /// just giving some time to user to comprehend what's just happened ;)
    		                        Timer timer = new Timer();
    		                        timer.schedule(new TimerTask() {
										
										@Override
										public void run() {
											tictactoe.reset();
										}
									},2000); 
    		                        break; 
    		}
    	}
    	
    	private void checkStatus(Bundle data){
    		
    		final int status = data.getInt("value");
    		String msg = "";
    		 switch(status){
    		 
    		    case TicTacToe.X_WON_STATUS : msg = "Congratulation!! You won the game.";
    		                                  break;
    		    case TicTacToe.O_WON_STATUS : msg = "Oops!..You lost the game.";
    		                                  break;
    		    case TicTacToe.TIE_STATUS   : msg = "It Is A draw mate!!";
    		                                  break;
    		 
    		 }
    		
    		 Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    	}
    	
    }
 
    
    class MainThread extends Thread {
    	
        private SurfaceHolder surfaceHolder;
        private boolean runFlag = false;
        boolean firstTime = true;
 
        public MainThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }
 
        public void setRunning(boolean run) {
            this.runFlag = run;
        }
        
        @Override
        public void run() {
            Canvas c;
            
            while (this.runFlag) {
            	
            	if(firstTime){
                  	  drawLines();
                  	  firstTime = false;
                  	  continue;
                	}
            	
                c = null;
                try {
                	 	
                    c = this.surfaceHolder.lockCanvas(null);
                    synchronized (this.surfaceHolder) {                  	
                        doDraw(c);
                        updateScores(c);
                    }
                } finally {
                   
                    if (c != null) {
                        this.surfaceHolder.unlockCanvasAndPost(c);
                        
                    }
                }
            }
        }
        
        private void drawLines(){
        	
        	final Paint paint = tablePaint;
        	final float border = TABLE_BORDER;

        	final int cellHeight = (int)(gameTable_height-(2*border))/3;
        	final int cellWidth  = (int)(gameTable_width-(2*border))/3;

        	final float table_eX = gameTable_width-border;
        	final float table_eY = gameTable_height-border;

        	Line[] myLines = new Line[4];
        	myLines[0] = new Line(border, cellHeight+border, table_eX, cellHeight+border);
        	myLines[1] = new Line(border, (cellHeight*2)+border, table_eX, (cellHeight*2)+border);
        	myLines[2] = new Line(cellWidth+border, border, cellWidth+border, table_eY);
        	myLines[3] = new Line((cellWidth*2)+border, border, (cellWidth*2)+border, table_eY);

        	LineDrawer drawer = new LineDrawer(myLines, this.surfaceHolder, paint);
        	drawer.start();
        	try{ 
        		drawer.join();
        	}catch(InterruptedException exp){
        		////Nothing
        	}
        	
        }
        
    }


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
	   if(event.getAction() == MotionEvent.ACTION_DOWN){	
		 this.Xs[0] = (int)event.getX();
		 this.Ys[0] = (int)event.getY();
	   } 
	  else if(event.getAction() == MotionEvent.ACTION_UP){
		  this.Xs[1] = (int)event.getX();
		  this.Ys[1] = (int)event.getY();
		  
		  this.draw = true;
	  }
		
	 return true;
	}

  //////////////////////////////////////////////////////	
	class LineDrawer extends Thread {
		
		private Line[] lines;
		private SurfaceHolder holder;
		private Paint paint;
		private final static int DRAW_PER_LINE    = 5;
		
	    LineDrawer(Line[] l,SurfaceHolder h,Paint p){
	     this.lines = l;	
	     this.holder = h;
	     this.paint = p;
	    }
		
		public void run(){
		  final int length = lines.length;	
		  for(int i=0;i<length;i++)
			 drawLine(i); 
		}
		
		private void drawLine(int index){
			
			final Line line = this.lines[index];
			
			final float addingPortion_X = ((line.eX - line.sX) / DRAW_PER_LINE);
			final float addingPortion_Y = ((line.eY - line.sY) / DRAW_PER_LINE);			
			
			for(int i=0;i<DRAW_PER_LINE;i++){
				
				Canvas canvas = this.holder.lockCanvas(null);
				canvas.drawBitmap(background, 0, 0, null);
				 for(int j=0;j<index;j++)
					 canvas.drawLine(this.lines[j].sX,
							         this.lines[j].sY, 
							         this.lines[j].eX, 
							         this.lines[j].eY,
				                     this.paint); 
					 
				     canvas.drawLine(line.sX,
						             line.sY, 
						             line.sX+(addingPortion_X*(i+1)), 
						             line.sY+(addingPortion_Y*(i+1)),
						             this.paint);
				
				this.holder.unlockCanvasAndPost(canvas);     
			  
			}   
			
		}
		
	}
  /////////////////////////////////////////////////
	class Line {	
		
		float sX;
		float sY;
		float eX;
		float eY;	
		
		Line(float sx , float sy , float ex , float ey){
		  this.sX = sx;	
		  this.sY = sy;
		  this.eX = ex;
		  this.eY = ey;
		}
	}
  ////////////////////////////////////////////
}

