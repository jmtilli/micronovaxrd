% fitness function for x-ray reflectivity curves

% TODO: update documentation
% q is a struct:
% q.alpha_0 is an 1xk vector of angle points in radians
% q.meas is a list of linear intensities for each angle in q.alpha_0
% q.beta_coeff is an 1xn vector of the coefficients beta/delta for each n layers
% q.lambda is the wavelength in meters
% q.stddevrad is the standard deviation of the convolution gaussian in radians
% q.pnorm is the p-norm to use in fitness calculation. Usually this is 1 or 2.

function E = XRDfitness(pop,q)
  R = XRDindividuals(pop,q);

  npop = size(pop,1);

  E = zeros(npop,1);

  for k = 1:npop
	  E(k) = feval(q.fitnessfunction,R(k,:),q.meas,q.g);
  end



  % debugging code, plot the best individual of the NEW population
  % not modified for forcexyspace -> rel -change
  %[minval, minind] = min(E);
  %dmin = d(minind,:)';
  %zspace = zeros(nlayer,1);
  %suscdata = zeros(nlayer,3);
  %for h = 1:nlayer
  %    [suscdata(h,:), xyspace_h, zspace(h)] = matsusc2_premixed(q.mixctx{h}, q.forcexyspace(h), x(minind,h));
  %end
  %R = XRDCurve(q.lambda, dmin(q.indices), zspace(q.indices), suscdata(q.indices,:), q.theta, q.stddevrad, thetaoffset(minind));
  %R = R*prodfactor(minind) + sumterm(minind);
  %plot(q.theta, log(R), q.theta, log(q.meas));
end
