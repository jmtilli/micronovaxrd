package fi.iki.jmtilli.javacomplex;
/**
   A common interface to complex numbers.
 */
public interface ComplexNumber {
  /**
     Returns the real part of the complex number.

     @return The real part
   */
  double getReal();
  /**
     Returns the real part of the complex number.

     @return The imaginary part
   */
  double getImag();
  /**
     Returns the absolute value of the complex number.
    
     @return Double.NaN if isNaN()
     @return Double.POSITIVE_INFINITY if isInfinite()
     @return x&ge;0 absolute value otherwise
   */
  double abs();
  /**
     Returns the argument of the complex number.
    
     @return Double.NaN if isNaN()
     @return -pi&le;x&le;pi argument otherwise
   */
  double arg();
  /**
     Checks whether the complex number is NaN.
    
     @return true if either the real or imaginary part is NaN
     @return false otherwise
   */
  boolean isNaN();
  /**
     Checks whether the complex number is infinite.

     @return true if the real or imaginary part is infinite and !isNaN()
     @return false otherwise
   */
  boolean isInfinite();
  /**
     Returns a String representation of the complex number.

     @return re if purely real
     @return im + "i" if purely imaginary
     @return re " + " + im + "i" if imaginary part positive
     @return re " - " + (-im) + "i" if imaginary part negative
   */
  String toString();
};
