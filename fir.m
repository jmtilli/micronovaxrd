function y = fir(b,s,low,high)
  %y = filter(b,1,[s,zeros(1,high-length(s)-1)])(low:end);
  n1 = max(low-length(b)+1,1);
  n2 = 1+low-n1;
  y = filter(b,1,[s(n1:end),zeros(1,high-length(s)-1)]);
  y = y(n2:end);
end
