package com.example.digitalfactory;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;

public class MainActivity extends AppCompatActivity {
    // IDs of all the numeric buttons
    private int[] numericButtons = {R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9};
    // IDs of all the operator buttons
    private int[] operatorButtons = {R.id.buttonAdd, R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide, R.id.buttonSin, R.id.buttonCos, R.id.buttonTan, R.id.buttonFactorial, R.id.buttonPower, R.id.buttonModulo};
    // TextView used to display the output
    private TextView txtInput, txtOutput;
    // Represent whether the lastly pressed key is numeric or not
    private boolean lastNumeric;
    // Represent that current state is in error or not
    private boolean stateError;
    // If true, do not allow to add another DOT
    private boolean lastDot;
    // Factorial
    Operator factorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find the TextViews
        this.txtInput = (TextView) findViewById(R.id.txtInput);
        this.txtOutput = (TextView) findViewById(R.id.txtOutput);
        // Find and set OnClickListener to numeric buttons
        setNumericOnClickListener();
        // Find and set OnClickListener to operator buttons, equal button and decimal point button
        setOperatorOnClickListener();
    }

    /**
     * Find and set OnClickListener to numeric buttons.
     */
    private void setNumericOnClickListener() {
        // Create a common OnClickListener
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just append/set the text of clicked button
                Button button = (Button) v;
                if (stateError) {
                    // If current state is Error, replace the error message
                    txtInput.setText(button.getText());
                    stateError = false;
                } else {
                    // If not, already there is a valid expression so append to it
                    txtInput.append(button.getText());
                }
                // Set the flag
                lastNumeric = true;
            }
        };
        // Assign the listener to all the numeric buttons
        for (int id : numericButtons) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    /**
     * Find and set OnClickListener to operator buttons, equal button and decimal point button.
     */
    private void setOperatorOnClickListener() {
        // Create a common OnClickListener for operators
        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the current state is Error do not append the operator
                // If the last input is number only, append the operator
                if (lastNumeric && !stateError) {
                    Button button = (Button) v;
                    txtInput.append(button.getText());
                    lastNumeric = false;
                    lastDot = false;    // Reset the DOT flag
                }
            }
        };
        // Assign the listener to all the operator buttons
        for (int id : operatorButtons) {
            findViewById(id).setOnClickListener(listener);
        }
        // Decimal point
        findViewById(R.id.buttonDot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastNumeric && !stateError && !lastDot) {
                    txtInput.append(".");
                    lastNumeric = false;
                    lastDot = true;
                }
                // if the last input was an operator, make it a decimal value (0.X)
                else if(!lastNumeric){
                    txtInput.append("0.");
                }
            }
        });
        // Clear button
        findViewById(R.id.buttonClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the screens
                txtInput.setText("");
                txtOutput.setText("");
                // Reset all the states and flags
                lastNumeric = false;
                stateError = false;
                lastDot = false;
            }
        });
        // Equal button
        findViewById(R.id.buttonEqual).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEqual();
            }
        });
        // Erase button
        findViewById(R.id.buttonErase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onErase();
            }
        });
        // Declare factorial (!)
        factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {
            @Override public double apply(double... args) {
                final long arg = (long) args[0];
                if ((double) arg != args[0]) {
                    throw new IllegalArgumentException("Operand for factorial has to be an integer");
                }

                if (arg < 0) {
                    throw new IllegalArgumentException("The operand of the factorial can not be less than zero");
                }

                double result = 1;
                for (int i = 1; i <= arg; i++) {
                    result *= i;
                }
                return result;
            }
        };
    }

    /**
     * Logic to calculate the solution.
     */
    private void onEqual() {
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        // Exclude factorial
        if (lastNumeric && !stateError && !txtInput.toString().contains("!")) {
            // Read the expression
            String txt = txtInput.getText().toString();
            // Create an Expression (A class from exp4j library)
            Expression expression = new ExpressionBuilder(txt).build();
            try {
                // Calculate the result and display
                double result = expression.evaluate();
                txtOutput.setText(Double.toString(result));
                lastDot = true; // Result contains a dot
            } catch (ArithmeticException ex) {
                // Display an error message
                txtOutput.setText("Error");
                stateError = true;
                lastNumeric = false;
            }
        }
        else if (!lastNumeric && txtInput.getText().toString().contains("!")){
            // Read the expression
            String txt = txtInput.getText().toString();
            try {
                // Calculate the result and display
                double result = new ExpressionBuilder(txt)
                        .operator(factorial)
                        .build()
                        .evaluate();
                txtOutput.setText(Double.toString(result));
                lastDot = true; // Result contains a dot
            } catch (ArithmeticException ex) {
                // Display an error message
                txtOutput.setText("Error");
                stateError = true;
                lastNumeric = false;
            }
        }
    }

    private void onErase(){
        // If txtInput is not empty
        if(txtInput.getText().length() > 0) {
            String txtInputString = txtInput.getText().toString();
            // Delete the last String
            txtInputString = txtInputString.substring(0, txtInputString.length()-1);
            txtInput.setText(txtInputString);
        }
    }
}