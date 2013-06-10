% initialize DE fitting with XRD layer model and fitness function.
% theta is in radians, meas is the linear measured intensity.
% mixtures is a cell array of material information in the format for matsusc.
% The composition parameter in mixtures is not used; composition
% must be specified separately in x.
% lambda is the wavelength in SI-units.
% rel is a row vector of the degrees of relaxation of
% strained layers. Unstrained layers have 1 in rel and fully strained layers have 0.
% d is a row vector of thicknesses in SI-units and x is a row vector of
% compositions. Their minimum and maximum values are
% specified along with their expected values (which are added to the
% initialized population). Look for algoname and npop in fitDE_init.m
%
% The individuals are [d,rho_e,r]. You need to manually extract d, rho_e
% and r from the fitting results by fitDE_best and fitDE_median

% all the layer properties are indexed through the indices parameter (TODO: explain better)

function ctx = fitDE_initXRD(theta, meas, mixtures, lambda, stddevrad, rel_min, rel, rel_max, d_min, d, d_max, x_min, x, x_max, prodfactor_min, prodfactor, prodfactor_max, sumterm_min, sumterm, sumterm_max, thetaoffset_min, thetaoffset, thetaoffset_max, indices, algoname, npop, func, g)
  q.theta = theta;
  q.meas = meas;
  %q.beta_coeff = beta_coeff;
  for k = 1:length(mixtures)
      q.mixctx{k} = matsusc2_premix(mixtures{k});
  end
  q.lambda = lambda;
  q.indices = indices;
  q.stddevrad = stddevrad;

  if exist('func','var')
      q.fitnessfunction = func;
      q.g = g;
  else
      q.fitnessfunction = @logfitnessfunction;
      q.g.pnorm = 2;
      q.g.threshold = 400;
  end

  ctx = fitDE_init(@XRDfitness, [prodfactor_min, sumterm_min, thetaoffset_min, d_min, x_min, rel_min], [prodfactor_max, sumterm_max, thetaoffset_max, d_max, x_max, rel_max], [prodfactor, sumterm, thetaoffset, d, x, rel], q, algoname, npop);
end
