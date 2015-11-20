package com.tulc.optimization;

import java.io.IOException;
import java.util.Vector;

import com.tulc.math.Matrix;
import com.tulc.math.MatrixUtil;

/* 
 * Class that implements GradientDescent
 * test more
 */
public class GradientDescent {
    protected Vector<Double> theeta;
    protected Vector<Double> y;
    protected Matrix X;
    protected Vector<Double> loss;
    protected Integer numOfRows;
    protected Integer numOfFeatures;
    protected Double mse;
    protected boolean checkNumOfIter;
    protected boolean checkMseGain;
    protected GradientDescentOptions gdOptions;
    
    /*
     * For use by derived classes
     */
    public GradientDescent() {
    }
    
    /**
     * Initializes gradient descent algorithm with input parameters
     * 
     * @param iniTheeta initial value of theeta
     * @param x is the matrix that is to be optimized
     * @param y is the response variable vector
     * @param numOfIter is the number of iterations gradient descent is run
     * @param mseGain stop condition if the difference between mean squared error (mse) of current and previous 
     * iterations is less than this value
     * @throws IOException 
     */
    public GradientDescent(Double iniTheeta, Matrix dataSet, Vector<Double> respVec, GradientDescentOptions gdo) 
            throws IOException {
        theeta = new Vector<Double>(X.numOfCols());
        for (int i = 0; i < theeta.capacity(); i++) {
            theeta.add(iniTheeta);
        }
        X = dataSet;
        y = respVec;
        gdOptions = gdo;
        numOfRows = X.numOfRows();
        numOfFeatures = X.numOfCols();
        mse = (double) 0;
        
        checkNumOfIter = (gdOptions.getNumOfIter() == -1) ? false : true;
        checkMseGain = (gdOptions.getMseGain() == -1) ? false : true;
        
        optimize();
     }

    /**
     * Run the gradient descent algorithm till threshold conditions are satisfied
     * @throws IOException
     */
    public Vector<Double> optimize() throws IOException {
        if (! (checkNumOfIter || checkMseGain))
            throw new IOException("Gradient descent must have a defined exit condition. " +
                                  "Provide number of iterations or mse gain");
        Double prevMse = (double) 0;
        Vector<Double> gradient = new Vector<Double>(numOfFeatures);
        int i = 0;
        do {
            computeLossAndMse();
            for (int m=0; m<numOfFeatures; m++) {
                gradient.insertElementAt((gdOptions.getLearningRate()/numOfRows) * MatrixUtil.dotProduct(X.getColumn(m), loss), m);
            }
            if (i > 0) {
                if ((mse - prevMse) < gdOptions.getMseGain()) {
                    return getTheeta();
                }
            }
            theeta = MatrixUtil.subtract(theeta, gradient);
            prevMse = mse;
        } while (
                (checkNumOfIter ? (i++ < gdOptions.getNumOfIter()) : true) && 
                (checkMseGain ? ((mse - prevMse) > gdOptions.getMseGain()) : true)
            );
        return getTheeta();
    }
    
    /**
     * Compute loss and mean square error
     * loss is a vector of differences between actual and predicted values for each record in training set
     * @throws IOException
     */
    private void computeLossAndMse() throws IOException {
        Double yhat = new Double(0);
        loss = new Vector<Double>(numOfRows);
        Vector<Double> row = new Vector<Double>();
        Double currY = new Double(0);
        if (numOfFeatures != theeta.size())
            throw new IOException("Invalid dimensions for the vector theeta. Number of features: " 
                    + numOfFeatures + ", size of theeta: " + theeta.size());
        for (int n=0; n<numOfRows; n++) {
            row = X.getRow(n);
            yhat = MatrixUtil.dotProduct(row, theeta);
            currY = y.get(n);
            loss.insertElementAt((yhat - currY), n);
            mse += (yhat - currY) * (yhat - currY);
        }
        mse = mse/numOfRows;
    }
    
    /**
     * Returns the vector of theeta
     * @return
     */
    public Vector<Double> getTheeta() {
        return (Vector<Double>) theeta;
    }
}
