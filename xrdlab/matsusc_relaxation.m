% Calculates susceptibility of a variable mixture
%
% parameters:
% varmix: a variable mixture
% forcexyspacing: force characteristic length parallel to surface to specific
%                 value (a strained layer)
% returns:
% xyspace: the characteristic length parallel to surface
% zspace: interplanar spacing
% suscdata: susceptibility data: [chi0, chih, chihinv]
function [suscdata,xyspace,zspace] = matsusc_relaxation(varmix, basexyspace, rel)
  lattice = mix2_relaxation(varmix, basexyspace, rel);

  occupation = lattice.occupation;
  r = lattice.r;
  B = lattice.B;
  hoenl = lattice.hoenl;
  sf_a = lattice.sf_a;
  sf_b = lattice.sf_b;
  sf_c = lattice.sf_c;
  V = lattice.V;
  H = lattice.H;
  xyspace = lattice.xyspace;
  zspace = lattice.zspace;
  lambda = lattice.lambda;

  % Here we use a dirty trick: 1/Inf == 0
  %chi0 = susc(occupation, r, B, hoenl, sf_a, sf_b, sf_c, V, [0,0,0], Inf, lambda);
  %chih = susc(occupation, r, B, hoenl, sf_a, sf_b, sf_c, V, H, zspace, lambda);
  %chihinv = susc(occupation, r, B, hoenl, sf_a, sf_b, sf_c, V, -H, zspace, lambda);
  %suscdata = [chi0, chih, chihinv];

  suscdata = susc3(occupation, r, B, hoenl, sf_a, sf_b, sf_c, V, H, zspace, lambda);
end
