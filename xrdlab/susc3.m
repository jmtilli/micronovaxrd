function suscdata = susc3(occupation, r, B, hoenl, sf_a, sf_b, sf_c, V, H, zspace, lambda)

  r_e = 2.817940325e-15;

  s = 1/(2*zspace);

  DW = exp(-B.*s^2);
  asf = sum(exp(-sf_b*s^2).*sf_a,2)+sf_c;
  f = (asf + hoenl).*DW;

  expterm = exp(-2*pi*i*r*H');
  occterm = occupation .* f;
  occterm0 = occupation .* (sf_c + sum(sf_a,2) + hoenl);
  suscdata = -r_e * lambda^2 * [sum(occterm0), occterm.' * expterm, occterm.' * conj(expterm)] / (pi*V);
end
