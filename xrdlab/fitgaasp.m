% import measurement
scan = HRXRDimport('gaasp.x00');
theta = scan.theta';
meas = scan.meas';
lambda = scan.lambda;

% load material database
matZ = matdb;

% calculate material properties for the applied wavelength
GaAs = Zmat2mat(matZ.GaAs, lambda);
GaP = Zmat2mat(matZ.GaP, lambda);

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

% layer thickness
d_min = [20e-9,0.0];
d = [50e-9,0.0];
d_max = [100e-9,0.0];

%d_min = [54e-9,0.0];
%d = [54e-9,0.0];
%d_max = [54e-9,0.0];

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

% should be 3..10 times the number of effective fitting parameters
% (in this case, 15 .. 50)
npop = 40;
niter = 120;

% initialize fitting
ctx = fitDE_initXRD(theta, meas, mixtures, lambda, stddevrad, rel_min, rel, rel_max, d_min, d, d_max, p_min, p, p_max, prodfactor_min, prodfactor, prodfactor_max, sumterm_min, sumterm, sumterm_max, thetaoffset_min, thetaoffset, thetaoffset_max, mixing, 'CovDE', npop);

for iter=1:niter
    ctx = fitDE(ctx);
    y = fitDE_best(ctx);
    bestfitness = fitDE_best_fitness(ctx);
    medianfitness = fitDE_median_fitness(ctx);
    iter,bestfitness, medianfitness
    prodfactor = y(1);
    sumterm = y(2);
    thetaoffset = y(3);
    y2 = y(4:end);
    d = y2(1:end/3)';
    x = y2(end/3+1:2*end/3);
    rel = y2(2*end/3+1:end);

    prodfactor,sumterm,thetaoffset,x,d,rel

    simul = XRDindividuals(y, ctx.q);
    axis([theta(1), theta(end), log(scan.PhotonLevel*0.5), log(max(meas*2))]);
    plot(theta, log(meas), theta, log(simul));

    drawnow;
end

niter, bestfitness, medianfitness
prodfactor,sumterm,thetaoffset,x,d

pause;
