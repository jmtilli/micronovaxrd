package fi.iki.jmtilli.javacomplex;
import java.io.Serializable;

/**
   An immutable complex number.

   An instance of this class refers to a complex number the value of
   which cannot be changed.
 */
public class Complex implements ComplexNumber, Serializable {
  private static final long serialVersionUID = -1687575357354609741L;

  /**
     A complex number representing the imaginary unit
   */
  public static final Complex I = new Complex(+0.0, 1.0);
  /**
     A complex number representing one
   */
  public static final Complex ONE = new Complex(1.0, +0.0);
  /**
     A complex number representing zero
   */
  public static final Complex ZERO = new Complex(+0.0, +0.0);
  /**
     A complex number representing NaN (not a number)
   */
  public static final Complex NaN = new Complex(Double.NaN, Double.NaN);

  /**
     The real part.
     @serial
   */
  private final double re;
  /**
     The imaginary part.
     @serial
   */
  private final double im;

  /**
     Create a complex number that has the same value as the specified other
     complex number.

     @param num The other complex number
   */
  public Complex(ComplexNumber num)
  {
    this(num.getReal(), num.getImag());
  }
  /**
     Create a complex number with zero imaginary part.
    
     @param re The real part
   */
  public Complex(double re)
  {
    this(re, +0.0);
  }
  /**
     Create a complex number.
    
     @param re The real part
     @param im The imaginary part
   */
  public Complex(double re, double im)
  {
    this.re = re;
    this.im = im;
  }

  /**
     Create a complex number that has the same value as the specified other
     complex number. If a new Complex instance is not required, this
     method should be used instead of the constructor Complex(ComplexNumber),
     as this method results in better performance by caching frequently used
     values.

     @param num The other complex number
     @return The new complex number
   */
  public static Complex valueOf(ComplexNumber num)
  {
    return Complex.valueOf(num.getReal(), num.getImag());
  }
  /**
     Create a complex number with zero imaginary part. If a new Complex instance
     is not required, this method should be used instead of the constructor
     Complex(double), as this method results in better performance by caching
     frequently used values.
    
     @param re The real part
     @return The new complex number
   */
  public static Complex valueOf(double re)
  {
    return Complex.valueOf(re, +0.0);
  }
  private static final long POS_ZERO_LONG_BITS = Double.doubleToLongBits(+0.0);
  /**
     Create a complex number. If a new Complex instance is not required, this
     method should be used instead of the constructor Complex(double, double),
     as this method results in better performance by caching frequently used
     values.
    
     @param re The real part
     @param im The imaginary part
     @return The new complex number
   */
  public static Complex valueOf(double re, double im)
  {
    /*
    final long re_long_bits = Double.doubleToLongBits(re);
    if (re_long_bits == POS_ZERO_LONG_BITS)
    {
      final long im_long_bits = Double.doubleToLongBits(im);
      if (im_long_bits == POS_ZERO_LONG_BITS)
      {
        return Complex.ZERO;
      }
      if (im == 1.0)
      {
        return Complex.I;
      }
    }
    if (re == 1.0)
    {
      final long im_long_bits = Double.doubleToLongBits(im);
      if (im_long_bits == POS_ZERO_LONG_BITS)
      {
        return Complex.ONE;
      }
    }
    */
    return new Complex(re, im);
  }
  /**
     Create a complex number from polar coordinates.
    
     @param abs The absolute value
     @param argument The argument
     @return The new complex number
   */
  public static Complex newPolar(double abs, double argument)
  {
    return ComplexUtils.newPolar(abs, argument);
  }

  /**
     Returns the real part of the complex number.

     @return The real part
   */
  public double getReal()
  {
    return re;
  }
  /**
     Returns the imaginary part of the complex number.

     @return The imaginary part
   */
  public double getImag()
  {
    return im;
  }

