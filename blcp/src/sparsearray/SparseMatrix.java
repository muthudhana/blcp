/*
 * SparseMatrix.java
 *
 * Created on March 5, 2006, 2:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sparsearray;

import java.util.*;

/**
 *
 * @author mike
 */
public class SparseMatrix {
    
    private ArrayList<SparseVector> matrix = null;
    private int minNumRows = -1;
    
    /** Creates a new instance of SparseMatrix */
    public SparseMatrix() {
        this.matrix = new ArrayList<SparseVector>();
    }
    
    public SparseMatrix(SparseMatrix source){
        this.matrix = new ArrayList<SparseVector>();
        
        this.addNColumns(source.numRows());
        
        for(int i = 0; i < source.numColumns(); ++i){
            int[] idx = ((SparseVector)(source.getColumn(i))).getIndicies();
            for(int j = 0; j < idx.length; ++j){
                try {
                    this.set(idx[j], i, source.get(idx[j], i));
                } catch (Exception e){
                    System.out.println("Should never happen!!!!!!!! FATAL!");
                    System.exit(-1);
                }
            }
        }    
        this.setMinNumRows(source.getMinNumRows());
    }
    
    private int numRowsReal(){
        Iterator<SparseVector> itr = this.matrix.iterator();
        int size = 0;
        while(itr.hasNext()){
            SparseVector sv = itr.next();
            if(sv.size() > size){
                size = sv.size();
            }
        }
        return(size);
    }
        
    public int numRows(){
        if(this.minNumRows > 0 && this.minNumRows > this.numRowsReal()){
            return(this.minNumRows);
        } else {
            return(this.numRowsReal());
        }
    }
    
    public void setMinNumRows(int num){
        this.minNumRows = num;
    }
    
    public int getMinNumRows(){
        return(this.minNumRows);
    }
    
    public SparseVector add(SparseVector operand) throws Exception {
        SparseMatrix tmp = new SparseMatrix();
        tmp.addColumn(operand);
        return(this.add(tmp).getColumn(0));
    }
    
    public SparseVector subtract(SparseVector operand) throws Exception {
        SparseMatrix tmp = new SparseMatrix();
        tmp.addColumn(operand);
        return(this.subtract(tmp).getColumn(0));
    }
    
    public SparseMatrix add(SparseMatrix operand) throws Exception {
        if(this.numColumns() != operand.numColumns() ||
                this.numRows() != operand.numRows()){
            throw new SparseVectorDimensionMismatch("Matrix addition requries matricies of identical dimensions.");
        }
        SparseMatrix sum = new SparseMatrix();
        for(int i = 0; i < this.numColumns(); ++i){
            SparseVector tmp = new SparseVector(this.getColumn(i));
            tmp.add(operand.getColumn(i));
            sum.addColumn(tmp);
        }
        return(sum);
    }
    
    public SparseMatrix subtract(SparseMatrix operand) throws Exception{
        if(this.numColumns() != operand.numColumns() ||
                this.numRows() != operand.numRows()){
            throw new SparseVectorDimensionMismatch("Matrix addition requries matricies of identical dimensions.");
        }
        SparseMatrix sum = new SparseMatrix();
        for(int i = 0; i < this.numColumns(); ++i){
            SparseVector tmp = new SparseVector(this.getColumn(i));
            tmp.subtract(operand.getColumn(i));
            sum.addColumn(tmp);
        }
        return(sum);
    }
    
    public void addColumn(){
        this.matrix.add(new SparseVector());
    }
    
    public void addNColumns(int n){
        for(int i = 0; i < n; ++i){
            this.addColumn();
        }
    }
    
    public void addColumn(SparseVector sv){
        this.matrix.add(sv);
    }
    
    public int numColumns(){
        return(this.matrix.size());
    }
    
    public SparseVector getColumn(int i){
        return(this.matrix.get(i));
    }
    
    public double getSparsity(){
        Iterator<SparseVector> itr = this.matrix.iterator();
        int size = 0;
        while(itr.hasNext()){
            SparseVector sv = itr.next();
            size += sv.size();
        }
        return(size / (this.numColumns() * this.numRows()));
    }
    
    public Double get(int rowOff, int colOff) throws Exception{
        if(colOff >= this.numColumns()){
            throw new SparseVectorDimensionMismatch("Requested cell outside of bounded area!");
        }
        
        Double val = ((SparseVector)(this.matrix.get(colOff))).get(rowOff);
        return(val);
    }
    
