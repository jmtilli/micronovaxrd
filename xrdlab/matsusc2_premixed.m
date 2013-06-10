% Calculate susceptibility for premixed variable mixture
% mixctx for the provided values of forcexyspace and x
function [suscdata,xyspace,zspace] = matsusc2_premixed(mixctx, basexyspace, rel, x)

  lambda = mixctx.lambda;

  xyspace = mixctx.xyspace1 .* (1-x) + mixctx.xyspace2 .* x;
  zspace = mixctx.zspace1 .* (1-x) + mixctx.zspace2 .* x;
  poissonratio = mixctx.poissonratio1 .* (1-x) + mixctx.poissonratio2 .* x;
  occupation = mixctx.occupation1 .* (1-x) + mixctx.occupation2 .* x;

  forcexyspace = rel*xyspace + (1-rel)*basexyspace;

  zspace = ((-2*poissonratio/(1-poissonratio) * (forcexyspace-xyspace)/xyspace) + 1) * zspace;
  xyspace = forcexyspace;

  % XXX: for efficiency, we don't check that V1 =~ V2
  %V = mixctx.V1*xyspace^2*zspace / mixctx.xyspace1^2 / mixctx.zspace1;
  %V = mixctx.V2*xyspace^2*zspace / mixctx.xyspace2^2 / mixctx.zspace2;
  V = mixctx.relV*xyspace^2*zspace;

  r = mixctx.r;
  B = mixctx.B;
  hoenl = mixctx.hoenl;
  sf_a = mixctx.sf_a;
  sf_b = mixctx.sf_b;
  sf_c = mixctx.sf_c;
  H = mixctx.H;

  suscdata = susc3(occupation, r, B, hoenl, sf_a, sf_b, sf_c, V, H, zspace, lambda);
end
