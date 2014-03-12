function E = bestf(dmatrix,npop,niter)
  scan = HRXRDimport('gaasp.x00');

  theta = scan.theta';
  meas = scan.meas';
  lambda = scan.lambda;

  % load material database
  matZ = matdb;

  % calculate material properties for the applied wavelength
  GaAs = Zmat2mat(matZ.GaAs_004, lambda);
  GaP = Zmat2mat(matZ.GaP_004, lambda);

  % fixed mixtures
  GaAs_mixture1 = {1.0, GaAs};
  GaP_mixture1 = {1.0, GaP};

  % variable mixtures
  GaAsP_mixture2.first = GaAs_mixture1;
  GaAsP_mixture2.second = GaP_mixture1;
  GaAsP_mixture2.x = 0; % will be replaced later with the value from p

  GaAs_mixture2.first = GaAs_mixture1;
  GaAs_mixture2.second = GaAs_mixture1;
  GaAs_mixture2.x = 0; % will be replaced later with the value from p

  % layer materials
  mixtures{1} = GaAsP_mixture2;
  mixtures{2} = GaAs_mixture2;

  % calculate GaAs_xyspace
  [dummy,GaAs_xyspace,dummy] = matsusc(GaAs_mixture2, 0);

  % mixing vector: the layer structure is
  % 1 (GaAsP)
  % 2 (GaAs substrate)
  % layers with the same number are fitted simultaneously
  mixing = [1,2];

  % The layers adopt the crystal structure of substrate
  % so they have the same in-plane lattice constant.
  %
  % layer materials are GaAsP, GaAs
  %
  % could be also [GaAs_xyspace, GaAs_xyspace]
  rel = [0, 1];
  rel_min = [0, 1];
  rel_max = [0, 1];

  % no instrument convolution (in radians)
  stddevrad = 0.0;

  % composition
  p_min = [0.0,0.0];
  p = [0.3,0.0];
  p_max = [0.5,0.0];

  %p_min = [0.36,0];
  %p = [0.36,0];
  %p_max = [0.36,0];

  % normalization: full intensity in decibels
  prodfactor_min = 30;
  prodfactor = 50;
  prodfactor_max = 70;

  % systematic error in measurement angle (in radians)
  thetaoffset_min = -0.05 * pi/180;
  thetaoffset = 0;
  thetaoffset_max = 0.05 * pi/180;

  % sum term in decibels
  % don't fit since background intensity is below photon counting noise
  % in this measurement
  sumterm_min = -100;
  sumterm = -100;
  sumterm_max = -100;

  
  dsize = size(dmatrix);
  E = zeros(size(dmatrix));
  for h = dsize(1)
      for k = dsize(2)
          thickness = dmatrix(h,k);
          % layer thickness
          d_min = [thickness,0.0];
          d = [thickness,0.0];
          d_max = [thickness,0.0];
          ctx = fitDE_initXRD(theta, meas, mixtures, lambda, stddevrad, rel_min, rel, rel_max, d_min, d, d_max, p_min, p, p_max, prodfactor_min, prodfactor, prodfactor_max, sumterm_min, sumterm, sumterm_max, thetaoffset_min, thetaoffset, thetaoffset_max, mixing, 'DE', npop);
          for iter=1:niter
            ctx = fitDE(ctx);
            fitDE_best_fitness(ctx)
          end
          E(h,k) = fitDE_best_fitness(ctx);
      end
  end
end
