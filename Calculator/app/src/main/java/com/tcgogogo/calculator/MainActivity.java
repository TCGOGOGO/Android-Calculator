package com.tcgogogo.calculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import java.util.*;

public class MainActivity extends Activity {

    private static final int NUM_CON = 10;
    private static final int OPR_CON = 9;

    private static final Map<Character, Integer> inStack = new HashMap<>();
    private static final Map<Character, Integer> outStack = new HashMap<>();
    static {
        inStack.put('#', 0);    outStack.put('#', 0);
        inStack.put('(', 1);    outStack.put('(', 6);
        inStack.put('+', 3);    outStack.put('+', 2);
        inStack.put('-', 3);    outStack.put('-', 2);
        inStack.put('×', 5);    outStack.put('×', 4);
        inStack.put('÷', 5);    outStack.put('÷', 4);
        inStack.put(')', 6);    outStack.put(')', 1);
    }

    private Button[] numButton = new Button[NUM_CON];
    private Button[] oprButton = new Button[OPR_CON];
    private TextView resTextView = null;
    private StringBuffer currentEquation = null;
    private StringBuffer preEquation = null;

    private Stack<String> mOprStack = null;
    private Stack<String> mNumStack = null;

    private Context mContext;

    private boolean isFloat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        numButton[0] = (Button) findViewById(R.id.num0);
        numButton[1] = (Button) findViewById(R.id.num1);
        numButton[2] = (Button) findViewById(R.id.num2);
        numButton[3] = (Button) findViewById(R.id.num3);
        numButton[4] = (Button) findViewById(R.id.num4);
        numButton[5] = (Button) findViewById(R.id.num5);
        numButton[6] = (Button) findViewById(R.id.num6);
        numButton[7] = (Button) findViewById(R.id.num7);
        numButton[8] = (Button) findViewById(R.id.num8);
        numButton[9] = (Button) findViewById(R.id.num9);

        oprButton[0] = (Button) findViewById(R.id.clear);
        oprButton[1] = (Button) findViewById(R.id.left_bracket);
        oprButton[2] = (Button) findViewById(R.id.right_bracket);
        oprButton[3] = (Button) findViewById(R.id.divide);
        oprButton[4] = (Button) findViewById(R.id.multiply);
        oprButton[5] = (Button) findViewById(R.id.subtract);
        oprButton[6] = (Button) findViewById(R.id.add);
        oprButton[7] = (Button) findViewById(R.id.equal);
        oprButton[8] = (Button) findViewById(R.id.dot);

        resTextView = (TextView) findViewById(R.id.result);

        currentEquation = new StringBuffer("");

        mContext = this;

        mOprStack = new Stack<>();
        mNumStack = new Stack<>();

        for (int i = 0; i < NUM_CON; i ++) {
            numButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button button = (Button) view;
                    String inputNum = button.getText().toString();
                    currentEquation.append(inputNum);
                    resTextView.setText(currentEquation);
                }
            });
        }
        for (int i = 0; i < OPR_CON; i ++) {
            oprButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button button = (Button) view;
                    String inputOpr = button.getText().toString();
                    switch (inputOpr) {
                        case "C" :
                            currentEquation.setLength(0);
                            break;
                        case "=" :
                            preEquation = currentEquation;
                            currentEquation = calculate(currentEquation);
                            Log.w(" TC ", "currentEquation = " + currentEquation);
                            isFloat = false;
                            break;
                        default:
                            currentEquation.append(inputOpr);
                    }
                    resTextView.setText(currentEquation);
                }
            });
        }

        resTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setTitle("上次操作")
                        .setMessage(getResources().getString(R.string.equation) + " " + preEquation.toString())
                        .setPositiveButton("确定", null)
                        .show();
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean isNumber(Character c) {
        return c >= '0' && c <= '9';
    }

    public boolean isDot(Character c) {
        return c == '.';
    }

    public boolean canIn(Character out) {
        char in = mOprStack.peek().charAt(0);
        if (outStack.containsKey(out) && inStack.containsKey(in)) {
            return outStack.get(out) > inStack.get(in);
        }
        return false;
    }

    public void prepare() {
        while (!mOprStack.isEmpty()) {
            mOprStack.pop();
        }
        mOprStack.push("#");
        while (!mNumStack.isEmpty()) {
            mNumStack.pop();
        }
    }

    public float operate() {
        Log.w(" TC ", " mNumStack size = " + mNumStack.size());
        float ans = 0;
        float a = Float.parseFloat(mNumStack.pop());
        String opr = mOprStack.pop();
        float b = Float.parseFloat(mNumStack.pop());
        Log.w(" TC ", "b = " + b + " ch = " + opr + "  a = " + a);
        switch (opr) {
            case "+":
                ans = b + a;
                break;
            case "-":
                ans = b - a;
                break;
            case "×":
                ans = b * a;
                break;
            case "÷":
                if (a == 0) {
                    new AlertDialog.Builder(mContext)
                            .setTitle("警告")
                            .setMessage(R.string.divide0warning)
                            .setPositiveButton("确定", null)
                            .show();
                    return 0;
                }
                ans = b / a;
                isFloat = (b % a) != 0;
                break;
        }
        return ans;
    }

    public StringBuffer calculate(StringBuffer equation) {
        Log.w(" TC ", " e = " + equation);
        prepare();
        float ans = 0;
        String str = equation.toString();
        int idx = 0, len = str.length();
        String curNum = "";
        while (idx < len) {
            char ch = str.charAt(idx);
            while (idx < len && (isNumber(ch) || isDot(ch))) {
                if (isDot(ch)) {
                    isFloat = true;
                }
                curNum += ch;
                idx ++;
                if (idx < len) {
                    ch = str.charAt(idx);
                }
            }
            if (curNum.length() != 0) {
                mNumStack.push(curNum);
                Log.w(" TC ", " cur = " + curNum);
                curNum = "";
                continue;
            }
            Log.w(" TC ", " ch = " + ch);
            if (canIn(ch)) {
                mOprStack.push(ch + "");
            } else if (ch == ')'){
                while (mOprStack.peek().charAt(0) != '(') {
                    ans = operate();
                    mNumStack.push(ans + "");
                }
                mOprStack.pop();
            } else {
                while(!canIn(ch) && mNumStack.size() > 1) {
                    ans = operate();
                    mNumStack.push(ans + "");
                }
                mOprStack.push(ch + "");
            }
            idx ++;
        }
        while(mNumStack.size() > 1) {
            ans = operate();
        }
        if (isFloat) {
            return new StringBuffer("" + ans);
        } else {
            return new StringBuffer("" + new Integer(new Float(ans).intValue()));
        }
    }
}
