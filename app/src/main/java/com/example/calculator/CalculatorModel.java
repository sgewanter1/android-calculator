package com.example.calculator;

import com.google.gson.Gson;

import java.math.BigDecimal;
import java.math.MathContext;

enum Operator {ADD, SUBTRACT, MULTIPLY, DIVIDE, SQUARE, SQUARE_ROOT}


public class CalculatorModel {
    //BigDecimal is used to avoid the computation errors resulting from float and double
    private BigDecimal operand1, operand2;
    private Operator operator;

    private MathContext mc = new MathContext(10);

    public CalculatorModel() {
        this("0", "0", Operator.ADD);
    }

    public CalculatorModel(String operand1, String operand2, Operator operator) {
        this.operand1 = new BigDecimal(operand1);
        this.operand2 = new BigDecimal(operand2);
        this.operator = operator;
    }

    public void setOperand1(String operand1) {
        this.operand1 = new BigDecimal(operand1);
    }

    public void setOperand2(String operand2) {
        this.operand2 = new BigDecimal(operand2);
    }

    public void negateOperand1(){
        operand1 = operand1.multiply(BigDecimal.valueOf(-1));
    }

    public void negateOperand2(){
        operand2 = operand2.multiply(BigDecimal.valueOf(-1));
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String compute() throws IllegalArgumentException{
        BigDecimal result;
        switch (operator) {
            case ADD:
                result = operand1.add(operand2);
                break;
            case SUBTRACT:
                result = operand1.subtract(operand2);
                break;
            case MULTIPLY:
                result = operand1.multiply(operand2);
                break;
            case DIVIDE:
                if (operand2.equals(BigDecimal.ZERO)) {
                    throw new IllegalArgumentException("Error: Divide by zero");
                } else {
                    result = operand1.divide(operand2, mc);
                }
                break;
            case SQUARE:
                result = operand1.pow(2);
                break;
            default:
                result = operand1;
        }
        return result.stripTrailingZeros().toPlainString();
    }

    public static CalculatorModel getObjectFromJSONString (String json){
        Gson gson = new Gson ();
        return gson.fromJson (json, CalculatorModel.class);
    }
    public static String getJSONStringFromObject (CalculatorModel object){
        Gson gson = new Gson ();
        return gson.toJson (object);
    }
}
