% used by calcB
function y = errint(x)
  y = zeros(size(x));
  xsize = size(x);
  for h = 1:xsize(1)
      for k = 1:xsize(2)
          if x(h,k) == 0
              y(h,k) = 1
          else
              y(h,k) = quad(@(tau)(tau/(exp(tau)-1)),0,x(h,k))/x(h,k);
          end
      end
  end
end