  /**
     Add another complex number to this complex number
    
     @param c The other complex number
    
     @return the sum
   */
  public Complex add(ComplexNumber c)
  {
    return ComplexUtils.add(this, c);
  }
  /**
     Add a real number to this complex number
    
     @param d The real number
    
     @return the sum
   */
  public Complex add(double d)
  {
    return ComplexUtils.add(this, d);
  }
  /**
     Subtract another complex number from this complex number
    
     @param c The other complex number
    
     @return the difference
   */
  public Complex subtract(ComplexNumber c)
  {
    return ComplexUtils.subtract(this, c);
  }
  /**
     Subtract this complex number from another complex number
    
     @param c The other complex number
    
     @return the difference
   */
  public Complex subtractReversed(ComplexNumber c)
  {
    return ComplexUtils.subtract(c, this);
  }
  /**
     Subtract a real number from this complex number
    
     @param d The real number
    
     @return the difference
   */
  public Complex subtract(double d)
  {
    return ComplexUtils.subtract(this, d);
  }
  /**
     Subtract this complex number from a real number
    
     @param d The real number
    
     @return the difference
   */
  public Complex subtractReversed(double d)
  {
    return ComplexUtils.subtract(d, this);
  }
  /**
     Multiply this complex number by another complex number
    
     @param c The complex number multiplier
    
     @return the product
   */
  public Complex multiply(ComplexNumber c)
  {
    return ComplexUtils.multiply(this, c);
  }
  /**
     Multiply this complex number by a real number
    
     @param d The real number multiplier
    
     @return the product
   */
  public Complex multiply(double d)
  {
    return ComplexUtils.multiply(this, d);
  }
  /**
     Multiply this complex number by an integer
    
     @param i The integer multiplier
    
     @return the product
   */
  public Complex multiply(int i)
  {
    return ComplexUtils.multiply(this, i);
  }
  /**
     Divide this complex number by another complex number
    
     @param c The complex number divisor
    
     @return the result of this division
   */
  public Complex divide(ComplexNumber c)
  {
    return ComplexUtils.divide(this, c);
  }
  /**
     Divide this complex number by a real number
    
     @param d The real number divisor
    
     @return the result of this division
   */
  public Complex divide(double d)
  {
    return ComplexUtils.divide(this, d);
  }
  /**
     Divide another complex number by this complex number
    
     @param c The complex number dividend
    
     @return the result of this division
   */
  public Complex divideReversed(ComplexNumber c)
  {
    return ComplexUtils.divide(c, this);
  }
  /**
     Divide a real number by this complex number
    
     @param d The real number dividend
    
     @return the result of this division
   */
  public Complex divideReversed(double d)
  {
    return ComplexUtils.divide(d, this);
  }
  /**
     Calculates the negation of this complex number
    
     @return -this
   */
  public Complex negate()
  {
    return ComplexUtils.negate(this);
  }
  /**
     Calculates the conjugate of this complex number
    
     @return The square root
   */
  public Complex conjugate()
  {
    return ComplexUtils.conjugate(this);
  }
  /**
     Calculates the square root of this complex number
    
     @return The square root
   */
  public Complex sqrt()
  {
    return ComplexUtils.sqrt(this);
  }
  /**
     Calculates the exponential of this complex number
    
     @return e raised to the power this
   */
  public Complex exp()
  {
    return ComplexUtils.exp(this);
  }
  /**
     Returns exp(this)-1. For values of this near 0, calculating expm1(this) is
     much more accurate than calculating exp(this)-1.
    
     @return The value exp(this)-1
   */
  public Complex expm1()
  {
    return ComplexUtils.expm1(this);
  }
  /**
     Calculates the natural logarithm of this complex number
    
     @return The natural logarithm
   */
  public Complex log()
  {
    return ComplexUtils.log(this);
  }
  /**
     Returns log(this+1). For values of this near 0, calculating log1p(this)
     is much more accurate than calculating log(1+this).
    
     @return The value log(1+this)
   */
  public Complex log1p()
  {
    return ComplexUtils.log1p(this);
  }
  /**
     Calculate the inverse hyperbolic cosine of this complex number.
    
     @return The inverse hyperbolic cosine of this complex number
   */
  public Complex acosh()
  {
    return ComplexUtils.acosh(this);
  }
  /**
     Calculate the inverse hyperbolic sine of this complex number.
    
     @return The inverse hyperbolic sine of this complex number
   */
  public Complex asinh()
  {
    return ComplexUtils.asinh(this);
  }
  /**
     Calculate the inverse hyperbolic tangent of this complex number.
    
     @return The inverse hyperbolic tangent of this complex number
   */
  public Complex atanh()
  {
    return ComplexUtils.atanh(this);
  }
  /**
     Calculate the inverse cosine of this complex number.
    
     @return The inverse cosine of this complex number
   */
  public Complex acos()
  {
    return ComplexUtils.acos(this);
  }
  /**
     Calculate the inverse sine of this complex number.
    
     @return The inverse sine of this complex number
   */
  public Complex asin()
  {
    return ComplexUtils.asin(this);
  }
  /**
     Calculate the inverse tangent of this complex number.
    
     @return The inverse tangent of this complex number
   */
  public Complex atan()
  {
    return ComplexUtils.atan(this);
  }
  /**
     Calculate the cosine of this complex number.
    
     @return The cosine of this complex number
   */
  public Complex cos()
  {
    return ComplexUtils.cos(this);
  }
  /**
     Calculate the sine of this complex number.
    
     @return The sine of this complex number
   */
  public Complex sin()
  {
    return ComplexUtils.sin(this);
  }
  /**
     Calculate the tangent of this complex number.
    
     @return The tangent of this complex number
   */
  public Complex tan()
  {
    return ComplexUtils.tan(this);
  }
  /**
     Calculate the hyperbolic cosine of this complex number.
    
     @return The hyperbolic cosine of this complex number
   */
  public Complex cosh()
  {
    return ComplexUtils.cosh(this);
  }
  /**
     Calculate the hyperbolic sine of this complex number.
    
     @return The hyperbolic sine of this complex number
   */
  public Complex sinh()
  {
    return ComplexUtils.sinh(this);
  }
  /**
     Calculate the hyperbolic tangent of this complex number.
    
     @return The hyperbolic tangent of this complex number
   */
  public Complex tanh()
  {
    return ComplexUtils.tanh(this);
  }
  public Complex pow(ComplexNumber c)
  {
    return ComplexUtils.pow(this, c);
  }
  public Complex pow(double d)
  {
    return ComplexUtils.pow(this, d);
  }
  /**
     Calculates the inverse of this complex number
    
     @return 1 divided by this complex number
   */
  public Complex invert()
  {
    return ComplexUtils.invert(this);
  }

