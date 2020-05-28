package com.example.calculator;

import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

enum ButtonType {EQUALS, OPERATOR, NUMBER_PAD}

public class MainActivity extends AppCompatActivity {
    private final String mKEY_CURRENT_CALC = "CURRENT_CALC";
    private CalculatorModel mCalculator;
    private TextView mScreen;
    private StringBuilder mOperand1, mOperand2, mCurrentOperand;
    private Snackbar mSnackBar;
    private ButtonType mLastPush;
    private boolean mUseLeadingZero;
    private int mScreenMaxChars;

    private final String mKeyPrefsName = "PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultValuesForPreferences();
        restoreAppSettingsFromPreferences();

        setContentView(R.layout.activity_main);
        setupToolbar();
        setupFAB();
        initializeFields();
        initializeSnackbar();
        setDefaultOperator();
        setupScreen();

    }

    private void setDefaultValuesForPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);
    }

    private void restoreAppSettingsFromPreferences() {
        String leadingZeroKey= "SHOW LEADING ZERO";

        SharedPreferences preferences = getSharedPreferences(mKeyPrefsName, MODE_PRIVATE);

        mUseLeadingZero = preferences.getBoolean(leadingZeroKey, true);
    }

    private void initializeSnackbar() {
        mSnackBar = Snackbar.make(findViewById(R.id.activity_main), R.string.show_full_results, Snackbar.LENGTH_INDEFINITE);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupFAB() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleEquals();
            }
        });
    }

    private void initializeFields() {
        mCalculator = new CalculatorModel();
        mOperand1 = new StringBuilder("0");
        mOperand2 = new StringBuilder("0");

        mLastPush = ButtonType.NUMBER_PAD;
        mScreenMaxChars = getResources().getInteger(R.integer.screen_max_characters);
    }

    private void setupScreen(){
        mScreen = findViewById(R.id.text_view);
        clearAll();
    }

    private void setDefaultOperator() {
        mCalculator.setOperator(Operator.ADD);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onPrepareOptionsMenu (Menu menu)
    {
        menu.findItem (R.id.action_leading_zero).setChecked (mUseLeadingZero);
        return super.onPrepareOptionsMenu (menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        dismissSnackBarIfShown ();

        int id = item.getItemId();

        switch (id){
            case R.id.menu_calculate:
                handleEquals();
                return true;
            case R.id.menu_clear_all:
                clearAll();
                return true;
            case R.id.action_leading_zero:
                toggleMenuItem (item);
                mUseLeadingZero = item.isChecked ();
                return true;
            case R.id.menu_info:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleMenuItem (MenuItem item)
    {
        item.setChecked (!item.isChecked ());
    }

    private void dismissSnackBarIfShown ()
    {
        if (mSnackBar.isShown ())
            mSnackBar.dismiss ();
    }

    private void stripLeadingZero(){
        if(!mUseLeadingZero && mCurrentOperand.length() > 1)
            if(mCurrentOperand.charAt(0) == '0')
                mCurrentOperand.deleteCharAt(0);
            else if(mCurrentOperand.charAt(0) == '-' && mCurrentOperand.charAt(1) == '0' )
                mCurrentOperand.deleteCharAt(1);
    }

    public void showAbout ()
    {
        Utils.showInfoDialog (MainActivity.this, R.string.about_dialog_title,
                R.string.about_dialog_banner);
    }

    private void updateScreen(){
        mScreen.setText(mCurrentOperand);
    }

    private void setEmptyOperandToZero(){
        if (mCurrentOperand.toString().equals(""))
            clear();
    }

    private void setNegationOfNothingToZero(){
        if (mCurrentOperand.toString().equals("-"))
            clear();
    }

    private void clearAll(){
        mOperand1.replace(0, mOperand1.length(), "0");
        mOperand2.replace(0, mOperand2.length(), "0");
        mCurrentOperand = mOperand1;
        mLastPush = ButtonType.NUMBER_PAD;

        updateScreen();
        setDefaultOperator();
    }

    private void clear(){
        mCurrentOperand.replace(0, mCurrentOperand.length(), "0");

        mLastPush = ButtonType.NUMBER_PAD;
        updateScreen();
    }

    public void handleBackspaceButton(View view) {
        dismissSnackBarIfShown();

        if (mLastPush != ButtonType.OPERATOR && mCurrentOperand.length() > 0) {
            mCurrentOperand.deleteCharAt(mCurrentOperand.length() - 1);
            setEmptyOperandToZero();
            setNegationOfNothingToZero();
            stripLeadingZero();

            updateScreen();
        }
    }

    private void handleEquals(){
        calculate();
        mLastPush = ButtonType.EQUALS;
    }

    private void calculate(){
        mCalculator.setOperand1(mOperand1.toString());
        mCalculator.setOperand2(mOperand2.toString());
        try {
            mOperand1.replace(0, mOperand1.length(), mCalculator.compute());
        } catch (IllegalArgumentException e){
            Snackbar.make(findViewById(R.id.fab) ,e.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        }
        mCurrentOperand = mOperand1;
        stripLeadingZero();
        updateScreen();
    }

    @SuppressWarnings("unused")
    public void cmdKeypad(View view) {
        handleKeyPadPress ((Button) view);
    }

    private void handleKeyPadPress (Button currentButton)
    {
        dismissSnackBarIfShown ();

        setupOperandsBasedOnLastPush();

        if(mCurrentOperand.length() < mScreenMaxChars - 1
            || (mCurrentOperand.charAt(0) == '-' && mCurrentOperand.length() < mScreenMaxChars)) {
            String currentButtonTextString = currentButton.getText().toString();
            if (mCurrentOperand.toString().equals("0")) {
                mCurrentOperand.replace(0, mCurrentOperand.length(), currentButtonTextString);
            } else {
                mCurrentOperand.append(currentButtonTextString);
            }
            mLastPush = ButtonType.NUMBER_PAD;
            updateScreen();
        }

    }

    private void setupOperandsBasedOnLastPush(){
        if (mLastPush == ButtonType.EQUALS) {
            clearAll();
        } else if (mLastPush == ButtonType.OPERATOR) {
            mOperand2.replace(0, mOperand2.length(), "0");
            mCurrentOperand = mOperand2;
        }

    }

    public void handleDecimalButton(View view) {
        dismissSnackBarIfShown ();

        setupOperandsBasedOnLastPush();

        if(mCurrentOperand.toString().contains("."))
            Snackbar.make(view, "Decimal can only contain one decimal point", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        else if(mCurrentOperand.length() < mScreenMaxChars - 1
                || ((mCurrentOperand.charAt(0) == '-' ) && mCurrentOperand.length() < mScreenMaxChars)){
            if (mUseLeadingZero)
                mCurrentOperand.append(".");
            else
                handleKeyPadPress ((Button) view);
        }

        mLastPush = ButtonType.NUMBER_PAD;
        updateScreen();
    }

    public void clearAll(View view) {
        clearAll();
    }

    public void clear(View view) {
        clear();
    }

    public void negate(View view) {
        dismissSnackBarIfShown();

        if(mLastPush != ButtonType.OPERATOR) {
            if (mCurrentOperand.charAt(0) == '-')
                mCurrentOperand.deleteCharAt(0);
            else {
                if (! mCurrentOperand.toString().equals("0"))
                    mCurrentOperand.insert(0, "-");
            }

            mLastPush = ButtonType.NUMBER_PAD;
            updateScreen();
        }
    }

    @SuppressWarnings("unused")
    public void handleOperatorKey(View view){
        if(mCurrentOperand == mOperand2) { //address comparison is deliberate
            calculate();
        }

        int id = view.getId ();

        switch (id) {
            case R.id.square:
                mCalculator.setOperator(Operator.SQUARE);
                calculate();
                break;
            case R.id.add:
                mCalculator.setOperator(Operator.ADD);
                break;
            case R.id.subtract:
                mCalculator.setOperator(Operator.SUBTRACT);
                break;
            case R.id.multiply:
                mCalculator.setOperator(Operator.MULTIPLY);
                break;
            case R.id.divide:
                mCalculator.setOperator(Operator.DIVIDE);
        }

        mLastPush = id == R.id.square ? ButtonType.EQUALS : ButtonType.OPERATOR;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String stringToSave = CalculatorModel.getJSONStringFromObject(mCalculator);
        outState.putString(mKEY_CURRENT_CALC, stringToSave);
        outState.putString("OPERAND1", mOperand1.toString());
        outState.putString("OPERAND2", mOperand2.toString());
        boolean mCurrentOperandIsOperand1 = mCurrentOperand == mOperand1; //address comparison is deliberate
        outState.putBoolean("CURRENT_OPERAND", mCurrentOperandIsOperand1);
        outState.putString("LAST_PUSH", String.valueOf(mLastPush));
    }

    @Override protected void onRestoreInstanceState (@NonNull Bundle savedInstanceState){
        super.onRestoreInstanceState (savedInstanceState);
        String savedStringOfObject = savedInstanceState.getString (mKEY_CURRENT_CALC);
        mCalculator = CalculatorModel.getObjectFromJSONString(savedStringOfObject);
        mOperand1.replace(0, mOperand1.length(), savedInstanceState.getString("OPERAND1"));
        mOperand2.replace(0, mOperand2.length(), savedInstanceState.getString("OPERAND2"));
        boolean mCurrentOperandIsOperand1 = savedInstanceState.getBoolean("CURRENT_OPERAND");
        mLastPush = ButtonType.valueOf(savedInstanceState.getString("LAST_PUSH"));

        if(mCurrentOperandIsOperand1)
            mCurrentOperand = mOperand1;
        else
            mCurrentOperand = mOperand2;

        updateScreen();
    }

    @Override
    protected void onStop(){
        saveToSharedPref();
        super.onStop();
    }

    private void saveToSharedPref() {
        SharedPreferences preferences = getSharedPreferences(mKeyPrefsName, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();

        String leadingZeroKey= "SHOW LEADING ZERO";
        editor.putBoolean(leadingZeroKey, mUseLeadingZero);
        editor.apply();
    }
}