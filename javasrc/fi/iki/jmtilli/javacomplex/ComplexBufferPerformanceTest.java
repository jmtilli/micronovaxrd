package fi.iki.jmtilli.javacomplex;
/**
   Performance test for mutable complex numbers.

   Shows that having a common interface to mutable and immutable
   complex numbers has no performance penalty, but the garbage-collection
   of immutable complex numbers has a large performance penalty.
 */
public class ComplexBufferPerformanceTest {
  public static void main(String[] args)
  {
    ComplexBuffer[] ar = new ComplexBuffer[1000];
    ComplexBuffer sum;
    int i;
    int j;
    for(i = 0; i < 1000; i++)
    {
      ar[i] = new ComplexBuffer(Complex.ZERO);
    }
    sum = new ComplexBuffer(Complex.ZERO);
    for(j = 0; j < 1000*1000; j++)
    {
      for(i = 0; i < 1000; i++)
      {
        ar[i].set(i, i);
      }
      sum.set(Complex.ZERO);
      for(i = 0; i < 1000; i++)
      {
        sum.addInPlace(ar[i]);
      }
      // We do this so that the argument can be either Complex or ComplexBuffer.
      // Makes it bit harder for the JIT to optimize if the argument can be
      // either one of these.
      sum.addInPlace(Complex.ZERO);
      if (sum.getReal() != 1000*(1000-1)/2 || sum.getImag() != 1000*(1000-1)/2)
      {
        System.out.println("err");
      }
    }
  }
};
