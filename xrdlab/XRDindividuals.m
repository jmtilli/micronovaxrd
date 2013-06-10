% Calculate XRD curves for population

function R = XRDindividuals(pop,q)
  % both are in decibels to make the fitting more efficient
  prodfactor = 10 .^ (pop(:,1) / 10);
  sumterm = 10 .^ (pop(:,2) / 10);
  thetaoffset = pop(:,3);

  pop_dxr = pop(:,4:end);

  npop = size(pop,1);
  ntheta = length(q.theta);

  d = pop_dxr(:,1:end/3);
  x = pop_dxr(:,end/3+1:2*end/3);
  rel = pop_dxr(:,2*end/3+1:end);

  nlayer = length(q.mixctx);
  npos = length(q.indices);


  R = zeros(npop,ntheta);

  for k = 1:npop
      zspace = zeros(npos,1);
      suscdata = zeros(npos,3);

      dk = d(k,:)';
      dk = dk(q.indices);

      [suscdata(end,:), basexyspace, zspace(end)] = matsusc2_premixed(q.mixctx{end}, 0, 1, x(k,end)); % substrate is fully relaxed

      for h = (npos-1):(-1):1
          l = q.indices(h);
          [suscdata(h,:), basexyspace, zspace(h)] = matsusc2_premixed(q.mixctx{l}, basexyspace, rel(k,l), x(k,l));
      end
      R(k,:) = XRDCurve(q.lambda, dk, zspace, suscdata, q.theta, q.stddevrad, thetaoffset(k));
      R(k,:) = R(k,:)*prodfactor(k) + sumterm(k);
  end
end
