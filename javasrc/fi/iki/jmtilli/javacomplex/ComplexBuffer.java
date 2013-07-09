package fi.iki.jmtilli.javacomplex;
import java.io.Serializable;

/**
   A mutable complex number.

   An instance of this class refers to a complex number the value of
   which can be changed. This allows higher performance, as if the
   value of the number can be changed, not that many small objects
   need to be created.
 */
public class ComplexBuffer implements ComplexNumber, Serializable {
  private static final long serialVersionUID = -7100163843976716496L;
  /**
     The real part.
     @serial
   */
  private double re;
  /**
     The imaginary part.
     @serial
   */
  private double im;

  /**
     Create a complex buffer that initially stores the specified complex number.

     @param num The specified complex number
   */
  public ComplexBuffer(ComplexNumber num)
  {
    this(num.getReal(), num.getImag());
  }
  /**
     Create a complex buffer that initially has zero real and imaginary parts.
   */
  public ComplexBuffer()
  {
    this(+0.0, +0.0);
  }
  /**
     Create a complex buffer that initially the specified real part and zero as
     the imaginary part.

     @param re The initial real part
   */
  public ComplexBuffer(double re)
  {
    this(re, +0.0);
  }
  /**
     Create a complex buffer that initially has the specified real and
     imaginary parts

     @param re The initial real part
     @param im The initial imaginary part
   */
  public ComplexBuffer(double re, double im)
  {
    this.set(re, im);
  }
  /**
     Modify the real part of this complex buffer and set imaginary part to zero.

     @param re The new real part
     @return this
   */
  public ComplexBuffer set(double re)
  {
    this.re = re;
    this.im = +0.0;
    return this;
  }
  /**
     Modify the real and imaginary parts of this complex buffer

     @param re The new real part
     @param im The new imaginary part
     @return this
   */
  public ComplexBuffer set(double re, double im)
  {
    this.re = re;
    this.im = im;
    return this;
  }
  /**
     Modify the value of this complex buffer

     @param num The new value
     @return this
   */
  public ComplexBuffer set(ComplexNumber num)
  {
    return this.set(num.getReal(), num.getImag());
  }
  /**
     Get the value of this complex buffer as an immutable object

     @return A new immutable complex number that has the same value as this
             buffer
   */
  public Complex get()
  {
    return Complex.valueOf(this.getReal(), this.getImag());
  }
  /**
     Add another complex number to this buffer and store the result in this
     buffer

     @param c The other complex number

     @return this
   */
  public ComplexBuffer addInPlace(ComplexNumber c)
  {
    return this.set(this.getReal() + c.getReal(), this.getImag() + c.getImag());
  }
  /**
     Add a real number to this buffer and store the result in this buffer

     @param d The real number

     @return this
   */
  public ComplexBuffer addInPlace(double d)
  {
    return this.set(this.getReal() + d, this.getImag());
  }
  /**
     Subtract another complex number from this buffer and store the result in
     this buffer

     @param c The other complex number

     @return this
   */
  public ComplexBuffer subtractInPlace(ComplexNumber c)
  {
    return this.set(this.getReal() - c.getReal(), this.getImag() - c.getImag());
  }
  /**
     Subtract a real number from this buffer and store the result in
     this buffer

     @param c The real number

     @return this
   */
  public ComplexBuffer subtractInPlace(double d)
  {
    return this.set(this.getReal() - d, this.getImag());
  }
  /**
     Subtract the value of this buffer from another complex number and store
     the result in this buffer

     @param c The other complex number

     @return this
   */
  public ComplexBuffer subtractReversedInPlace(ComplexNumber c)
  {
    return this.set(c.getReal() - this.getReal(), c.getImag() - this.getImag());
  }
  /**
     Subtract the value of this buffer from a real number and store the result
     in this buffer

     @param c The real number

     @return this
   */
  public ComplexBuffer subtractReversedInPlace(double d)
  {
    return this.set(d - this.getReal(), -this.getImag());
  }
  /**
     Negate the value of this buffer and store the result in this buffer

     @return this
   */
  public ComplexBuffer negateInPlace()
  {
    return this.set(-this.getReal(), -this.getImag());
  }
  /**
     Calculate the conjugate of the value of this buffer and store the
     result in this buffer

     @return this
   */
  public ComplexBuffer conjugateInPlace()
  {
    return this.set(this.getReal(), -this.getImag());
  }
  /**
     Calculate the inverse of the value of this buffer and store the
     result in this buffer

     @return this
   */
  public ComplexBuffer inverseInPlace()
  {
    return divideReversedInPlace(1.0);
  }
  /**
     Calculate the square root of the value of this buffer and store the result
     in this buffer

     @return this
   */
  public ComplexBuffer sqrtInPlace()
  {
    final double w = ComplexUtils.calcSqrtAuxiliaryNumber(this);
    final double re = this.getReal();
    final double im = this.getImag();
    if (w == 0.0)
    {
      return this.set(+0.0, +0.0);
    }
    else if (re >= 0.0)
    {
      return this.set(w, im/(2*w));
    }
    else if (im >= 0.0)
    {
      return this.set(Math.abs(im)/(2*w), w);
    }
    else
    {
      return this.set(Math.abs(im)/(2*w), -w);
    }
  }
  /**
     Calculate the exponential of the value of this buffer and store the result
     in this buffer

     @return this
   */
  public ComplexBuffer expInPlace()
  {
    final double m = Math.exp(this.getReal());
    return this.set(m*Math.cos(this.getImag()), m*Math.sin(this.getImag()));
  }
  /**
     Calculate the logarithm of the value of this buffer and store the result
     in this buffer

     @return this
   */
  public ComplexBuffer logInPlace()
  {
    return this.set(Math.log(this.abs()), this.arg());
  }
  /**
     Calculate the logarithm of 1 added to the value of this buffer and store
     the result in this buffer

     @return this
   */
  public ComplexBuffer log1pInPlace()
  {
    final double rho = this.abs();
    final double re = this.getReal();
    this.set(this.getReal() + 1, this.getImag());
    if (rho > 0.375)
    {
      return this.logInPlace();
    }
    return this.set(0.5*Math.log1p(2*re + rho*rho), this.arg());
  }
  /**
     Calculate exp(this)-1 and store the result in this buffer

     @return this
   */
  public ComplexBuffer expm1InPlace()
  {
    /*
       expm1(z) = exp(x)*exp(i*y) - 1
                =   expm1(x) * (1 - 2*sin(y/2)**2)
                  - 2*sin(y/2)**2
                  + i*sin(y)*(1 + expm1(x))
     */
    final double re = this.getReal();
    final double im = this.getImag();
    final double rho = this.abs();
    double expm1_re;
    double two_mul_sin_im_div_2_sq;
    if (rho > 0.5)
    {
      this.expInPlace();
      return this.set(this.getReal() - 1, this.getImag());
    }
    expm1_re = Math.expm1(re);
    two_mul_sin_im_div_2_sq = Math.sin(im/2);
    two_mul_sin_im_div_2_sq = two_mul_sin_im_div_2_sq * two_mul_sin_im_div_2_sq;
    return this.set(  expm1_re * (1 - two_mul_sin_im_div_2_sq)
                    - two_mul_sin_im_div_2_sq,
                    Math.sin(im)*(1 + expm1_re));
  }
  /**
     Calculate the inverse hyperbolic cosine of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer acoshInPlace()
  {
    // Could be optimized a lot, eg. multiplication with Complex.I
    this.acosInPlace();
    return this.multiplyInPlace(Complex.I);
  }
  /**
     Calculate the inverse hyperbolic sine of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer asinhInPlace()
  {
    // Could be optimized a lot, eg. multiplication with Complex.I
    this.multiplyInPlace(Complex.I);
    this.asinInPlace();
    this.multiplyInPlace(Complex.I);
    return this.negateInPlace();
  }
  /**
     Calculate the inverse hyperbolic tangent of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer atanhInPlace()
  {
    // Could be optimized a lot, eg. multiplication with Complex.I
    this.multiplyInPlace(Complex.I);
    this.atanInPlace();
    this.multiplyInPlace(Complex.I);
    return this.negateInPlace();
  }
  /**
     Calculate the inverse cosine of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer acosInPlace()
  {
    // Generates one object of garbage
    // Could be optimized a lot, eg. multiplication with Complex.I
    final ComplexBuffer copy = new ComplexBuffer(this);
    this.multiplyInPlace(this).subtractReversedInPlace(1.0).sqrtInPlace();
    this.multiplyInPlace(Complex.I).addInPlace(copy);
    this.logInPlace();
    return this.multiplyInPlace(Complex.I).negateInPlace();
  }
  /**
     Calculate the inverse sine of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer asinInPlace()
  {
    // Generates one object of garbage
    // Could be optimized a lot, eg. multiplication with Complex.I
    final ComplexBuffer copy = new ComplexBuffer(this);
    this.multiplyInPlace(this).subtractReversedInPlace(1.0).sqrtInPlace();
    this.addInPlace(copy.multiplyInPlace(Complex.I));
    this.logInPlace();
    return this.multiplyInPlace(Complex.I).negateInPlace();
  }
  /**
     Calculate the inverse tangent of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer atanInPlace()
  {
    // Generates one object of garbage
    // Could be optimized a lot, eg. mul/add/sub with Complex.I
    final ComplexBuffer copy = new ComplexBuffer(this);
    this.addInPlace(Complex.I);
    this.divideInPlace(copy.subtractReversedInPlace(Complex.I));
    this.logInPlace();
    return this.multiplyInPlace(0.5).multiplyInPlace(Complex.I);
  }
  /**
     Calculate the cosine of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer cosInPlace()
  {
    return this.set( Math.cos(getReal()) * Math.cosh(getImag()),
                    -Math.sin(getReal()) * Math.sinh(getImag()));
  }
  /**
     Calculate the sine of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer sinInPlace()
  {
    return this.set(Math.sin(getReal()) * Math.cosh(getImag()),
                    Math.cos(getReal()) * Math.sinh(getImag()));
  }
  /**
     Calculate the tangent of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer tanInPlace()
  {
    final double real_x2 = this.getReal() * 2;
    final double imag_x2 = this.getImag() * 2;
    final double d = Math.cos(real_x2) + Math.cosh(imag_x2);
    return this.set(Math.sin(real_x2)/d, Math.sinh(imag_x2)/d);
  }
  /**
     Calculate the hyperbolic cosine of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer coshInPlace()
  {
    return this.set(Math.cosh(getReal()) * Math.cos(getImag()),
                    Math.sinh(getReal()) * Math.sin(getImag()));

  }
  /**
     Calculate the hyperbolic sine of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer sinhInPlace()
  {
    return this.set(Math.sinh(getReal()) * Math.cos(getImag()),
                    Math.cosh(getReal()) * Math.sin(getImag()));
  }
  /**
     Calculate the hyperbolic tangent of the value of the buffer
     and store the result in this buffer

     @return this
   */
  public ComplexBuffer tanhInPlace()
  {
    final double real_x2 = this.getReal() * 2;
    final double imag_x2 = this.getImag() * 2;
    final double d = Math.cosh(real_x2) + Math.cos(imag_x2);
    return this.set(Math.sinh(real_x2)/d, Math.sin(imag_x2)/d);
  }
  /**
     Raise this complex number to a real power
     and store the result in this buffer

     @param b The real power

     @return this
   */
  public ComplexBuffer powInPlace(double b)
  {
    return this.logInPlace().multiplyInPlace(b).expInPlace();
  }
  /**
     Raise this complex number to a complex power
     and store the result in this buffer

     @param b The complex power

     @return this
   */
  public ComplexBuffer powInPlace(ComplexNumber b)
  {
    if (b == this)
    {
      b = new Complex(b); // freeze b to make it work if b == this
    }
    return this.logInPlace().multiplyInPlace(b).expInPlace();
  }
  /**
     Multiply the value of this complex buffer by another complex number
     and store the result in this buffer

     @param c The other complex number

     @return this
   */
  public ComplexBuffer multiplyInPlace(ComplexNumber c)
  {
    double this_re = this.getReal(), this_im = this.getImag();
    double that_re = c.getReal(), that_im = c.getImag();
    return this.set(this_re*that_re - this_im*that_im,
                    this_im*that_re + this_re*that_im);
  }
  /**
     Multiply the value of this complex buffer by a real number
     and store the result in this buffer

     @param d The real number

     @return this
   */
  public ComplexBuffer multiplyInPlace(double d)
  {
    return this.set(this.getReal() * d, this.getImag() * d);
  }
  /**
     Multiply the value of this complex buffer by an integer
     and store the result in this buffer

     @param i The integer

     @return this
   */
  public ComplexBuffer multiplyInPlace(int i)
  {
    return this.set(this.getReal() * i, this.getImag() * i);
  }
  /**
     Divide the value of this complex buffer by another complex number
     and store the result in this buffer

     @param c The other complex number

     @return this
   */
  public ComplexBuffer divideInPlace(ComplexNumber c)
  {
    final double this_re = this.getReal(), this_im = this.getImag();
    final double c_re = c.getReal(), c_im = c.getImag();
    if (Math.abs(c_re) > Math.abs(c_im))
    {
      final double c_im_div_re = c_im/c_re;
      final double w = 1.0 / (c_re + c_im*c_im_div_re);
      return this.set((this_re + this_im*c_im_div_re) * w,
                      (this_im - this_re*c_im_div_re) * w);
    }
    else
    {
      final double c_re_div_im = c_re/c_im;
      final double w = 1.0 / (c_im + c_re*c_re_div_im);
      return this.set((this_re*c_re_div_im + this_im) * w,
                      (this_im*c_re_div_im - this_re) * w);
    }
  }
  /**
     Divide the value of this complex buffer by a real number
     and store the result in this buffer

     @param c The real number

     @return this
   */
  public ComplexBuffer divideInPlace(double d)
  {
    return this.set(this.getReal() / d, this.getImag() / d);
  }
  /**
     Divide another complex number by the value of this complex buffer
     and store the result in this buffer

     @param c The other complex number

     @return this
   */
  public ComplexBuffer divideReversedInPlace(ComplexNumber c)
  {
    final double c_re = c.getReal(), c_im = c.getImag();
    final double this_re = this.getReal(), this_im = this.getImag();
    if (Math.abs(this_re) > Math.abs(this_im))
    {
      final double this_im_div_re = this_im/this_re;
      final double w = 1.0 / (this_re + this_im*this_im_div_re);
      return this.set((c_re + c_im*this_im_div_re) * w,
                      (c_im - c_re*this_im_div_re) * w);
    }
    else
    {
      final double this_re_div_im = this_re/this_im;
      final double w = 1.0 / (this_im + this_re*this_re_div_im);
      return this.set((c_re*this_re_div_im + c_im) * w,
                      (c_im*this_re_div_im - c_re) * w);
    }
  }
  /**
     Divide a real number by the value of this complex buffer
     and store the result in this buffer

     @param d The real number

     @return this
   */
  public ComplexBuffer divideReversedInPlace(double d)
  {
    final double old_re = this.getReal();
    final double old_im = this.getImag();
    if (Math.abs(old_re) > Math.abs(old_im))
    {
      final double im_div_re = old_im/old_re;
      final double w = d / (old_re + old_im*im_div_re);
      return this.set(w, -im_div_re * w);
    }
    else
    {
      final double re_div_im = old_re/old_im;
      final double w = d / (old_im + old_re*re_div_im);
      return this.set(re_div_im * w, -w);
    }
  }