    public Double getTranspose(int rowOff, int colOff) throws Exception{
        return(this.get(colOff, rowOff));
    }
    
    
    public void set(int rowOff, int colOff, Double value) throws Exception{
        if(colOff >= this.numColumns()){
            throw new SparseVectorDimensionMismatch("Column requested not present in the matrix!");
        }
        
        ((SparseVector)(this.matrix.get(colOff))).set(rowOff, value);
        return;
    }
    
    
    /**
     * Returns the transpose of this matrix as a new SparseMatrix object.
     * This function is intensive, so only call as needed and cache results
     * if possible!
     */
    public SparseMatrix transpose(){
        SparseMatrix sm = new SparseMatrix();
        sm.addNColumns(this.numRows());
        
        for(int i = 0; i < this.numColumns(); ++i){
            int[] idx = this.matrix.get(i).getIndicies();
            for(int j = 0; j < idx.length; ++j){
                try {
                    sm.set(i, idx[j], this.get(idx[j], i));
                } catch (Exception e){
                    System.out.println("Should never happen!!!!!!!! FATAL!");
                    System.exit(-1);
                }
            }
        }
        
        return(sm);
    }
    
    /**
     * Let this matrix be X and the parameter matrix be Y. This method
     * calculates X * Y and returns the result as a new matrix!
     */
    public SparseMatrix multiply(SparseMatrix operand) throws Exception {
        if(operand.numRows() != this.numColumns()){
            throw new SparseVectorDimensionMismatch("Matricies are of the wrong dimension to be multiplied!");
        }
        
        SparseMatrix product = new SparseMatrix();
        int numColsInOperand = operand.numColumns();
        
        for(int i = 0; i < numColsInOperand; ++i){
            System.out.println("Multiplying Column " + i + " out of " + numColsInOperand);
            SparseVector sv = this.multiply(operand.getColumn(i));
            product.addColumn(sv);
        }

        return(product);
    }
    
    /**
     * Let the matrix this object represents be X and the operand vector
     * be Y.  This function calculates X * Y and returns the result.
     */
    public SparseVector multiply(SparseVector operand) throws Exception{
    /* We need SPEEEEED so we are going to calculate this product
     * one column at a time!  Since that is how we are storing the matrix
     * it will let us only multiply non-sparse elements. And do so extremely
     * quickly! -- or so I hope!
     */
        if(this.numColumns() < operand.size()){
            throw new SparseVectorDimensionMismatch("SparseVector provided to SparseMatrix.multiply is of incorrect dimension.");
        }

        int numColumns = this.numColumns();
        
        SparseVector output = new SparseVector();
        
        for(int i = 0; i < numColumns; ++i){
            SparseVector curCol = this.matrix.get(i);
            int[] idx = curCol.getIndicies();
            for(int j = 0; j < idx.length; ++j){
                double val = output.get(idx[j]);
                val += curCol.get(idx[j]) * operand.get(i);
                output.set(idx[j], val);
            }
        }
        
        return(output);
    }
    
    public SparseMatrix scalarMultiply(double scalar){
        SparseMatrix copy = new SparseMatrix(this);
        for(int i = 0; i < copy.numColumns(); ++i){
            SparseVector s = copy.getColumn(i);
            s.scalarMultiply(scalar);
        }
        return(copy);
    }
    
    public SparseMatrix scalarDivide(double scalar){
        return(this.scalarMultiply(1D/scalar));
    }
    
    public SparseVector getMeanColumnVector(){
        SparseVector mean = new SparseVector();
        for(int i = 0; i < this.numColumns(); ++i){
            SparseVector col = this.getColumn(i);
            int[] idx = col.getIndicies();
            for(int j = 0; j < idx.length; ++j){
                double val = mean.get(idx[j]);
                val += col.get(idx[j]);
                mean.set(idx[j], val);
            }
        }
        mean.scalarDivide(this.numColumns());
        return(mean);
    }
    
    
    public SparseVector getDominantEigenvector() throws Exception{
        
        SparseVector eigenVector = new SparseVector();
        eigenVector.set(this.numRows() - 1, 1D);        
        
        SparseMatrix aT = this.transpose();
        SparseMatrix M = new SparseMatrix();
        M.addColumn(this.getMeanColumnVector());
        SparseMatrix MpartA = new SparseMatrix(M);
        double multiplier = (2 * this.numColumns()) - this.numRows();
        SparseMatrix MT = M.transpose();
        
        int runs = 7;
        for(int j = runs; j > 0; --j){
            SparseVector part1Result = aT.multiply(eigenVector);
            part1Result = this.multiply(part1Result);
            SparseVector part2Result = M.scalarMultiply(multiplier).getColumn(0);
            part2Result.scalarMultiply(M.getColumn(0).dotProduct(eigenVector));
            part1Result.subtract(part2Result);
            eigenVector = part1Result;
            eigenVector.normalize();
        }
        
        return(eigenVector);
        
    }
}
