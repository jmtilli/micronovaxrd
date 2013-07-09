package fi.iki.jmtilli.javacomplex;
/**
   Unit test for the equality comparisons of complex numbers
 */
public class ComplexTestEquality {
  private static void assertEqual(double a, double b)
  {
    if (Double.doubleToLongBits(a) != Double.doubleToLongBits(b))
    {
      throw new RuntimeException("inequal: " + a + ", " + b);
    }
  }
  private static void assertEqual(String a, String b)
  {
    if (!a.equals(b))
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
  private static void hashCodeTest()
  {
    // test NaN
    assertTrue(   new Complex(Double.NaN, 0.0).hashCode()
               == Complex.NaN.hashCode());
    assertTrue(   new Complex(0.0, Double.NaN).hashCode()
               == Complex.NaN.hashCode());
    assertTrue(   new Complex(Double.NaN, Double.NaN).hashCode()
               == Complex.NaN.hashCode());

    // test infinity
    assertTrue(   new Complex(Double.POSITIVE_INFINITY,
                              Double.POSITIVE_INFINITY).hashCode()
               != new Complex(Double.NEGATIVE_INFINITY,
                              Double.NEGATIVE_INFINITY).hashCode());
    assertTrue(   new Complex(Double.POSITIVE_INFINITY,
                              Double.POSITIVE_INFINITY).hashCode()
               != new Complex(Double.NEGATIVE_INFINITY,
                              Double.POSITIVE_INFINITY).hashCode());
    assertTrue(   new Complex(Double.POSITIVE_INFINITY,
                              Double.POSITIVE_INFINITY).hashCode()
               != new Complex(Double.POSITIVE_INFINITY,
                              Double.NEGATIVE_INFINITY).hashCode());
    assertTrue(   new Complex(Double.POSITIVE_INFINITY,
                              Double.NEGATIVE_INFINITY).hashCode()
               != new Complex(Double.NEGATIVE_INFINITY,
                              Double.NEGATIVE_INFINITY).hashCode());
    assertTrue(   new Complex(Double.NEGATIVE_INFINITY,
                              Double.POSITIVE_INFINITY).hashCode()
               != new Complex(Double.NEGATIVE_INFINITY,
                              Double.NEGATIVE_INFINITY).hashCode());
    assertTrue(   new Complex(Double.NEGATIVE_INFINITY,
                              Double.POSITIVE_INFINITY).hashCode()
               != new Complex(Double.POSITIVE_INFINITY,
                              Double.NEGATIVE_INFINITY).hashCode());
    assertTrue(   new Complex(Double.POSITIVE_INFINITY,
                              Double.POSITIVE_INFINITY).hashCode()
               != new Complex(0.0,
                              0.0).hashCode());
    assertTrue(   new Complex(Double.POSITIVE_INFINITY,
                              Double.POSITIVE_INFINITY).hashCode()
               != new Complex(0.0,
                              Double.POSITIVE_INFINITY).hashCode());
    assertTrue(   new Complex(Double.POSITIVE_INFINITY,
                              Double.POSITIVE_INFINITY).hashCode()
               != new Complex(Double.POSITIVE_INFINITY,
                              0.0).hashCode());
    assertTrue(   new Complex(Double.POSITIVE_INFINITY,
                              0.0).hashCode()
               != new Complex(0.0,
                              0.0).hashCode());
    assertTrue(   new Complex(0.0,
                              Double.POSITIVE_INFINITY).hashCode()
               != new Complex(0.0,
                              0.0).hashCode());
    assertTrue(   new Complex(0.0,
                              Double.POSITIVE_INFINITY).hashCode()
               != new Complex(Double.POSITIVE_INFINITY,
                              0.0).hashCode());

    // test negative zero
    assertTrue(   new Complex(+0.0, +0.0).hashCode()
               != new Complex(-0.0, -0.0).hashCode());
    assertTrue(   new Complex(+0.0, +0.0).hashCode()
               != new Complex(-0.0, +0.0).hashCode());
    assertTrue(   new Complex(+0.0, +0.0).hashCode()
               != new Complex(+0.0, -0.0).hashCode());
    assertTrue(   new Complex(+0.0, -0.0).hashCode()
               != new Complex(-0.0, -0.0).hashCode());
    assertTrue(   new Complex(-0.0, +0.0).hashCode()
               != new Complex(-0.0, -0.0).hashCode());
    assertTrue(   new Complex(-0.0, +0.0).hashCode()
               != new Complex(+0.0, -0.0).hashCode());

    // test Complex buffer: uses java.lang.Object's hashCode
    assertTrue(   new ComplexBuffer().hashCode()
               != new ComplexBuffer().hashCode());

    // trivial test: zero and one must have different hash codes
    assertTrue(Complex.ZERO.hashCode() != Complex.ONE.hashCode());

    // test that imaginary and real parts are handled in a different way
    assertTrue(Complex.ONE.hashCode() != Complex.I.hashCode());
    assertTrue(new Complex(1, 2).hashCode() != new Complex(2, 1).hashCode());

    // test that ComplexUtils has the same hashCode than Complex
    assertTrue(Complex.ZERO.hashCode() == ComplexUtils.hashCode(Complex.ZERO));
    assertTrue(Complex.ONE.hashCode() == ComplexUtils.hashCode(Complex.ONE));
    assertTrue(Complex.I.hashCode() == ComplexUtils.hashCode(Complex.I));
  }
  private static void equalityTests()
  {
    Complex a, b;
    for (int i = 0; i < 1000; i++)
    {
      a = new Complex((Math.random()-0.5)*1000, (Math.random()-0.5)*1000);
      b = new Complex(a);
      assertTrue(a != b);
      assertTrue(a.equals(b));
      assertFalse(a.equals(new ComplexBuffer(b)));
      assertTrue(ComplexUtils.equal(a, b));
      assertTrue(ComplexUtils.equalRealImag(a, b));
      assertTrue(ComplexUtils.longBitsEqual(a, b));
      assertTrue(ComplexUtils.equal(a, new ComplexBuffer(b)));
      assertTrue(ComplexUtils.equalRealImag(a, new ComplexBuffer(b)));
      assertTrue(ComplexUtils.longBitsEqual(a, new ComplexBuffer(b)));
    }
    // test NaN
    assertTrue(ComplexUtils.equal(
                  new Complex(Double.NaN, 0.0),
                  Complex.NaN));
    assertTrue(ComplexUtils.equal(
                  new Complex(0.0, Double.NaN),
                  Complex.NaN));
    assertTrue(ComplexUtils.equal(
                  new Complex(Double.NaN, Double.NaN),
                  Complex.NaN));
    assertFalse(ComplexUtils.longBitsEqual(
                  new Complex(Double.NaN, 0.0),
                  Complex.NaN));
    assertFalse(ComplexUtils.longBitsEqual(
                  new Complex(0.0, Double.NaN),
                  Complex.NaN));
    assertTrue(ComplexUtils.longBitsEqual(
                  new Complex(Double.NaN, Double.NaN),
                  Complex.NaN));
    assertFalse(ComplexUtils.equalRealImag(Complex.NaN, Complex.NaN));

    // test infinity
    assertFalse(new Complex(Double.POSITIVE_INFINITY,
                            Double.POSITIVE_INFINITY).equals(
                               new Complex(Double.NEGATIVE_INFINITY,
                                           Double.NEGATIVE_INFINITY)));
    assertFalse(new Complex(Double.POSITIVE_INFINITY,
                            Double.POSITIVE_INFINITY).equals(
                               new Complex(Double.NEGATIVE_INFINITY,
                                           Double.POSITIVE_INFINITY)));
    assertFalse(new Complex(Double.POSITIVE_INFINITY,
                            Double.POSITIVE_INFINITY).equals(
                               new Complex(Double.POSITIVE_INFINITY,
                                           Double.NEGATIVE_INFINITY)));
    assertFalse(new Complex(Double.POSITIVE_INFINITY,
                            Double.NEGATIVE_INFINITY).equals(
                               new Complex(Double.NEGATIVE_INFINITY,
                                           Double.NEGATIVE_INFINITY)));
    assertFalse(new Complex(Double.NEGATIVE_INFINITY,
                            Double.POSITIVE_INFINITY).equals(
                               new Complex(Double.NEGATIVE_INFINITY,
                                           Double.NEGATIVE_INFINITY)));
    assertFalse(new Complex(Double.NEGATIVE_INFINITY,
                            Double.POSITIVE_INFINITY).equals(
                               new Complex(Double.POSITIVE_INFINITY,
                                           Double.NEGATIVE_INFINITY)));
    assertFalse(new Complex(Double.POSITIVE_INFINITY,
                            Double.POSITIVE_INFINITY).equals(
                               new Complex(0.0,
                                           0.0)));
    assertFalse(new Complex(Double.POSITIVE_INFINITY,
                            Double.POSITIVE_INFINITY).equals(
                               new Complex(0.0,
                                           Double.POSITIVE_INFINITY)));
    assertFalse(new Complex(Double.POSITIVE_INFINITY,
                            Double.POSITIVE_INFINITY).equals(
                               new Complex(Double.POSITIVE_INFINITY,
                                           0.0)));
    assertFalse(new Complex(Double.POSITIVE_INFINITY,
                            0.0).equals(
                               new Complex(0.0,
                                           0.0)));
    assertFalse(new Complex(0.0,
                            Double.POSITIVE_INFINITY).equals(
                               new Complex(0.0,
                                           0.0)));
    assertFalse(new Complex(0.0,
                            Double.POSITIVE_INFINITY).equals(
                               new Complex(Double.POSITIVE_INFINITY,
                                           0.0)));

    // test negative zero
    assertFalse(new Complex(+0.0, +0.0).equals(new Complex(-0.0, -0.0)));
    assertFalse(new Complex(+0.0, +0.0).equals(new Complex(-0.0, +0.0)));
    assertFalse(new Complex(+0.0, +0.0).equals(new Complex(+0.0, -0.0)));
    assertFalse(new Complex(+0.0, -0.0).equals(new Complex(-0.0, -0.0)));
    assertFalse(new Complex(-0.0, +0.0).equals(new Complex(-0.0, -0.0)));
    assertFalse(new Complex(-0.0, +0.0).equals(new Complex(+0.0, -0.0)));

    assertTrue(ComplexUtils.equal(new Complex(+0.0, +0.0),
                                  new Complex(+0.0, +0.0)));
    assertTrue(ComplexUtils.equal(new Complex(+0.0, -0.0),
                                  new Complex(+0.0, -0.0)));
    assertTrue(ComplexUtils.equal(new Complex(-0.0, -0.0),
                                  new Complex(-0.0, -0.0)));
    assertTrue(ComplexUtils.equal(new Complex(-0.0, +0.0),
                                  new Complex(-0.0, +0.0)));
    assertFalse(ComplexUtils.equal(new Complex(+0.0, +0.0),
                                   new Complex(-0.0, -0.0)));
    assertFalse(ComplexUtils.equal(new Complex(+0.0, +0.0),
                                   new Complex(-0.0, +0.0)));
    assertFalse(ComplexUtils.equal(new Complex(+0.0, +0.0),
                                   new Complex(+0.0, -0.0)));
    assertFalse(ComplexUtils.equal(new Complex(+0.0, -0.0),
                                   new Complex(-0.0, -0.0)));
    assertFalse(ComplexUtils.equal(new Complex(-0.0, +0.0),
                                   new Complex(-0.0, -0.0)));
    assertFalse(ComplexUtils.equal(new Complex(-0.0, +0.0),
                                   new Complex(+0.0, -0.0)));

    assertTrue(ComplexUtils.longBitsEqual(new Complex(+0.0, +0.0),
                                          new Complex(+0.0, +0.0)));
    assertTrue(ComplexUtils.longBitsEqual(new Complex(+0.0, -0.0),
                                          new Complex(+0.0, -0.0)));
    assertTrue(ComplexUtils.longBitsEqual(new Complex(-0.0, +0.0),
                                          new Complex(-0.0, +0.0)));
    assertTrue(ComplexUtils.longBitsEqual(new Complex(-0.0, -0.0),
                                          new Complex(-0.0, -0.0)));
    assertFalse(ComplexUtils.longBitsEqual(new Complex(+0.0, +0.0),
                                           new Complex(-0.0, -0.0)));
    assertFalse(ComplexUtils.longBitsEqual(new Complex(+0.0, +0.0),
                                           new Complex(-0.0, +0.0)));
    assertFalse(ComplexUtils.longBitsEqual(new Complex(+0.0, +0.0),
                                           new Complex(+0.0, -0.0)));
    assertFalse(ComplexUtils.longBitsEqual(new Complex(+0.0, -0.0),
                                           new Complex(-0.0, -0.0)));
    assertFalse(ComplexUtils.longBitsEqual(new Complex(-0.0, +0.0),
                                           new Complex(-0.0, -0.0)));
    assertFalse(ComplexUtils.longBitsEqual(new Complex(-0.0, +0.0),
                                           new Complex(+0.0, -0.0)));

    assertTrue(ComplexUtils.equalRealImag(new Complex(+0.0, +0.0),
                                          new Complex(+0.0, +0.0)));
    assertTrue(ComplexUtils.equalRealImag(new Complex(+0.0, -0.0),
                                          new Complex(+0.0, -0.0)));
    assertTrue(ComplexUtils.equalRealImag(new Complex(-0.0, +0.0),
                                          new Complex(-0.0, +0.0)));
    assertTrue(ComplexUtils.equalRealImag(new Complex(-0.0, -0.0),
                                          new Complex(-0.0, -0.0)));
    assertTrue(ComplexUtils.equalRealImag(new Complex(+0.0, +0.0),
                                          new Complex(-0.0, -0.0)));
    assertTrue(ComplexUtils.equalRealImag(new Complex(+0.0, +0.0),
                                          new Complex(-0.0, +0.0)));
    assertTrue(ComplexUtils.equalRealImag(new Complex(+0.0, +0.0),
                                          new Complex(+0.0, -0.0)));
    assertTrue(ComplexUtils.equalRealImag(new Complex(+0.0, -0.0),
                                          new Complex(-0.0, -0.0)));
    assertTrue(ComplexUtils.equalRealImag(new Complex(-0.0, +0.0),
                                          new Complex(-0.0, -0.0)));
    assertTrue(ComplexUtils.equalRealImag(new Complex(-0.0, +0.0),
                                          new Complex(+0.0, -0.0)));

    // test Complex buffer: uses java.lang.Object's equals
    assertFalse(new ComplexBuffer().equals(new ComplexBuffer()));

    // trivial tests
    assertFalse(Complex.ZERO.equals(Complex.ONE));
    assertFalse(Complex.ONE.equals(Complex.I));
    assertFalse(new Complex(1, 2).equals(new Complex(2, 1)));
  }
  public static void main(String[] args)
  {
    /*
     * FIXME TODO add a new test for valueOf
     * FIXME TODO add a new test for newPolar
     */
    hashCodeTest();
    equalityTests();
  }
};
