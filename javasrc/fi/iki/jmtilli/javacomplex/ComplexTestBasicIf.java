package fi.iki.jmtilli.javacomplex;
/**
   Unit test for the basic interface of complex numbers
 */
public class ComplexTestBasicIf {
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
  private static void NaNTest()
  {
    assertFalse(Complex.ZERO.isNaN());
    assertFalse(Complex.ONE.isNaN());
    assertFalse(Complex.I.isNaN());
    assertFalse(Complex.I.multiply(3.0).isNaN());
    assertFalse(Complex.I.multiply(3.0).add(
                Complex.ONE.multiply(4.0)).isNaN());
    assertFalse(Complex.I.multiply(3.0).add(
                Complex.ONE.multiply(4.0)).inverse().isNaN());

    assertFalse(new Complex(Double.POSITIVE_INFINITY, 0.0).isNaN());
    assertFalse(new Complex(Double.POSITIVE_INFINITY, 1.0).isNaN());
    assertFalse(new Complex(Double.POSITIVE_INFINITY,
                            Double.NEGATIVE_INFINITY).isNaN());
    assertTrue(Complex.NaN.isNaN());
    assertTrue(new Complex(0.0, Double.NaN).isNaN());
    assertTrue(new Complex(Double.NaN, 1.0).isNaN());

    assertFalse(new ComplexBuffer(Double.POSITIVE_INFINITY, 0.0).isNaN());
    assertFalse(new ComplexBuffer(Double.POSITIVE_INFINITY, 1.0).isNaN());
    assertFalse(new ComplexBuffer(Double.POSITIVE_INFINITY,
                            Double.NEGATIVE_INFINITY).isNaN());
    assertTrue(new ComplexBuffer(Double.NaN, Double.NaN).isNaN());
    assertTrue(new ComplexBuffer(0.0, Double.NaN).isNaN());
    assertTrue(new ComplexBuffer(Double.NaN, 1.0).isNaN());

    assertFalse(ComplexUtils.isNaN(Complex.ZERO));
    assertFalse(ComplexUtils.isNaN(Complex.ONE));
    assertFalse(ComplexUtils.isNaN(Complex.I));
    assertFalse(ComplexUtils.isNaN(Complex.I.multiply(3.0)));
    assertFalse(ComplexUtils.isNaN(Complex.I.multiply(3.0).add(
                Complex.ONE.multiply(4.0))));
    assertFalse(ComplexUtils.isNaN(Complex.I.multiply(3.0).add(
                Complex.ONE.multiply(4.0)).inverse()));

    assertFalse(ComplexUtils.isNaN(new Complex(Double.POSITIVE_INFINITY, 0.0)));
    assertFalse(ComplexUtils.isNaN(new Complex(Double.POSITIVE_INFINITY, 1.0)));
    assertFalse(ComplexUtils.isNaN(new Complex(Double.POSITIVE_INFINITY,
                                               Double.NEGATIVE_INFINITY)));
    assertTrue(ComplexUtils.isNaN(Complex.NaN));
    assertTrue(ComplexUtils.isNaN(new Complex(0.0, Double.NaN)));
    assertTrue(ComplexUtils.isNaN(new Complex(Double.NaN, 1.0)));

    assertFalse(ComplexUtils.isNaN(new ComplexBuffer(Double.POSITIVE_INFINITY,
                                                     0.0)));
    assertFalse(ComplexUtils.isNaN(new ComplexBuffer(Double.POSITIVE_INFINITY,
                                                     1.0)));
    assertFalse(ComplexUtils.isNaN(new ComplexBuffer(
                                           Double.POSITIVE_INFINITY,
                                           Double.NEGATIVE_INFINITY)));
    assertTrue(ComplexUtils.isNaN(new ComplexBuffer(Double.NaN, Double.NaN)));
    assertTrue(ComplexUtils.isNaN(new ComplexBuffer(0.0, Double.NaN)));
    assertTrue(ComplexUtils.isNaN(new ComplexBuffer(Double.NaN, 1.0)));
  }
  private static void infTest()
  {
    assertFalse(Complex.ZERO.isInfinite());
    assertFalse(Complex.ONE.isInfinite());
    assertFalse(Complex.I.isInfinite());
    assertFalse(Complex.I.multiply(3.0).isInfinite());
    assertFalse(Complex.I.multiply(3.0).add(
                Complex.ONE.multiply(4.0)).isInfinite());
    assertFalse(Complex.I.multiply(3.0).add(
                Complex.ONE.multiply(4.0)).inverse().isInfinite());

    assertTrue(new Complex(Double.POSITIVE_INFINITY, 0.0).isInfinite());
    assertTrue(new Complex(Double.POSITIVE_INFINITY, 1.0).isInfinite());
    assertTrue(new Complex(Double.POSITIVE_INFINITY,
                            Double.NEGATIVE_INFINITY).isInfinite());
    assertFalse(new Complex(Double.POSITIVE_INFINITY, Double.NaN).isInfinite());
    assertFalse(new Complex(Double.NaN, Double.NEGATIVE_INFINITY).isInfinite());
    assertFalse(Complex.NaN.isInfinite());
    assertFalse(new Complex(0.0, Double.NaN).isInfinite());
    assertFalse(new Complex(Double.NaN, 1.0).isInfinite());

    assertTrue(new ComplexBuffer(Double.POSITIVE_INFINITY, 0.0).isInfinite());
    assertTrue(new ComplexBuffer(Double.POSITIVE_INFINITY, 1.0).isInfinite());
    assertTrue(new ComplexBuffer(Double.POSITIVE_INFINITY,
                                 Double.NEGATIVE_INFINITY).isInfinite());
    assertFalse(new ComplexBuffer(Double.POSITIVE_INFINITY,
                                  Double.NaN).isInfinite());
    assertFalse(new ComplexBuffer(Double.NaN,
                                  Double.NEGATIVE_INFINITY).isInfinite());
    assertFalse(new ComplexBuffer(Double.NaN, Double.NaN).isInfinite());
    assertFalse(new ComplexBuffer(0.0, Double.NaN).isInfinite());
    assertFalse(new ComplexBuffer(Double.NaN, 1.0).isInfinite());

    assertFalse(ComplexUtils.isInfinite(Complex.ZERO));
    assertFalse(ComplexUtils.isInfinite(Complex.ONE));
    assertFalse(ComplexUtils.isInfinite(Complex.I));
    assertFalse(ComplexUtils.isInfinite(Complex.I.multiply(3.0)));
    assertFalse(ComplexUtils.isInfinite(Complex.I.multiply(3.0).add(
                Complex.ONE.multiply(4.0))));
    assertFalse(ComplexUtils.isInfinite(Complex.I.multiply(3.0).add(
                Complex.ONE.multiply(4.0)).inverse()));

    assertTrue(ComplexUtils.isInfinite(new Complex(Double.POSITIVE_INFINITY,
                                                   0.0)));
    assertTrue(ComplexUtils.isInfinite(new Complex(Double.POSITIVE_INFINITY,
                                                   1.0)));
    assertTrue(ComplexUtils.isInfinite(new Complex(Double.POSITIVE_INFINITY,
                                                   Double.NEGATIVE_INFINITY)));
    assertFalse(ComplexUtils.isInfinite(new Complex(Double.POSITIVE_INFINITY,
                                                    Double.NaN)));
    assertFalse(ComplexUtils.isInfinite(new Complex(Double.NaN,
                                                    Double.NEGATIVE_INFINITY)));
    assertFalse(ComplexUtils.isInfinite(Complex.NaN));
    assertFalse(ComplexUtils.isInfinite(new Complex(0.0, Double.NaN)));
    assertFalse(ComplexUtils.isInfinite(new Complex(Double.NaN, 1.0)));

    assertTrue(ComplexUtils.isInfinite(new ComplexBuffer(
                                             Double.POSITIVE_INFINITY, 0.0)));
    assertTrue(ComplexUtils.isInfinite(new ComplexBuffer(
                                             Double.POSITIVE_INFINITY, 1.0)));
    assertTrue(ComplexUtils.isInfinite(new ComplexBuffer(
                                             Double.POSITIVE_INFINITY,
                                             Double.NEGATIVE_INFINITY)));
    assertFalse(ComplexUtils.isInfinite(new ComplexBuffer(
                                              Double.POSITIVE_INFINITY,
                                              Double.NaN)));
    assertFalse(ComplexUtils.isInfinite(new ComplexBuffer(
                                              Double.NaN,
                                              Double.NEGATIVE_INFINITY)));
    assertFalse(ComplexUtils.isInfinite(new ComplexBuffer(
                                              Double.NaN,
                                              Double.NaN)));
    assertFalse(ComplexUtils.isInfinite(new ComplexBuffer(0.0, Double.NaN)));
    assertFalse(ComplexUtils.isInfinite(new ComplexBuffer(Double.NaN, 1.0)));
  }
  private static void absTest()
  {
    assertEqual(Complex.ZERO.abs(), 0.0);
    assertEqualTolerance(1e-10, Complex.ONE.abs(), 1.0);
    assertEqualTolerance(1e-10, Complex.I.abs(), 1.0);
    assertEqualTolerance(1e-10, Complex.I.multiply(3.0).add(
                                  Complex.ONE.multiply(4.0)).abs(), 5.0);
    assertEqual(new Complex(Double.MAX_VALUE, 0.0).abs(), Double.MAX_VALUE);
    assertEqual(new Complex(0.0, Double.MAX_VALUE).abs(), Double.MAX_VALUE);

    assertEqual(new Complex(0.0, Double.NaN).abs(), Double.NaN);
    assertEqual(new Complex(Double.NaN, 0.0).abs(), Double.NaN);
    assertEqual(new Complex(Double.NaN, Double.POSITIVE_INFINITY).abs(),
                Double.NaN);
    assertEqual(new Complex(Double.POSITIVE_INFINITY, Double.NaN).abs(),
                Double.NaN);
    assertEqual(new Complex(Double.NaN, Double.NEGATIVE_INFINITY).abs(),
                Double.NaN);
    assertEqual(new Complex(Double.NEGATIVE_INFINITY, Double.NaN).abs(),
                Double.NaN);
    assertEqual(new Complex(Double.NaN, Double.NaN).abs(), Double.NaN);
    assertEqual(Complex.NaN.abs(), Double.NaN);

    assertEqual(new Complex(Double.POSITIVE_INFINITY,
                            Double.POSITIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new Complex(Double.NEGATIVE_INFINITY,
                            Double.POSITIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new Complex(Double.POSITIVE_INFINITY,
                            Double.NEGATIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new Complex(Double.NEGATIVE_INFINITY,
                            Double.NEGATIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new Complex(0.0,
                            Double.POSITIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new Complex(Double.NEGATIVE_INFINITY,
                            1.0).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new Complex(-0.00001,
                            Double.NEGATIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new Complex(-Double.MAX_VALUE,
                            -Double.MAX_VALUE).abs(),
                Double.POSITIVE_INFINITY);

    assertEqual(new ComplexBuffer(Double.MAX_VALUE, 0.0).abs(),
                Double.MAX_VALUE);
    assertEqual(new ComplexBuffer(0.0, Double.MAX_VALUE).abs(),
                Double.MAX_VALUE);

    assertEqual(new ComplexBuffer(0.0, Double.NaN).abs(), Double.NaN);
    assertEqual(new ComplexBuffer(Double.NaN, 0.0).abs(), Double.NaN);
    assertEqual(new ComplexBuffer(Double.NaN, Double.POSITIVE_INFINITY).abs(),
                Double.NaN);
    assertEqual(new ComplexBuffer(Double.POSITIVE_INFINITY, Double.NaN).abs(),
                Double.NaN);
    assertEqual(new ComplexBuffer(Double.NaN, Double.NEGATIVE_INFINITY).abs(),
                Double.NaN);
    assertEqual(new ComplexBuffer(Double.NEGATIVE_INFINITY, Double.NaN).abs(),
                Double.NaN);
    assertEqual(new ComplexBuffer(Double.NaN, Double.NaN).abs(), Double.NaN);
    assertEqual(Complex.NaN.abs(), Double.NaN);

    assertEqual(new ComplexBuffer(Double.POSITIVE_INFINITY,
                                  Double.POSITIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new ComplexBuffer(Double.NEGATIVE_INFINITY,
                                  Double.POSITIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new ComplexBuffer(Double.POSITIVE_INFINITY,
                                  Double.NEGATIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new ComplexBuffer(Double.NEGATIVE_INFINITY,
                                  Double.NEGATIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new ComplexBuffer(0.0,
                                  Double.POSITIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new ComplexBuffer(Double.NEGATIVE_INFINITY,
                                  1.0).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new ComplexBuffer(-0.00001,
                                  Double.NEGATIVE_INFINITY).abs(),
                Double.POSITIVE_INFINITY);
    assertEqual(new ComplexBuffer(-Double.MAX_VALUE,
                                  -Double.MAX_VALUE).abs(),
                Double.POSITIVE_INFINITY);
  }
  private static void argTest()
  {
    // FIXME add more tests
    assertEqual(Complex.ZERO.arg(), 0.0);
    assertEqual(Complex.ONE.arg(), 0.0);
    assertEqualTolerance(1e-10,
                         new Complex(1.0, 0.0).arg(),
                         new Complex(Double.MAX_VALUE, Double.MIN_VALUE).arg());
    assertEqualTolerance(1e-10,
                         new Complex(0.0, 1.0).arg(),
                         new Complex(Double.MIN_VALUE, Double.MAX_VALUE).arg());

    assertEqual(new Complex(0.0, Double.NaN).arg(), Double.NaN);
    assertEqual(new Complex(Double.NaN, 0.0).arg(), Double.NaN);
    assertEqual(new Complex(Double.NaN, Double.POSITIVE_INFINITY).arg(),
                Double.NaN);
    assertEqual(new Complex(Double.POSITIVE_INFINITY, Double.NaN).arg(),
                Double.NaN);
    assertEqual(new Complex(Double.NaN, Double.NEGATIVE_INFINITY).arg(),
                Double.NaN);
    assertEqual(new Complex(Double.NEGATIVE_INFINITY, Double.NaN).arg(),
                Double.NaN);
    assertEqual(new Complex(Double.NaN, Double.NaN).arg(), Double.NaN);
    assertEqual(Complex.NaN.arg(), Double.NaN);

    assertEqualTolerance(1e-10,
                         new ComplexBuffer(1.0, 0.0).arg(),
                         new ComplexBuffer(Double.MAX_VALUE,
                                           Double.MIN_VALUE).arg());
    assertEqualTolerance(1e-10,
                         new ComplexBuffer(0.0, 1.0).arg(),
                         new ComplexBuffer(Double.MIN_VALUE,
                                           Double.MAX_VALUE).arg());

    assertEqual(new ComplexBuffer(0.0, Double.NaN).arg(), Double.NaN);
    assertEqual(new ComplexBuffer(Double.NaN, 0.0).arg(), Double.NaN);
    assertEqual(new ComplexBuffer(Double.NaN, Double.POSITIVE_INFINITY).arg(),
                Double.NaN);
    assertEqual(new ComplexBuffer(Double.POSITIVE_INFINITY, Double.NaN).arg(),
                Double.NaN);
    assertEqual(new ComplexBuffer(Double.NaN, Double.NEGATIVE_INFINITY).arg(),
                Double.NaN);
    assertEqual(new ComplexBuffer(Double.NEGATIVE_INFINITY, Double.NaN).arg(),
                Double.NaN);
    assertEqual(new ComplexBuffer(Double.NaN, Double.NaN).arg(), Double.NaN);
  }
  private static void realTest()
  {
    assertEqual(new Complex(Double.MAX_VALUE, Double.MIN_VALUE).getReal(),
                Double.MAX_VALUE);
    assertEqual(new Complex(-Double.MAX_VALUE, Double.MIN_VALUE).getReal(),
                -Double.MAX_VALUE);
    assertEqual(new Complex(0.0, Double.MIN_VALUE).getReal(),
                0.0);
    assertEqual(new Complex(1.0, Double.MIN_VALUE).getReal(),
                1.0);
    assertEqual(new Complex(Double.MAX_VALUE, 2.0).getReal(),
                Double.MAX_VALUE);
    assertEqual(new Complex(-Double.MAX_VALUE, 4.0).getReal(),
                -Double.MAX_VALUE);
    assertEqual(new Complex(0.0, -6.0).getReal(),
                0.0);
    assertEqual(new Complex(1.0, -8.3).getReal(),
                1.0);

    assertEqual(new ComplexBuffer(Double.MAX_VALUE, Double.MIN_VALUE).getReal(),
                Double.MAX_VALUE);
    assertEqual(new ComplexBuffer(-Double.MAX_VALUE,
                                  Double.MIN_VALUE).getReal(),
                -Double.MAX_VALUE);
    assertEqual(new ComplexBuffer(0.0, Double.MIN_VALUE).getReal(),
                0.0);
    assertEqual(new ComplexBuffer(1.0, Double.MIN_VALUE).getReal(),
                1.0);
    assertEqual(new ComplexBuffer(Double.MAX_VALUE, 2.0).getReal(),
                Double.MAX_VALUE);
    assertEqual(new ComplexBuffer(-Double.MAX_VALUE, 4.0).getReal(),
                -Double.MAX_VALUE);
    assertEqual(new ComplexBuffer(0.0, -6.0).getReal(),
                0.0);
    assertEqual(new ComplexBuffer(1.0, -8.3).getReal(),
                1.0);
  }
  private static void imagTest()
  {
    assertEqual(new Complex(Double.MAX_VALUE, Double.MIN_VALUE).getImag(),
                Double.MIN_VALUE);
    assertEqual(new Complex(-Double.MAX_VALUE, Double.MIN_VALUE).getImag(),
                Double.MIN_VALUE);
    assertEqual(new Complex(0.0, Double.MIN_VALUE).getImag(),
                Double.MIN_VALUE);
    assertEqual(new Complex(1.0, Double.MIN_VALUE).getImag(),
                Double.MIN_VALUE);
    assertEqual(new Complex(Double.MAX_VALUE, 2.0).getImag(),
                2.0);
    assertEqual(new Complex(-Double.MAX_VALUE, 4.0).getImag(),
                4.0);
    assertEqual(new Complex(0.0, -6.0).getImag(),
                -6.0);
    assertEqual(new Complex(1.0, -8.3).getImag(),
                -8.3);

    assertEqual(new ComplexBuffer(Double.MAX_VALUE,
                                  Double.MIN_VALUE).getImag(),
                Double.MIN_VALUE);
    assertEqual(new ComplexBuffer(-Double.MAX_VALUE,
                                  Double.MIN_VALUE).getImag(),
                Double.MIN_VALUE);
    assertEqual(new ComplexBuffer(0.0, Double.MIN_VALUE).getImag(),
                Double.MIN_VALUE);
    assertEqual(new ComplexBuffer(1.0, Double.MIN_VALUE).getImag(),
                Double.MIN_VALUE);
    assertEqual(new ComplexBuffer(Double.MAX_VALUE, 2.0).getImag(),
                2.0);
    assertEqual(new ComplexBuffer(-Double.MAX_VALUE, 4.0).getImag(),
                4.0);
    assertEqual(new ComplexBuffer(0.0, -6.0).getImag(),
                -6.0);
    assertEqual(new ComplexBuffer(1.0, -8.3).getImag(),
                -8.3);
  }
  private static void toStringTest()
  {
    assertEqual(new Complex(Double.NaN, 0).toString(), "NaN");
    assertEqual(new Complex(Double.NaN, Double.NaN).toString(), "NaN");
    assertEqual(new Complex(0, Double.NaN).toString(), "NaN");
    assertEqual(new Complex(1, 0).toString(), "1.0");
    assertEqual(new Complex(1, 2).toString(), "1.0 + 2.0i");
    assertEqual(new Complex(1, -2).toString(), "1.0 - 2.0i");
    assertEqual(new Complex(0, 1).toString(), "1.0i");
    assertEqual(new Complex(0, Double.POSITIVE_INFINITY).toString(),
                "Infinityi");
    assertEqual(new Complex(Double.POSITIVE_INFINITY, 0).toString(),
                "Infinity");
    assertEqual(new Complex(Double.POSITIVE_INFINITY, 1).toString(),
                "Infinity + 1.0i");
    assertEqual(new Complex(2, Double.POSITIVE_INFINITY).toString(),
                "2.0 + Infinityi");
    assertEqual(new Complex(Double.POSITIVE_INFINITY,
                            Double.NEGATIVE_INFINITY).toString(),
                "Infinity - Infinityi");

    assertEqual(new ComplexBuffer(Double.NaN, 0).toString(), "NaN");
    assertEqual(new ComplexBuffer(Double.NaN, Double.NaN).toString(), "NaN");
    assertEqual(new ComplexBuffer(0, Double.NaN).toString(), "NaN");
    assertEqual(new ComplexBuffer(1, 0).toString(), "1.0");
    assertEqual(new ComplexBuffer(1, 2).toString(), "1.0 + 2.0i");
    assertEqual(new ComplexBuffer(1, -2).toString(), "1.0 - 2.0i");
    assertEqual(new ComplexBuffer(0, 1).toString(), "1.0i");
    assertEqual(new ComplexBuffer(0, Double.POSITIVE_INFINITY).toString(),
                "Infinityi");
    assertEqual(new ComplexBuffer(Double.POSITIVE_INFINITY, 0).toString(),
                "Infinity");
    assertEqual(new ComplexBuffer(Double.POSITIVE_INFINITY, 1).toString(),
                "Infinity + 1.0i");
    assertEqual(new ComplexBuffer(2, Double.POSITIVE_INFINITY).toString(),
                "2.0 + Infinityi");
    assertEqual(new ComplexBuffer(Double.POSITIVE_INFINITY,
                                  Double.NEGATIVE_INFINITY).toString(),
                "Infinity - Infinityi");
  }
  public static void main(String[] args)
  {
    absTest();
    argTest();
    NaNTest();
    infTest();
    realTest();
    imagTest();
    toStringTest();
  }
};
