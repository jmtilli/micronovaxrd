package fi.iki.jmtilli.javacomplex;
/**
   Performance test for immutable complex numbers.

   Shows that having a common interface to mutable and immutable
   complex numbers has no performance penalty, but the garbage-collection
   of immutable complex numbers has a large performance penalty.
 */
public class ComplexPerformanceTest {
  public static void main(String[] args)
  {
    Complex[] ar = new Complex[1000];
    Complex sum;
    ComplexBuffer BUF_ZERO = new ComplexBuffer();
    int i;
    int j;
    for(i = 0; i < 1000; i++)
    {
      ar[i] = Complex.ZERO;
    }
    sum = Complex.ZERO;
    for(j = 0; j < 1000*1000; j++)
    {
      for(i = 0; i < 1000; i++)
      {
        ar[i] = new Complex(i, i);
      }
      sum = Complex.ZERO;
      for(i = 0; i < 1000; i++)
      {
        sum = sum.add(ar[i]);
      }
      // We do this so that the argument can be either Complex or ComplexBuffer.
      // Makes it bit harder for the JIT to optimize if the argument can be
      // either one of these.
      sum = sum.add(BUF_ZERO);
      if (sum.getReal() != 1000*(1000-1)/2 || sum.getImag() != 1000*(1000-1)/2)
      {
        System.out.println("err");
      }
    }
  }
};
