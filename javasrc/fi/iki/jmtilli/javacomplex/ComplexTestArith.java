package fi.iki.jmtilli.javacomplex;
/**
   Unit test for the arithmetic operations of complex numbers
 */
public class ComplexTestArith {
  private static void assertSameObject(Object a, Object b)
  {
    if (a != b)
    {
      throw new RuntimeException("different objects: " + a + ", " + b);
    }
  }
  private static void assertEqual(double a, double b)
  {
    if (Double.doubleToLongBits(a) != Double.doubleToLongBits(b))
    {
      throw new RuntimeException("inequal: " + a + ", " + b);
    }
  }
  private static void assertEqualTolerance(double tol, double a, double b)
  {
    if (Math.abs(a - b) > tol)
    {
      throw new RuntimeException("inequal: " + a + ", " + b);
    }
  }
  private static void assertEqualTolerance(double tol,
                                          ComplexNumber a,ComplexNumber b)
  {
    if (ComplexUtils.subtract(a, b).abs() > tol)
    {
      throw new RuntimeException("inequal: " + a + ", " + b);
    }
  }
  private static void assertTrue(boolean b)
  {
    if (!b)
    {
      throw new RuntimeException("false");
    }
  }
  private static void assertFalse(boolean b)
  {
    if (b)
    {
      throw new RuntimeException("true");
    }
  }
  private static void addTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).add(new Complex(3, 4)),
                         new Complex(4, 6));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).add(new Complex(-3, -4)),
                         new Complex(-2, -2));
  }
  private static void addBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.addInPlace(new Complex(3, 4)));
    assertEqualTolerance(1e-10, buf, new Complex(4, 6));
    assertSameObject(buf, buf.set(new Complex(1,2)));
    assertSameObject(buf, buf.addInPlace(new ComplexBuffer(-3, -4)));
    assertEqualTolerance(1e-10, buf, new Complex(-2, -2));
  }
  private static void addDoubleTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).add(3.0),
                         new Complex(4, 2));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).add(-3.0),
                         new Complex(-2, 2));
  }
  private static void addDoubleBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.addInPlace(3.0));
    assertEqualTolerance(1e-10, buf, new Complex(4, 2));
    assertSameObject(buf, buf.set(new Complex(1,2)));
    assertSameObject(buf, buf.addInPlace(-3.0));
    assertEqualTolerance(1e-10, buf, new Complex(-2, 2));
  }
  private static void subtractTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).subtract(new Complex(3, 4)),
                         new Complex(-2, -2));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).subtract(new Complex(-3, -4)),
                         new Complex(4, 6));
  }
  private static void subtractBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.subtractInPlace(new Complex(3, 4)));
    assertEqualTolerance(1e-10, buf, new Complex(-2, -2));
    assertSameObject(buf, buf.set(new Complex(1,2)));
    assertSameObject(buf, buf.subtractInPlace(new ComplexBuffer(-3, -4)));
    assertEqualTolerance(1e-10, buf, new Complex(4, 6));
  }
  private static void subtractDoubleTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).subtract(3.0),
                         new Complex(-2, 2));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).subtract(-3.0),
                         new Complex(4, 2));
  }
  private static void subtractDoubleBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.subtractInPlace(3.0));
    assertEqualTolerance(1e-10, buf, new Complex(-2, 2));
    assertSameObject(buf, buf.set(new Complex(1,2)));
    assertSameObject(buf, buf.subtractInPlace(-3.0));
    assertEqualTolerance(1e-10, buf, new Complex(4, 2));
  }
  private static void subtractReversedTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).subtractReversed(new Complex(3, 4)),
                         new Complex(2, 2));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).subtractReversed(
                                              new Complex(-3, -4)),
                         new Complex(-4, -6));
  }
  private static void subtractReversedBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.subtractReversedInPlace(new Complex(3, 4)));
    assertEqualTolerance(1e-10, buf, new Complex(2, 2));
    assertSameObject(buf, buf.set(new Complex(1,2)));
    assertSameObject(buf, buf.subtractReversedInPlace(
                                  new ComplexBuffer(-3, -4)));
    assertEqualTolerance(1e-10, buf, new Complex(-4, -6));
  }
  private static void subtractReversedDoubleTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).subtractReversed(3.0),
                         new Complex(2, -2));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).subtractReversed(-3.0),
                         new Complex(-4, -2));
  }
  private static void subtractReversedDoubleBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.subtractReversedInPlace(3.0));
    assertEqualTolerance(1e-10, buf, new Complex(2, -2));
    assertSameObject(buf, buf.set(new Complex(1, 2)));
    assertSameObject(buf, buf.subtractReversedInPlace(-3.0));
    assertEqualTolerance(1e-10, buf, new Complex(-4, -2));
  }
  private static void multiplyIntTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).multiply(2),
                         new Complex(2, 4));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).multiply(-1),
                         new Complex(-1, -2));
  }
  private static void multiplyIntBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.multiplyInPlace(2));
    assertEqualTolerance(1e-10, buf, new Complex(2, 4));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.multiplyInPlace(-1));
    assertEqualTolerance(1e-10, buf, new Complex(-1, -2));
  }
  private static void multiplyDoubleTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).multiply(2.0),
                         new Complex(2, 4));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).multiply(-1.5),
                         new Complex(-1.5, -3.0));
  }
  private static void multiplyDoubleBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.multiplyInPlace(2.0));
    assertEqualTolerance(1e-10, buf, new Complex(2, 4));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.multiplyInPlace(-1.5));
    assertEqualTolerance(1e-10, buf, new Complex(-1.5, -3.0));
  }
  private static void multiplyTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).multiply(new Complex(3, 4)),
                         new Complex(-5, 10));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).multiply(new Complex(-3, -4)),
                         new Complex(5, -10));
  }
  private static void multiplyBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.multiplyInPlace(new Complex(3, 4)));
    assertEqualTolerance(1e-10, buf, new Complex(-5, 10));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.multiplyInPlace(new Complex(-3, -4)));
    assertEqualTolerance(1e-10, buf, new Complex(5, -10));
  }
  private static void conjugateTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).conjugate(),
                         new Complex(1, -2));
    assertEqualTolerance(1e-10,
                         new Complex(1, -2).conjugate(),
                         new Complex(1, 2));
  }
  private static void conjugateBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.conjugateInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1, -2));
    assertSameObject(buf, buf.set(1,-2));
    assertSameObject(buf, buf.conjugateInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1, 2));
  }
  private static void negateTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).negate(),
                         new Complex(-1, -2));
    assertEqualTolerance(1e-10,
                         new Complex(1, -2).negate(),
                         new Complex(-1, 2));
  }
  private static void negateBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.negateInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(-1, -2));
    assertSameObject(buf, buf.set(1,-2));
    assertSameObject(buf, buf.negateInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(-1, 2));
  }
  private static void invertTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).multiply(
                           new Complex(1, 2).invert()),
                         new Complex(1, 0));
    assertEqualTolerance(1e-10,
                         new Complex(1, -2).multiply(
                           new Complex(1, -2).invert()),
                         new Complex(1, 0));
  }
  private static void invertBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.invertInPlace());
    assertSameObject(buf, buf.multiplyInPlace(new Complex(1, 2)));
    assertEqualTolerance(1e-10, buf, new Complex(1, 0));
    assertSameObject(buf, buf.set(1,-2));
    assertSameObject(buf, buf.invertInPlace());
    assertSameObject(buf, buf.multiplyInPlace(new Complex(1, -2)));
    assertEqualTolerance(1e-10, buf, new Complex(1, 0));
  }
  private static void divideTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).divide(new Complex(3, 4)),
                         new Complex(0.44, 0.08));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).divide(new Complex(-3, -4)),
                         new Complex(-0.44, -0.08));
    assertEqualTolerance(1e-10,
                         new Complex(1.79319890202516e+308,
                                     1.79319890202516e+308).divide(
                                     new Complex(1.79319890202516e+308,
                                                 1.79319890202516e+308)
                                     ),
                         new Complex(1, 0));
  }
  private static void divideBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.divideInPlace(new Complex(3, 4)));
    assertEqualTolerance(1e-10, buf, new Complex(0.44, 0.08));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.divideInPlace(new Complex(-3, -4)));
    assertEqualTolerance(1e-10, buf, new Complex(-0.44, -0.08));
    assertSameObject(buf, buf.set(1.79319890202516e+308,
                                  1.79319890202516e+308));
    assertSameObject(buf, buf.divideInPlace(
                               new Complex(1.79319890202516e+308,
                                           1.79319890202516e+308)));
    assertEqualTolerance(1e-10, buf, new Complex(1, 0));
  }
  private static void divideDoubleTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).divide(3.0),
                         new Complex(1.0/3.0, 2.0/3.0));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).divide(-3.0),
                         new Complex(-1.0/3.0, -2.0/3.0));
    assertEqualTolerance(1e-10,
                         new Complex(1.79319890202516e+308,
                                     1.79319890202516e+308).divide(
                                         1.79319890202516e+308),
                         new Complex(1, 1));
  }
  private static void divideDoubleBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.divideInPlace(3.0));
    assertEqualTolerance(1e-10, buf, new Complex(1.0/3.0, 2.0/3.0));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.divideInPlace(-3.0));
    assertEqualTolerance(1e-10, buf, new Complex(-1.0/3.0, -2.0/3.0));
    assertSameObject(buf, buf.set(1.79319890202516e+308,
                                  1.79319890202516e+308));
    assertSameObject(buf, buf.divideInPlace(1.79319890202516e+308));
    assertEqualTolerance(1e-10, buf, new Complex(1, 1));
  }
  private static void divideReversedTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).divideReversed(new Complex(3, 4)),
                         new Complex(2.2, -0.4));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).divideReversed(new Complex(-3, -4)),
                         new Complex(-2.2, 0.4));
    assertEqualTolerance(1e-10,
                         new Complex(1.79319890202516e+308,
                                     1.79319890202516e+308).divideReversed(
                                     new Complex(1.79319890202516e+308,
                                                 1.79319890202516e+308)
                                     ),
                         new Complex(1, 0));
  }
  private static void divideReversedBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.divideReversedInPlace(new Complex(3, 4)));
    assertEqualTolerance(1e-10, buf, new Complex(2.2, -0.4));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.divideReversedInPlace(new Complex(-3, -4)));
    assertEqualTolerance(1e-10, buf, new Complex(-2.2, 0.4));
    assertSameObject(buf, buf.set(1.79319890202516e+308,
                                  1.79319890202516e+308));
    assertSameObject(buf, buf.divideReversedInPlace(
                                     new Complex(1.79319890202516e+308,
                                                 1.79319890202516e+308)
                                     ));
    assertEqualTolerance(1e-10, buf, new Complex(1, 0));
  }
  private static void divideReversedDoubleTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).divideReversed(2.0),
                         new Complex(0.4, -0.8));
    assertEqualTolerance(1e-10,
                         new Complex(1, 2).divideReversed(-2.0),
                         new Complex(-0.4, 0.8));
    assertEqualTolerance(1e-10,
                         new Complex(1.79319890202516e+307,
                                     1.79319890202516e+307).divideReversed(
                                         1.79319890202516e+307),
                         new Complex(0.5, -0.5));
  }
  private static void divideReversedDoubleBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.divideReversedInPlace(2.0));
    assertEqualTolerance(1e-10, buf, new Complex(0.4, -0.8));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.divideReversedInPlace(-2.0));
    assertEqualTolerance(1e-10, buf, new Complex(-0.4, 0.8));
    assertSameObject(buf, buf.set(1.79319890202516e+307,
                                  1.79319890202516e+307));
    assertSameObject(buf, buf.divideReversedInPlace(
                                  1.79319890202516e+307));
    assertEqualTolerance(1e-10, buf, new Complex(0.5, -0.5));
  }
  private static void sqrtTest()
  {
    assertEqualTolerance(1e-10,
                         new Complex(0, 0).sqrt(),
                         new Complex(0, 0));

    assertEqualTolerance(1e-10,
                         new Complex(1, 2).sqrt(),
                         new Complex(1.2720196495140691,
                                     0.786151377757423));
    assertEqualTolerance(1e-10,
                         new Complex(1, -2).sqrt(),
                         new Complex(1.2720196495140691,
                                     -0.786151377757423));
    assertEqualTolerance(1e-10,
                         new Complex(-1, -2).sqrt(),
                         new Complex(0.786151377757423,
                                     -1.272019649514069));
    assertEqualTolerance(1e-10,
                         new Complex(-1, 2).sqrt(),
                         new Complex(0.786151377757423,
                                     1.272019649514069));

    assertEqualTolerance(1e-10,
                         new Complex(2, 1).sqrt(),
                         new Complex(1.455346690225355,
                                     0.343560749722512));
    assertEqualTolerance(1e-10,
                         new Complex(2, -1).sqrt(),
                         new Complex(1.455346690225355,
                                     -0.343560749722512));
    assertEqualTolerance(1e-10,
                         new Complex(-2, -1).sqrt(),
                         new Complex(0.343560749722512,
                                     -1.455346690225355));
    assertEqualTolerance(1e-10,
                         new Complex(-2, 1).sqrt(),
                         new Complex(0.343560749722512,
                                     1.455346690225355));

    assertEqualTolerance(1e154*1e-10,
                         new Complex(1.79319890202516e+308,
                                     1.79319890202516e+308).sqrt(),
                         new Complex(1.47125203641349e+154,
                                     6.09412547151502e+153));
  }
  private static void sqrtBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(0,0));
    assertSameObject(buf, buf.sqrtInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0, 0));

    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.sqrtInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.2720196495140691,
                                                 0.786151377757423));

    assertSameObject(buf, buf.set(1,-2));
    assertSameObject(buf, buf.sqrtInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.2720196495140691,
                                                -0.786151377757423));

    assertSameObject(buf, buf.set(-1,-2));
    assertSameObject(buf, buf.sqrtInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.786151377757423,
                                                -1.272019649514069));

    assertSameObject(buf, buf.set(-1,2));
    assertSameObject(buf, buf.sqrtInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.786151377757423,
                                                 1.272019649514069));

    assertSameObject(buf, buf.set(2,1));
    assertSameObject(buf, buf.sqrtInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.455346690225355,
                                                 0.343560749722512));

    assertSameObject(buf, buf.set(2,-1));
    assertSameObject(buf, buf.sqrtInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.455346690225355,
                                                -0.343560749722512));

    assertSameObject(buf, buf.set(-2,-1));
    assertSameObject(buf, buf.sqrtInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.343560749722512,
                                                -1.455346690225355));

    assertSameObject(buf, buf.set(-2,1));
    assertSameObject(buf, buf.sqrtInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.343560749722512,
                                                 1.455346690225355));

    assertSameObject(buf, buf.set(
                         new Complex(1.79319890202516e+308,
                                     1.79319890202516e+308)));
    assertSameObject(buf, buf.sqrtInPlace());
    assertEqualTolerance(1e154*1e-10, buf,
                         new Complex(1.47125203641349e+154,
                                     6.09412547151502e+153));
  }

  private static void complexBufThisTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    //
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.set(buf));
    assertEqualTolerance(1e-10, buf, new Complex(1, 2));
    //
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.addInPlace(buf));
    assertEqualTolerance(1e-10, buf, new Complex(2, 4));
    //
    assertSameObject(buf, buf.set(new Complex(1,2)));
    assertSameObject(buf, buf.subtractInPlace(buf));
    assertEqualTolerance(1e-10, buf, new Complex(0, 0));
    //
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.subtractReversedInPlace(buf));
    assertEqualTolerance(1e-10, buf, new Complex(0, 0));
    //
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.multiplyInPlace(buf));
    assertEqualTolerance(1e-10, buf, new Complex(-3, 4));
    //
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.divideInPlace(buf));
    assertEqualTolerance(1e-10, buf, new Complex(1, 0));
    //
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.divideReversedInPlace(buf));
    assertEqualTolerance(1e-10, buf, new Complex(1, 0));
    //
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.powInPlace(buf));
    assertEqualTolerance(1e-10, buf,
                         new Complex(-0.222517156801773,
                                      0.100709131136075));
  }
  private static void trigFuncTest()
  {
    Complex num;
    num = new Complex(1, 2).sin();
    assertEqualTolerance(1e-10, num, new Complex(3.16577851321617,
                                                 1.95960104142161));
    num = new Complex(1, 2).cos();
    assertEqualTolerance(1e-10, num, new Complex(2.03272300701967,
                                                -3.05189779915180));
    num = new Complex(1, 2).tan();
    assertEqualTolerance(1e-10, num, new Complex(0.0338128260798966,
                                                 1.0147936161466335));
  }
  private static void trigFuncBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.sinInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(3.16577851321617,
                                                 1.95960104142161));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.cosInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(2.03272300701967,
                                                -3.05189779915180));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.tanInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.0338128260798966,
                                                 1.0147936161466335));
  }
  private static void powTest()
  {
    Complex num;
    num = new Complex(1, 2).pow(new Complex(3, 4));
    assertEqualTolerance(1e-10, num, new Complex(0.1290095940744670,
                                                 0.0339240929051701));
    num = new Complex(1, 2).pow(new Complex(3, 0));
    assertEqualTolerance(1e-10, num, new Complex(-11, -2));
    num = new Complex(1, 2).pow(new Complex(0, 3));
    assertEqualTolerance(1e-10, num, new Complex(-0.0269628767354069,
                                                  0.0240053256572358));
  }
  private static void powBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.powInPlace(new Complex(3, 4)));
    assertEqualTolerance(1e-10, buf, new Complex(0.1290095940744670,
                                                 0.0339240929051701));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.powInPlace(new Complex(3, 0)));
    assertEqualTolerance(1e-10, buf, new Complex(-11, -2));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.powInPlace(new Complex(0, 3)));
    assertEqualTolerance(1e-10, buf, new Complex(-0.0269628767354069,
                                                  0.0240053256572358));
  }
  private static void powDoubleTest()
  {
    Complex num;
    num = new Complex(1, 2).pow(-2.0);
    assertEqualTolerance(1e-10, num, new Complex(-0.12, -0.16));
    num = new Complex(1, 2).pow(-1.0);
    assertEqualTolerance(1e-10, num, new Complex(0.2, -0.4));
    num = new Complex(1, 2).pow(0.0);
    assertEqualTolerance(1e-10, num, new Complex(1.0, 0.0));
    num = new Complex(1, 2).pow(1.0);
    assertEqualTolerance(1e-10, num, new Complex(1.0, 2.0));
    num = new Complex(1, 2).pow(2.0);
    assertEqualTolerance(1e-10, num, new Complex(-3.0, 4.0));
  }
  private static void powDoubleBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.powInPlace(-2.0));
    assertEqualTolerance(1e-10, buf, new Complex(-0.12, -0.16));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.powInPlace(-1.0));
    assertEqualTolerance(1e-10, buf, new Complex(0.2, -0.4));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.powInPlace(0.0));
    assertEqualTolerance(1e-10, buf, new Complex(1.0, 0.0));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.powInPlace(1.0));
    assertEqualTolerance(1e-10, buf, new Complex(1.0, 2.0));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.powInPlace(2.0));
    assertEqualTolerance(1e-10, buf, new Complex(-3.0, 4.0));
  }
  private static void expTest()
  {
    Complex num;
    num = new Complex(-1, 2).exp();
    assertEqualTolerance(1e-10, num, new Complex(-0.153091865674226,
                                                  0.334511829239262));
    num = new Complex(-1, 0).exp();
    assertEqualTolerance(1e-10, num, new Complex(0.367879441171442, 0.0));
    num = new Complex(0, 0).exp();
    assertEqualTolerance(1e-10, num, new Complex(1.0, 0.0));
    num = new Complex(1, 0).exp();
    assertEqualTolerance(1e-10, num, new Complex(2.71828182845905, 0.0));
    num = new Complex(2, 2).exp();
    assertEqualTolerance(1e-10, num, new Complex(-3.07493232063936,
                                                  6.71884969742825));
  }
  private static void expm1Test()
  {
    Complex num;
    num = new Complex(-1, 2).expm1();
    assertEqualTolerance(1e-10, num, new Complex(-0.153091865674226-1,
                                                  0.334511829239262));
    num = new Complex(-1, 0).expm1();
    assertEqualTolerance(1e-10, num, new Complex(0.367879441171442-1, 0.0));
    num = new Complex(0, 0).expm1();
    assertEqualTolerance(1e-10, num, new Complex(1.0-1, 0.0));
    num = new Complex(1, 0).expm1();
    assertEqualTolerance(1e-10, num, new Complex(2.71828182845905-1, 0.0));
    num = new Complex(2, 2).expm1();
    assertEqualTolerance(1e-10, num, new Complex(-3.07493232063936-1,
                                                  6.71884969742825));
    num = new Complex(1e-20, 1e-20).expm1();
    assertEqualTolerance(1e-30, num, new Complex(1e-20, 1e-20));
  }
  private static void expm1BufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(-1, 2));
    assertSameObject(buf, buf.expm1InPlace());
    assertEqualTolerance(1e-10, buf, new Complex(-0.153091865674226-1,
                                                  0.334511829239262));
    assertSameObject(buf, buf.set(-1, 0));
    assertSameObject(buf, buf.expm1InPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.367879441171442-1, 0.0));
    assertSameObject(buf, buf.set(0, 0));
    assertSameObject(buf, buf.expm1InPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.0-1, 0.0));
    assertSameObject(buf, buf.set(1, 0));
    assertSameObject(buf, buf.expm1InPlace());
    assertEqualTolerance(1e-10, buf, new Complex(2.71828182845905-1, 0.0));
    assertSameObject(buf, buf.set(2, 2));
    assertSameObject(buf, buf.expm1InPlace());
    assertEqualTolerance(1e-10, buf, new Complex(-3.07493232063936-1,
                                                  6.71884969742825));
    assertSameObject(buf, buf.set(1e-20, 1e-20));
    assertSameObject(buf, buf.expm1InPlace());
    assertEqualTolerance(1e-30, buf, new Complex(1e-20, 1e-20));
  }
  private static void expBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(-1, 2));
    assertSameObject(buf, buf.expInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(-0.153091865674226,
                                                  0.334511829239262));
    assertSameObject(buf, buf.set(-1, 0));
    assertSameObject(buf, buf.expInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.367879441171442, 0.0));
    assertSameObject(buf, buf.set(0, 0));
    assertSameObject(buf, buf.expInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.0, 0.0));
    assertSameObject(buf, buf.set(1, 0));
    assertSameObject(buf, buf.expInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(2.71828182845905, 0.0));
    assertSameObject(buf, buf.set(2, 2));
    assertSameObject(buf, buf.expInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(-3.07493232063936,
                                                  6.71884969742825));
  }
  private static void logTest()
  {
    Complex num;
    num = new Complex(-1, 2).log();
    assertEqualTolerance(1e-10, num, new Complex(0.804718956217050,
                                                 2.034443935795703));
    num = new Complex(-1, 0).log();
    assertEqualTolerance(1e-10, num, new Complex(0.0, 3.141592653589793));
    num = new Complex(0, 0).log();
    assertTrue(num.isInfinite());
    num = new Complex(1, 0).log();
    assertEqualTolerance(1e-10, num, new Complex(0.0, 0.0));
    num = new Complex(2, 2).log();
    assertEqualTolerance(1e-10, num, new Complex(1.039720770839918,
                                                 0.785398163397448));
  }
  private static void logBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(-1, 2));
    assertSameObject(buf, buf.logInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.804718956217050,
                                                 2.034443935795703));
    assertSameObject(buf, buf.set(-1, 0));
    assertSameObject(buf, buf.logInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.0, 3.141592653589793));
    assertSameObject(buf, buf.set(0, 0));
    assertSameObject(buf, buf.logInPlace());
    assertTrue(buf.isInfinite());
    assertSameObject(buf, buf.set(1, 0));
    assertSameObject(buf, buf.logInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.0, 0.0));
    assertSameObject(buf, buf.set(2, 2));
    assertSameObject(buf, buf.logInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.039720770839918,
                                                 0.785398163397448));
  }
  private static void log1pTest()
  {
    Complex num;
    num = new Complex(-1-1, 2).log1p();
    assertEqualTolerance(1e-10, num, new Complex(0.804718956217050,
                                                 2.034443935795703));
    num = new Complex(-1-1, 0).log1p();
    assertEqualTolerance(1e-10, num, new Complex(0.0, 3.141592653589793));
    num = new Complex(0-1, 0).log1p();
    assertTrue(num.isInfinite());
    num = new Complex(1-1, 0).log1p();
    assertEqualTolerance(1e-10, num, new Complex(0.0, 0.0));
    num = new Complex(2-1, 2).log1p();
    assertEqualTolerance(1e-10, num, new Complex(1.039720770839918,
                                                 0.785398163397448));
    num = new Complex(1e-20, 1e-20).log1p();
    assertEqualTolerance(1e-30, num, new Complex(1e-20, 1e-20));
  }
  private static void log1pBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(-1-1, 2));
    assertSameObject(buf, buf.log1pInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.804718956217050,
                                                 2.034443935795703));
    assertSameObject(buf, buf.set(-1-1, 0));
    assertSameObject(buf, buf.log1pInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.0, 3.141592653589793));
    assertSameObject(buf, buf.set(0-1, 0));
    assertSameObject(buf, buf.log1pInPlace());
    assertTrue(buf.isInfinite());
    assertSameObject(buf, buf.set(1-1, 0));
    assertSameObject(buf, buf.log1pInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.0, 0.0));
    assertSameObject(buf, buf.set(1e-20, 1e-20));
    assertSameObject(buf, buf.log1pInPlace());
    assertEqualTolerance(1e-30, buf, new Complex(1e-20, 1e-20));
  }
  private static void hypFuncTest()
  {
    Complex num;
    num = new Complex(1,2).sinh();
    assertEqualTolerance(1e-10, num, new Complex(-0.489056259041294,
                                                  1.403119250622041));
    num = new Complex(1,2).cosh();
    assertEqualTolerance(1e-10, num, new Complex(-0.642148124715520,
                                                  1.068607421382778));
    num = new Complex(1,2).tanh();
    assertEqualTolerance(1e-10, num, new Complex(1.166736257240920,
                                                -0.243458201185725));
  }
  private static void hypFuncBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.sinhInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(-0.489056259041294,
                                                  1.403119250622041));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.coshInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(-0.642148124715520,
                                                  1.068607421382778));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.tanhInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.166736257240920,
                                                -0.243458201185725));
  }

  private static void arcusFuncTest()
  {
    Complex num;
    num = new Complex(1, 2).asin();
    assertEqualTolerance(1e-10, num, new Complex(0.427078586392476,
                                                 1.528570919480998));
    num = new Complex(1, 2).acos();
    assertEqualTolerance(1e-10, num, new Complex(1.14371774040242,
                                                -1.52857091948100));
    num = new Complex(1, 2).atan();
    assertEqualTolerance(1e-10, num, new Complex(1.338972522294493,
                                                 0.402359478108525));
  }
  private static void arcusFuncBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.asinInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.427078586392476,
                                                 1.528570919480998));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.acosInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.14371774040242,
                                                -1.52857091948100));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.atanInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.338972522294493,
                                                 0.402359478108525));
  }
  private static void areaFuncTest()
  {
    Complex num;
    num = new Complex(1, 2).asinh();
    assertEqualTolerance(1e-10, num, new Complex(1.46935174436819,
                                                 1.06344002357775));
    num = new Complex(1, 2).acosh();
    assertEqualTolerance(1e-10, num, new Complex(1.52857091948100,
                                                 1.14371774040242));
    num = new Complex(1, 2).atanh();
    assertEqualTolerance(1e-10, num, new Complex(0.173286795139986,
                                                 1.178097245096172));
  }
  private static void areaFuncBufTest()
  {
    ComplexBuffer buf = new ComplexBuffer();
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.asinhInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.46935174436819,
                                                 1.06344002357775));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.acoshInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(1.52857091948100,
                                                 1.14371774040242));
    assertSameObject(buf, buf.set(1,2));
    assertSameObject(buf, buf.atanhInPlace());
    assertEqualTolerance(1e-10, buf, new Complex(0.173286795139986,
                                                 1.178097245096172));
  }

  public static void main(String[] args)
  {
    addTest();
    addBufTest();
    addDoubleTest();
    addDoubleBufTest();
    subtractTest();
    subtractBufTest();
    subtractDoubleTest();
    subtractDoubleBufTest();
    subtractReversedTest();
    subtractReversedBufTest();
    subtractReversedDoubleTest();
    subtractReversedDoubleBufTest();
    multiplyIntTest();
    multiplyIntBufTest();
    multiplyDoubleTest();
    multiplyDoubleBufTest();
    multiplyTest();
    multiplyBufTest();
    divideTest();
    divideBufTest();
    divideDoubleTest();
    divideDoubleBufTest();
    divideReversedTest();
    divideReversedBufTest();
    divideReversedDoubleTest();
    divideReversedDoubleBufTest();
    conjugateTest();
    conjugateBufTest();
    negateTest();
    negateBufTest();
    invertTest();
    invertBufTest();
    sqrtTest();
    sqrtBufTest();
    powTest();
    powBufTest();
    powDoubleTest();
    powDoubleBufTest();
    expTest();
    expBufTest();
    expm1Test();
    expm1BufTest();
    logTest();
    logBufTest();
    log1pTest();
    log1pBufTest();
    trigFuncTest();
    trigFuncBufTest();
    hypFuncTest();
    hypFuncBufTest();
    arcusFuncTest();
    arcusFuncBufTest();
    areaFuncTest();
    areaFuncBufTest();
    complexBufThisTest();
  }
};
