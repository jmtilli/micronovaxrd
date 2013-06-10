% A: matrix of atomic weights in g/mol
% Td: matrix of debye temperatures in K
% T: measurement temperature (or matrix of temperatures) in K
% returns: B in m^(-2)
%
% used by debyetemp.m
function B = calcB(T,Td,A)
  Na = 6.022e+23;
  h = 6.626068e-34;
  kb = 1.3806503e-23;

  m = A/(1000*Na);
  x = Td./T;
  B = 6*h^2./(kb * (m.*Td)) .* (errint(x)./x + 1/4);
end