  /**
     Calculates the absolute value of this complex number.
    
     @return x&ge;0 the absolute value
   */
  public double abs()
  {
    return ComplexUtils.abs(this);
  }
  /**
     Calculates the argument of this complex number.

     The argument is the angle between the positive real axis and the point
     that represents this number in the complex plane.
    
     @return -pi&le;x&le;pi the argument
   */
  public double arg()
  {
    return ComplexUtils.arg(this);
  }
  /**
     Check whether the complex number is NaN (not-a-numer).

     A complex number is considered NaN if either the real or the imaginary
     part is NaN.

     @return Whether the complex number is NaN
   */
  public boolean isNaN()
  {
    return ComplexUtils.isNaN(this);
  }
  /**
     Check whether the complex number is infinite.

     A complex number is considered infinite if either the real or the
     imaginary part is infinite. If either the real of imaginary part
     is NaN, the number is not considered infinite, so isNaN() and
     isInfinite() cannot be true at the same time.

     @return Whether the complex number is infinite
   */
  public boolean isInfinite()
  {
    return ComplexUtils.isInfinite(this);
  }
  /**
     Returns a text representation of the complex number.

     @return A text representation of the complex number.
     @return "NaN" if NaN
     @return re if purely real
     @return im + "i" if purely imaginary
     @return re " + " + im + "i" if imaginary part positive
     @return re " - " + (-im) + "i" if imaginary part negative

   */
  public String toString()
  {
    return ComplexUtils.toString(this);
  }
  /**
     Returns a hash code of the complex number.

     The hash code is based on the bit representations of the real and
     imaginary parts. If two complex numbers are considered equal by the
     equals method, they have the same hash code. The hash code calculation
     is the same as ComplexUtils.hashCode.

     @return The hash code
   */
  public int hashCode()
  {
    return ComplexUtils.hashCode(this);
  }
  /**
     Compare this complex number to another object.

     Note that this compares the bit representations of the real and imaginary
     parts. Thus for example NaN is equal to itself and +0.0+0.0i is not equal
     to -0.0-0.0i. All instances of NaN are considered equal. This definition
     allows hash tables to work properly. The equality comparison is the same
     as ComplexUtils.equal.

     @return true if both complex numbers are NaN
     @return true if the bit representations of the real and imaginary
             parts are equal
     @return false if o is not an instance of the Complex class
     @return false otherwise
   */
  public boolean equals(Object o)
  {
    Complex c;
    if (this == o)
    {
      return true;
    }
    try {
      c = (Complex)o;
    }
    catch(ClassCastException e)
    {
      return false;
    }
    return ComplexUtils.equal(this, c);
  }
};
