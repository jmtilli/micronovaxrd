% Optimize calculation of susceptibilities for a variable
% mixture as a function of x and forcexyspace.
%
% The general contract is that
%   matsusc(varmix, forcexyspace)
% gives always the same values as
%   matsusc2_premixed(matsusc2_premix(varmix), forcexyspace, varmix.x)
% within the accurary of floating-point calculation
function mixctx = matsusc2_premix(varmix)
  % Calculate lattice constants
  elasticprop1 = mix_latticeconst(varmix.first);
  elasticprop2 = mix_latticeconst(varmix.second);
  mixctx.poissonratio1 = elasticprop1.poissonratio;
  mixctx.poissonratio2 = elasticprop2.poissonratio;
  mixctx.xyspace1 = elasticprop1.xyspace;
  mixctx.xyspace2 = elasticprop2.xyspace;
  mixctx.zspace1 = elasticprop1.zspace;
  mixctx.zspace2 = elasticprop2.zspace;


  varmix.x = 0;
  material1 = mix2_relaxation(varmix, 0, 1); % unstrained
  varmix.x = 1;
  material2 = mix2_relaxation(varmix, 0, 1);
  % here material1.r == material2.r and material1.<x> == material2.<x> where <x> == B, hoenl, sf_a, sf_b or sf_c

  relV1 = material1.V / (mixctx.xyspace1^2 * mixctx.zspace1);
  relV2 = material2.V / (mixctx.xyspace2^2 * mixctx.zspace2);
  relV = mean([relV1,relV2]);
  assert(all(abs([relV1,relV2]/relV-1)<1e-3));

  lambda = mean([material1.lambda,material2.lambda]);
  assert(all(abs([material1.lambda,material2.lambda]/lambda-1)<1e-4));

  mixctx.relV = relV;
  mixctx.r = material1.r;

  mixctx.B = material1.B;
  mixctx.hoenl = material1.hoenl;
  mixctx.sf_a = material1.sf_a;
  mixctx.sf_b = material1.sf_b;
  mixctx.sf_c = material1.sf_c;
  %mixctx.xyspace1 = material1.xyspace;
  %mixctx.xyspace2 = material2.xyspace;
  %mixctx.zspace1 = material1.zspace;
  %mixctx.zspace2 = material2.zspace;
  %mixctx.poissonratio1 = material1.poissonratio;
  %mixctx.poissonratio2 = material2.poissonratio;
  mixctx.occupation1 = material1.occupation;
  mixctx.occupation2 = material2.occupation;
  mixctx.H = material1.H;
  mixctx.lambda = lambda;
end
