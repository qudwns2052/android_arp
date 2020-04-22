package com.example.test;

import android.widget.Button;

public class ListViewBtnItem {

    private String textStr;
    private Button button1;
    private Button button2;

    public void setButton1(Button button) {
        button1 = button;
    }
    public Button getButton1() {
        return this.button1;
    }

    public void setButton2(Button button) {
        button2 = button;
    }
    public Button getButton2() {
        return this.button2;
    }

    public void setText(String text) {
        textStr = text ;
    }

    public String getText() {
        return this.textStr ;
    }

    public void switchButton(){
        if(button1.isEnabled())
        {
            button1.setEnabled(false);
            button2.setEnabled(true);
        }
        else
        {
            button1.setEnabled(true);
            button2.setEnabled(false);

        }
    }

}
