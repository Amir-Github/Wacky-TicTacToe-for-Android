package com.amir.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class TicTacToe {

	public final static int EMRTY    = 64;
    public final static int X_PLAYER = 2;
    public final static int O_PLAYER = 5;
    
    public final static int X_WON_STATUS = 7;
    public final static int O_WON_STATUS = 11;
    public final static int TIE_STATUS = 13;
    public final static int ONGOING_STATUS = 17;
    
    public final static int MOVEMENT = 8;
    public final static int STATUS   = 16;
    
    private int x_Player_win_counter = 0;
    private int o_Player_win_counter = 0;
    
    private Handler handler;
    private int[] gameMatrix;
	private boolean x_turn;
	private int moveCounter;
	private int status = ONGOING_STATUS;
    private boolean autoReset = false;
	
	///////
	public TicTacToe(Handler handler,boolean autoReset){
	  this.handler = handler;	
	  this.autoReset = autoReset;
	  reset();
	}
	
	//////
	public synchronized int getContent(int index){
		return this.gameMatrix[index];
	}
	
	/////
    public boolean move_Request(int index){
    	
      if(this.gameMatrix[index] != EMRTY || !this.x_turn || this.status != ONGOING_STATUS)
    	  return false;
      else
    	  this.gameMatrix[index] = X_PLAYER;
      
      sendMove(index);
      x_turn = false;
      this.moveCounter++;
      calculateNextMove();
      return true;
    }

    //////
    public synchronized int[] getAvailableIndexes(){
    	int[] indexes = new int[9-this.moveCounter];
    	int j = 0;
    	 for(int i=0;i<9;i++)
    		if(this.gameMatrix[i] == EMRTY)
    			indexes[j++] = i;
    	 
     return indexes;	 
    }
    
    
    public int getYourScore(){
    	return x_Player_win_counter;
    }
    
    public int getOpponentScore(){
    	return o_Player_win_counter;
    }
    
    /////
    protected void calculateNextMove(){
    	
    	if(gameEnded()){
    		if(this.autoReset)
    			reset();
    	 return;
    	}	
    	
    	int index = getNextMove();
    	this.gameMatrix[index] = O_PLAYER;
    	this.x_turn = true;
    	
    	Message msg = new Message();
    	Bundle bundle = new Bundle();
    	bundle.putInt("value", index);
    	msg.what    = MOVEMENT;
    	msg.setData(bundle);
    	this.handler.sendMessage(msg);
    	
    	this.moveCounter++;   	
    	if(gameEnded() && this.autoReset)
    		reset();
    }
    
    /*
     * Can be overridden to send the next movement over network or Bluetooth or whatever....
     */
    protected void sendMove(int moveIndex){
    }
     
    /*
     * Can be overridden to get the next movement over network or Bluetooth or whatever....
     */
    protected int getNextMove(){
    	int[] availableMoves = getAvailableIndexes();
    	int moveIndex = (int)Math.floor(Math.random()*availableMoves.length);
     return availableMoves[moveIndex];	
    }
        
    /////
    private boolean gameEnded(){
      this.status = checkStatus();
  	  if(status != ONGOING_STATUS){
  		  
  		  if(status == X_WON_STATUS)
  			  this.x_Player_win_counter++;
  		  else if(status == O_WON_STATUS)
  			  this.o_Player_win_counter++;
  		  
  		  Message msg = new Message();
  		  Bundle bundle = new Bundle();
  		  bundle.putInt("value", status);
  		  msg.what = STATUS;
  		  msg.setData(bundle);
  		  handler.sendMessage(msg);
  		  
  		return true; 
  	  }	
    
  	  return false;
    }
    
    //////
    private int checkStatus(){
       
    	final int winIndicator_O = O_PLAYER*3;
    	final int winIndicator_X = X_PLAYER*3;
    	
    	for(int i=0;i<3;i++){
    	  int j = i*3;	
    	  int rowSum    = this.gameMatrix[j]+this.gameMatrix[j+1]+this.gameMatrix[j+2];
    	  int columnSum = this.gameMatrix[i]+this.gameMatrix[3+i]+this.gameMatrix[6+i];
    	   if(rowSum == winIndicator_O || columnSum == winIndicator_O)
    		  return O_WON_STATUS;
    	   else if(rowSum == winIndicator_X || columnSum == winIndicator_X)
    		   return X_WON_STATUS;	
    	}
    	
    	int firstDiameter  = this.gameMatrix[0]+this.gameMatrix[4]+this.gameMatrix[8];
    	int secondDiameter = this.gameMatrix[2]+this.gameMatrix[4]+this.gameMatrix[6];
    	
    	if(firstDiameter == winIndicator_O || secondDiameter == winIndicator_O)
    		return O_WON_STATUS;
    	else if(firstDiameter == winIndicator_X || secondDiameter == winIndicator_X)
    		return X_WON_STATUS;
    	
        if(this.moveCounter < 9)
        	return ONGOING_STATUS;
    	
       return TIE_STATUS; 
    }
    
    /////
    public synchronized void  reset(){
    	this.gameMatrix = new int[9];
    	for(int i=0;i<9;i++)
    		this.gameMatrix[i] = EMRTY;
    	
    	this.moveCounter = 0;
    	this.x_turn      = true;
    	this.status = ONGOING_STATUS;
    	
    }

    
}