  /**
     Returns the real part of the complex number.

     @return The real part
   */
  public double getReal()
  {
    return this.re;
  }
  /**
     Returns the imaginary part of the complex number.

     @return The imaginary part
   */
  public double getImag()
  {
    return this.im;
  }

  /**
     Calculate the absolute value of the complex number in this complex buffer.

     @return x&ge;0 The absolute value
   */
  public double abs()
  {
    return ComplexUtils.abs(this);
  }
  /**
     Calculate the argument of the complex number in this complex buffer.

     The argument is the angle between the positive real axis and the point
     that represents this number in the complex plane.

     @return -pi&le;x&le;pi The argument
   */
  public double arg()
  {
    return ComplexUtils.arg(this);
  }
  /**
     Check whether the complex number in this buffer is NaN (not-a-numer).

     A complex number is considered NaN if either the real or the imaginary
     part is NaN.

     @return Whether the complex number in this buffer is NaN
   */
  public boolean isNaN()
  {
    return ComplexUtils.isNaN(this);
  }
  /**
     Check whether the complex number in this buffer is infinite.

     A complex number is considered infinite if either the real or the
     imaginary part is infinite. If either the real of imaginary part
     is NaN, the number is not considered infinite, so isNaN() and
     isInfinite() cannot be true at the same time.

     @return Whether the complex number in this buffer is infinite
   */
  public boolean isInfinite()
  {
    return ComplexUtils.isInfinite(this);
  }
  /**
     Returns a String representation of the complex number in this complex
     buffer.
    
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

};
