package com.amir.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class TicTacToeActivity extends Activity{
    
	 private TicTacToeView view;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tictactoe);
        
        TicTacToe oldTTT = (TicTacToe)getLastNonConfigurationInstance();
        if(oldTTT != null)
         this.view = new TicTacToeView(this, null, oldTTT);
        else
         this.view = new TicTacToeView(this , null);

    }

    @Override
    public Object onRetainNonConfigurationInstance(){
    	return this.view.getInternalState();
    }
	
}